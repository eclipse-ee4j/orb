/*
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

import com.sun.corba.ee.spi.orb.ORB;

import com.sun.corba.ee.spi.logging.ORBUtilSystemException;

import java.nio.ByteBuffer;

/**
 * Defines the contract between the BufferManager and CDR stream on the writing side. The CDR stream calls back to the
 * BufferManagerWrite when it needs more room in the output buffer to continue. The BufferManager can then grow the
 * output buffer or use some kind of fragmentation technique.
 */
public abstract class BufferManagerWrite {
    protected ORB orb;
    protected static final ORBUtilSystemException wrapper = ORBUtilSystemException.self;

    BufferManagerWrite(ORB orb) {
        this.orb = orb;
    }

    /**
     * Has the stream sent out any fragments so far?
     * 
     * @return If any fragments have been sent
     */
    public abstract boolean sentFragment();

    /**
     * Has the entire message been sent? (Has sendMessage been called?)
     * 
     * @return If {@link #sendMessage()} has been called
     */
    public boolean sentFullMessage() {
        return sentFullMessage;
    }

    /**
     * Returns the correct buffer size for this type of buffer manager as set in the ORB.
     * 
     * @return buffer size
     */
    public abstract int getBufferSize();

    /*
     * Invoked when we run out of room to write. Must either expand the buffer or send it as a fragment and clear it.
     */
    protected abstract ByteBuffer overflow(ByteBuffer byteBuffer, int numBytesNeeded);

    /**
     * Returns true if this buffer manager fragments when an overflow occurs.
     * 
     * @return If this buffer manager fragments
     */
    public abstract boolean isFragmentOnOverflow();

    /**
     * Called after Stub._invoke (i.e., before complete message has been sent).
     *
     * IIOPOutputStream.writeTo called from IIOPOutputStream.invoke
     *
     * Case: overflow was never called (bbwi.buf contains complete message). Backpatch size field. If growing or collecting:
     * this.bufQ.put(bbwi). this.bufQ.iterate // However, see comment in getBufferQ this.connection.send(fragment) If
     * streaming: this.connection.send(bbwi).
     *
     * Case: overflow was called N times (bbwi.buf contains last buffer). If growing or collecting: this.bufQ.put(bbwi).
     * backpatch size field in first buffer. this.bufQ.iterate // However, see comment in getBufferQ
     * this.connection.send(fragment) If streaming: backpatch fragment size field in bbwi.buf. Set no more fragments bit.
     * this.connection.send(bbwi).
     */

    public abstract void sendMessage();

    /**
     * A reference to the connection level stream will be required when sending fragments.
     * 
     * @param outputObject GIOPObject to use.
     */
    public void setOutputObject(Object outputObject) {
        this.outputObject = outputObject;
    }

    /**
     * Close the BufferManagerWrite and do any outstanding cleanup.
     */
    abstract public void close();

    // XREVISIT - Currently a java.lang.Object during
    // the rip-int-generic transition. Should eventually
    // become a GIOPOutputObject.
    protected Object outputObject;

    protected boolean sentFullMessage = false;
}
