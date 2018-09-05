/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.spi.transport;

import java.nio.ByteBuffer;

/**
 * @author Charlie Hunt
 */
public interface ByteBufferPool
{
    public ByteBuffer getByteBuffer(int theSize);
    public void releaseByteBuffer(ByteBuffer thebb);
    public int activeCount();
    /**
     * Return a new <code>ByteBuffer</code> of at least <code>minimumSize</code>
     * and copy any bytes in the <code>oldByteBuffer</code> starting at
     * <code>oldByteBuffer.position()</code> up to <code>oldByteBuffer.limit()</code>
     * into the returned <code>ByteBuffer</code>.
     */
    public ByteBuffer reAllocate(ByteBuffer oldByteBuffer, int minimumSize);
}

// End of file.
