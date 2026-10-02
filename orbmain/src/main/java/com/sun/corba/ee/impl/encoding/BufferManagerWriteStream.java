/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Eclipse Distribution License
 * v. 1.0 which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License v. 2.0 are satisfied: GNU General Public License v2.0
 * w/Classpath exception which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR BSD-3-Clause OR GPL-2.0 WITH
 * Classpath-exception-2.0
 */

package com.sun.corba.ee.impl.encoding;

import com.sun.corba.ee.impl.protocol.giopmsgheaders.FragmentMessage;
import com.sun.corba.ee.impl.protocol.giopmsgheaders.Message;
import com.sun.corba.ee.impl.protocol.giopmsgheaders.MessageBase;
import com.sun.corba.ee.impl.protocol.giopmsgheaders.ReplyMessage;
import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.transport.ByteBufferPool;
import com.sun.corba.ee.spi.transport.Connection;
import com.sun.corba.ee.spi.transport.ContactInfoListIterator;

import java.nio.ByteBuffer;
import java.util.EmptyStackException;

import org.glassfish.pfl.basic.reflection.Bridge;
import org.omg.CORBA.SystemException;
import org.omg.CORBA.portable.RemarshalException;

/**
 * Streaming buffer manager.
 *
 * <p>Fragments are not written one by one. Each was a write on the connection of its own, under the connection's write
 * lock: with GlassFish's default of 1 KiB fragments a 64 KiB value cost about 130 system calls each way, and in a
 * measurement on a value of a few kilobytes the write calls per call fell from 3.3 to 1.3 at 8 KiB fragments. A full
 * fragment is now copied into a holding buffer instead, after its header is completed, and the fragments held are
 * written together when the next one would take the buffer past {@link #HELD_BYTES}, and with the last fragment of the
 * message. Copying a fragment costs far less than the system call it saves. The fragments on the wire are the same,
 * byte for byte, in the same order; they leave in groups.
 */
public class BufferManagerWriteStream extends BufferManagerWrite {
    /**
     * Most bytes of complete fragments held before they are written. A fragment larger than half of it is written on
     * its own, as before: there is little to gain, and the receiver would wait longer for the first fragment.
     */
    static final int HELD_BYTES = 16 * 1024;

    private int fragmentCount = 0;

    // Complete fragments, back to back, not yet written; null until the first.
    private ByteBuffer held;
    private int heldFragments;

    BufferManagerWriteStream(ORB orb) {
        super(orb);
    }

    /**
     * Whether a fragment has been written to the connection; fragments still held do not count.
     */
    @Override
    public boolean sentFragment() {
        return fragmentCount > 0;
    }

    /**
     * The size of the buffer a stream starts with: the configured GIOP
     * buffer size, but never more than a fragment.
     *
     * <p>This used to be the fragment size itself, so raising the fragment
     * size to cut the number of fragments - and of writes - for large
     * messages also made every small message allocate a buffer that large.
     * The stream now starts small and {@link #expandWithinFragment} grows
     * the buffer, doubling, up to the fragment size; fragments are still
     * cut at exactly the same points. With the defaults, where the two sizes
     * are equal, nothing changes.
     */
    @Override
    public int getBufferSize() {
        return Math.min(orb.getORBData().getGIOPBufferSize(), getFragmentSize());
    }

    private int getFragmentSize() {
        return orb.getORBData().getGIOPFragmentSize();
    }

    @Override
    public ByteBuffer expandWithinFragment(ByteBuffer byteBuffer, int numBytesNeeded) {
        int fragmentSize = getFragmentSize();
        int needed = byteBuffer.position() + numBytesNeeded;
        if (byteBuffer.limit() >= fragmentSize || needed > fragmentSize) {
            // Either the buffer is a whole fragment already, or the data
            // would not fit in one anyway: this is a real overflow.
            return null;
        }

        int newSize = Math.min(fragmentSize, Math.max(needed, byteBuffer.limit() * 2));
        ByteBufferPool byteBufferPool = orb.getByteBufferPool();
        ByteBuffer larger = byteBufferPool.getByteBuffer(newSize);
        larger.order(byteBuffer.order());
        larger.limit(newSize);

        byteBuffer.flip();
        larger.put(byteBuffer);
        byteBufferPool.releaseByteBuffer(byteBuffer);
        return larger;
    }

