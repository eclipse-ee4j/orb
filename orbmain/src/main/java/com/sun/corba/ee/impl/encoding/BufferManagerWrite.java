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
     */
    public abstract boolean sentFragment();

    /**
     * Has the entire message been sent? (Has sendMessage been called?)
     */
    public boolean sentFullMessage() {
        return sentFullMessage;
    }

    /**
     * Returns the correct buffer size for this type of buffer manager as set in the ORB.
     */
    public abstract int getBufferSize();

    /*
     * Invoked when we run out of room to write. Must either expand the buffer or send it as a fragment and clear it.
     */
    protected abstract ByteBuffer overflow(ByteBuffer byteBuffer, int numBytesNeeded);

    /**
     * Returns true if this buffer manager fragments when an overflow occurs.
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
