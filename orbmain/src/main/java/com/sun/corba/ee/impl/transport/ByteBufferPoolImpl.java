/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.transport;

import java.nio.ByteBuffer;
import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.transport.ByteBufferPool;
import com.sun.corba.ee.spi.logging.ORBUtilSystemException;
import com.sun.corba.ee.spi.misc.ORBConstants;

/**
 * @author Charlie Hunt
 */
public class ByteBufferPoolImpl implements ByteBufferPool {
    final private static ORBUtilSystemException wrapper = ORBUtilSystemException.self;

    private ByteBuffer byteBufferSlab;
    final private boolean useDirectBuffers;
    final private int byteBufferSlabSize;
    final private ORB orb;

    public ByteBufferPoolImpl(ORB orb) {
        this.orb = orb;
        this.useDirectBuffers = !this.orb.getORBData().disableDirectByteBufferUse();
        // If using DirectByteBuffers, setup a pool of DirectByteBuffers.
        // Otherwise, we'll just allocate buffers as we need them with no
        // pooling.
        if (this.useDirectBuffers) {
            this.byteBufferSlabSize = orb.getORBData().getPooledDirectByteBufferSlabSize();
            this.byteBufferSlab = allocateDirectByteBufferSlab();
        } else {
            // these have no meaning when not using direct buffers
            this.byteBufferSlabSize = -1;
            this.byteBufferSlab = null;
        }
    }

    /** Return a ByteBuffer of the requested size. */
    public ByteBuffer getByteBuffer(int size) {
        if (useDirectBuffers) {
            if (size > byteBufferSlabSize) {
                // return a HeapByteBuffer instead of allocating
                // a DirectByteBuffer greater the byteBufferSlabSize.
                return ByteBuffer.allocate(size);
            }
            synchronized (this) {
                if (byteBufferSlab == null || (byteBufferSlab.capacity() - byteBufferSlab.limit() < size)) {
                    byteBufferSlab = allocateDirectByteBufferSlab();
                }

                byteBufferSlab.limit(byteBufferSlab.position() + size);
                ByteBuffer view = byteBufferSlab.slice();
                byteBufferSlab.position(byteBufferSlab.limit());

                return view;
            }
        } else {
            return ByteBuffer.allocate(size);
        }
    }

    public void releaseByteBuffer(ByteBuffer buffer) {
        // nothing to do here other than help the garbage collector
        // Remove this, as it is not useful, and gets flagged by findbugs.
        // This method is important if we are using direct ByteBuffers.
        // buffer = null;
    }

    // REVISIT - Active ByteBuffers are currently not tracked.
    /**
     * Get a count of the outstanding allocated DirectByteBuffers. (Those allocated and have not been returned to the pool).
     */
    public int activeCount() {
        return 0;
    }

    /**
     * Return a new <code>ByteBuffer</code> of at least <code>minimumSize</code> and copy any bytes in the
     * <code>oldByteBuffer</code> starting at <code>oldByteBuffer.position()</code> up to <code>oldByteBuffer.limit()</code>
     * into the returned <code>ByteBuffer</code>.
     */
    public ByteBuffer reAllocate(ByteBuffer oldByteBuffer, int minimumSize) {
        int size = orb.getORBData().getReadByteBufferSize();
        while (size <= minimumSize) {
            size *= 2;
        }

        if (size > orb.getORBData().getMaxReadByteBufferSizeThreshold()) {
            if (minimumSize > orb.getORBData().getMaxReadByteBufferSizeThreshold()) {
                throw wrapper.maximumReadByteBufferSizeExceeded(orb.getORBData().getMaxReadByteBufferSizeThreshold(), size,
                        ORBConstants.MAX_READ_BYTE_BUFFER_SIZE_THRESHOLD_PROPERTY);
            } else {
                // minimumSize is greater than 1/2 of size, and less than or
                // equal to max read byte buffer size threshold. So, just
                // re-allocate a ByteBuffer of minimumSize.
                size = minimumSize;
            }
        }

        ByteBuffer newByteBuffer = getByteBuffer(size);

        // copy oldByteBuffer into newByteBuffer
        newByteBuffer.put(oldByteBuffer);

        return newByteBuffer;
    }

    /**
     * Allocate a DirectByteBuffer slab.
     */
    private ByteBuffer allocateDirectByteBufferSlab() {
        return ByteBuffer.allocateDirect(byteBufferSlabSize);
    }
}

// End of file.