    protected ByteBuffer overflow(ByteBuffer byteBuffer, int numBytesNeeded) {
        // Set the fragment's moreFragments field to true
        MessageBase.setFlag(byteBuffer, Message.MORE_FRAGMENTS_BIT);

        try {
            if (getFragmentSize() * 2 <= HELD_BYTES) {
                hold(((CDROutputObject) outputObject).sealFragment());
            } else {
                sendFragment(false);
            }
        } catch (SystemException se) {
            // REVISIT: this part similar to
            // CorbaClientRequestDispatchImpl.beginRequest()
            // and CorbaClientRequestDelegate.request()
            ContactInfoListIterator itr;
            try {
                itr = getContactInfoListIterator();
            } catch (EmptyStackException ese) {
                // server side, don't reportException
                throw se;
            }

            // bug 6382377: must not lose exception in PI
            orb.getPIHandler().invokeClientPIEndingPoint(ReplyMessage.SYSTEM_EXCEPTION, se);

            boolean retry = itr.reportException(null, se);
            if (retry) {
                Bridge bridge = Bridge.get();
                bridge.throwException(new RemarshalException());
            } else {
                // re-throw the SystemException
                throw se;
            }
        }

        // Reuse the old buffer

        // REVISIT - need to account for case when needed > available
        // even after fragmenting. This is the large array case, so
        // the caller should retry when it runs out of space.
        byteBuffer.position(0);
        byteBuffer.limit(byteBuffer.capacity());

        // Now we must marshal in the fragment header/GIOP header

        // REVISIT - we can optimize this by not creating the fragment message
        // each time.

        FragmentMessage header = ((CDROutputObject) outputObject).getMessageHeader().createFragmentMessage();

        header.write(((CDROutputObject) outputObject));
        return byteBuffer;
    }

    @Override
    public boolean isFragmentOnOverflow() {
        return true;
    }

    private void sendFragment(boolean isLastFragment) {
        Connection conn = ((CDROutputObject) outputObject).getMessageMediator().getConnection();

        // REVISIT: need an ORB
        // System.out.println("sendFragment: last?: " + isLastFragment);
        conn.writeLock();

        try {
            // Send the fragment
            conn.sendWithoutLock(((CDROutputObject) outputObject));

            fragmentCount++;

        } finally {

            conn.writeUnlock();
        }

    }

    /**
     * Copies a complete fragment into the holding buffer, and writes what is held once another whole fragment would
     * not fit in {@link #HELD_BYTES}. So there is always room for the fragment being held: it is never larger than a
     * fragment.
     *
     * @param fragment the stream's buffer, its position at the end of the fragment
     */
    private void hold(ByteBuffer fragment) {
        int length = fragment.position();
        if (held == null || held.remaining() < length) {
            // Room for four fragments to start with, as most messages that
            // fragment at all are a few kilobytes; doubled when it is full.
            int used = held == null ? 0 : held.position();
            int capacity = held == null ? 4 * getFragmentSize() : 2 * held.capacity();
            ByteBufferPool byteBufferPool = orb.getByteBufferPool();
            ByteBuffer larger = byteBufferPool.getByteBuffer(Math.min(HELD_BYTES, Math.max(capacity, used + length)));
            larger.clear();
            if (held != null) {
                held.flip();
                larger.put(held);
                byteBufferPool.releaseByteBuffer(held);
            }
            held = larger;
        }

        fragment.flip();
        held.put(fragment);
        heldFragments++;

        if (held.position() + getFragmentSize() > HELD_BYTES) {
            sendHeld();
        }
    }

    /** Writes the fragments held, under the connection's write lock, and empties the holding buffer. */
    private void sendHeld() {
        Connection conn = ((CDROutputObject) outputObject).getMessageMediator().getConnection();
        conn.writeLock();
        try {
            held.flip();
            conn.sendWithoutLock(held);
            fragmentCount += heldFragments;
        } finally {
            conn.writeUnlock();
            held.clear();
            heldFragments = 0;
        }
    }

    // Sends the last fragment, with any held before it
    @Override
    public void sendMessage() {
        if (heldFragments == 0) {
            sendFragment(true);
        } else {
            hold(((CDROutputObject) outputObject).sealFragment());
            if (heldFragments > 0) {
                sendHeld();
            }
        }
        releaseHeld();

        sentFullMessage = true;
    }

    /**
     * Close the BufferManagerWrite and do any outstanding cleanup: fragments still held, if the message was not
     * finished, are dropped without being written.
     */
    @Override
    public void close() {
        releaseHeld();
    }

    private void releaseHeld() {
        if (held != null) {
            orb.getByteBufferPool().releaseByteBuffer(held);
            held = null;
            heldFragments = 0;
        }
    }

    /**
     * Get CorbaContactInfoListIterator
     *
     * NOTE: Requires this.orb
     * 
     * @return the ContactInfoListIterator
     */
    protected ContactInfoListIterator getContactInfoListIterator() {
        return (ContactInfoListIterator) this.orb.getInvocationInfo().getContactInfoListIterator();
    }
}
