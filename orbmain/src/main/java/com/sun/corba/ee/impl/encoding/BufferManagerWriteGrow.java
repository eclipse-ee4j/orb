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

import com.sun.corba.ee.spi.transport.ByteBufferPool;
import com.sun.corba.ee.spi.transport.Connection;
import com.sun.corba.ee.spi.orb.ORB;

import java.nio.ByteBuffer;

public class BufferManagerWriteGrow extends BufferManagerWrite {
    BufferManagerWriteGrow(ORB orb) {
        super(orb);
    }

    public boolean sentFragment() {
        return false;
    }

    /**
     * Returns the correct buffer size for this type of buffer manager as set in the ORB.
     */
    public int getBufferSize() {
        return orb.getORBData().getGIOPBufferSize();
    }

    @Override
    protected ByteBuffer overflow(ByteBuffer byteBuffer, int numBytesNeeded) {
        int newLength = byteBuffer.limit() * 2;

        while (byteBuffer.position() + numBytesNeeded >= newLength)
            newLength = newLength * 2;

        ByteBufferPool byteBufferPool = orb.getByteBufferPool();
        ByteBuffer newBB = byteBufferPool.getByteBuffer(newLength);

        byteBuffer.flip();
        newBB.put(byteBuffer);

        byteBufferPool.releaseByteBuffer(byteBuffer);
        return newBB;
    }

    @Override
    public boolean isFragmentOnOverflow() {
        return false;
    }

    public void sendMessage() {
        Connection conn = ((CDROutputObject) outputObject).getMessageMediator().getConnection();

        conn.writeLock();

        try {

            conn.sendWithoutLock((CDROutputObject) outputObject);

            sentFullMessage = true;

        } finally {

            conn.writeUnlock();
        }
    }

    /**
     * Close the BufferManagerWrite and do any outstanding cleanup.
     *
     * No work to do for a BufferManagerWriteGrow.
     */
    public void close() {
    }

}
