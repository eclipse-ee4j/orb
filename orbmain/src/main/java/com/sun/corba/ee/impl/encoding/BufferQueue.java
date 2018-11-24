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

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.NoSuchElementException;

/**
 * Simple unsynchronized queue implementation for ByteBuffer.
 */
public class BufferQueue {
    private LinkedList<ByteBuffer> list = new LinkedList<ByteBuffer>();

    public void enqueue(ByteBuffer item) {
        list.addLast(item);
    }

    public ByteBuffer dequeue() throws NoSuchElementException {
        return list.removeFirst();
    }

    public int size() {
        return list.size();
    }

    // Adds the given ByteBuffer to the front of the queue.
    public void push(ByteBuffer item) {
        list.addFirst(item);
    }

    public void clear() {
        list.clear();
    }
}
