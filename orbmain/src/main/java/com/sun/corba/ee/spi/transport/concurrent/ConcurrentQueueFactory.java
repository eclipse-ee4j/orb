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

package com.sun.corba.ee.spi.transport.concurrent;

import com.sun.corba.ee.impl.transport.concurrent.ConcurrentQueueBlockingImpl;
import com.sun.corba.ee.impl.transport.concurrent.ConcurrentQueueNonBlockingImpl;
import com.sun.corba.ee.impl.transport.concurrent.ConcurrentQueueImpl;

/**
 * A factory class for creating instances of ConcurrentQueue. Note that a rather unusual syntax is needed for calling
 * these methods:
 *
 * ConcurrentQueueFactory.&lt;V&gt;makeXXXConcurrentQueue()
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
     * 
     * @param <V> type of queue
     * @param ttl time to live in milliseconds
     * @return ConcurrentQueue
     */
    public static <V> ConcurrentQueue makeNonBlockingConcurrentQueue(final long ttl) {
        return new ConcurrentQueueNonBlockingImpl<V>(ttl);
    }

    /**
     * Create a ConcurrentQueue whose implementation uses conventional locking to protect the data structure.
     * 
     * @param <V> type of queue
     * @param ttl time to live in milliseconds
     * @return ConcurrentQueue
     */
    public static <V> ConcurrentQueue makeBlockingConcurrentQueue(final long ttl) {
        return new ConcurrentQueueBlockingImpl<V>(ttl);
    }

    /**
     * Create a ConcurrentQueue that does no locking at all. For use in data structures that manage their own locking.
     * 
     * @param <V> type of queue
     * @param ttl time to live in milliseconds
     * @return ConcurrentQueue
     */
    public static <V> ConcurrentQueue makeConcurrentQueue(final long ttl) {
        return new ConcurrentQueueImpl<V>(ttl);
    }
}
