/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.spi.transport.concurrent;

import com.sun.corba.ee.impl.transport.concurrent.ConcurrentQueueBlockingImpl;
import com.sun.corba.ee.impl.transport.concurrent.ConcurrentQueueNonBlockingImpl;
import com.sun.corba.ee.impl.transport.concurrent.ConcurrentQueueImpl;

/**
 * A factory class for creating instances of ConcurrentQueue. Note that a rather unusual syntax is needed for calling
 * these methods:
 *
 * ConcurrentQueueFactory.<V>makeXXXConcurrentQueue()
 *
 * This is required because the type variable V is not used in the parameters of the factory method, so the correct type
 * cannot be inferred by the compiler.
 */
public final class ConcurrentQueueFactory {
    private ConcurrentQueueFactory() {
    }

    /**
     * Create a ConcurrentQueue whose implementation never blocks. Currently not fully implemented: the NonBlocking and
     * Blocking impls are basically the same.
     */
    public static <V> ConcurrentQueue makeNonBlockingConcurrentQueue(final long ttl) {
        return new ConcurrentQueueNonBlockingImpl<V>(ttl);
    }

    /**
     * Create a ConcurrentQueue whose implementation uses conventional locking to protect the data structure.
     */
    public static <V> ConcurrentQueue makeBlockingConcurrentQueue(final long ttl) {
        return new ConcurrentQueueBlockingImpl<V>(ttl);
    }

    /**
     * Create a ConcurrentQueue that does no locking at all. For use in data structures that manage their own locking.
     */
    public static <V> ConcurrentQueue makeConcurrentQueue(final long ttl) {
        return new ConcurrentQueueImpl<V>(ttl);
    }
}
