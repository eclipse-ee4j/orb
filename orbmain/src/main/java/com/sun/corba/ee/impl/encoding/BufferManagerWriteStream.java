/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.encoding;

import com.sun.corba.ee.impl.protocol.giopmsgheaders.FragmentMessage;
import com.sun.corba.ee.impl.protocol.giopmsgheaders.Message;
import com.sun.corba.ee.impl.protocol.giopmsgheaders.MessageBase;
import com.sun.corba.ee.impl.protocol.giopmsgheaders.ReplyMessage;
import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.transport.Connection;
import com.sun.corba.ee.spi.transport.ContactInfoListIterator;
import org.glassfish.pfl.basic.reflection.Bridge;
import org.omg.CORBA.SystemException;
import org.omg.CORBA.portable.RemarshalException;

import java.nio.ByteBuffer;
import java.util.EmptyStackException;

/**
 * Streaming buffer manager.
 */
public class BufferManagerWriteStream extends BufferManagerWrite {
    private int fragmentCount = 0;

    BufferManagerWriteStream(ORB orb) {
        super(orb);
    }

    public boolean sentFragment() {
        return fragmentCount > 0;
    }

    /**
     * Returns the correct buffer size for this type of buffer manager as set in the ORB.
     */
    public int getBufferSize() {
        return orb.getORBData().getGIOPFragmentSize();
    }

    protected ByteBuffer overflow(ByteBuffer byteBuffer, int numBytesNeeded) {
        // Set the fragment's moreFragments field to true
        MessageBase.setFlag(byteBuffer, Message.MORE_FRAGMENTS_BIT);

        try {
            sendFragment(false);
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

    // Sends the last fragment
    public void sendMessage() {
        sendFragment(true);

        sentFullMessage = true;
    }

    /**
     * Close the BufferManagerWrite and do any outstanding cleanup.
     *
     * No work to do for a BufferManagerWriteStream
     */
    public void close() {
    };

    /**
     * Get CorbaContactInfoListIterator
     *
     * NOTE: Requires this.orb
     */
    protected ContactInfoListIterator getContactInfoListIterator() {
        return (ContactInfoListIterator) this.orb.getInvocationInfo().getContactInfoListIterator();
    }
}
