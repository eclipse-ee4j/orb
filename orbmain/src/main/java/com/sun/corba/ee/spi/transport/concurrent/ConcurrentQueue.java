/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.spi.transport.concurrent ;

/** A class that provides a very simply unbounded queue.
 * The main requirement here is that the class support constant time (very fast)
 * deletion of arbitrary elements.  An instance of this class must be thread safe,
 * either by locking or by using a wait-free algorithm (preferred).
 * The interface is made as simple is possible to make it easier to produce
 * a wait-free implementation.
 */
public interface ConcurrentQueue<V> {
    /** A Handle provides the capability to delete an element of a ConcurrentQueue
     * very quickly.  Typically, the handle is stored in the element, so that an
     * element located from another data structure can be quickly deleted from 
     * a ConcurrentQueue.
     */
    public interface Handle<V> {
        /** Return the value that corresponds to this handle.
         */
        V value() ;

        /** Delete the element corresponding to this handle 
         * from the queue.  Takes constant time.  Returns
         * true if the removal succeeded, or false if it failed.
         * which can occur if another thread has already called
         * poll or remove on this element.
         */
        boolean remove() ;

        /** Time at which the element will expire 
         * 
         * @return time in milliseconds since 1/1/70 when this item expires.
         */
        long expiration() ;
    }

    /** Return the number of elements in the queue.
     */
    int size() ;

    /** Add a new element to the tail of the queue.
     * Returns a handle for the element in the queue.
     */
    Handle<V> offer( V arg ) ;

    /** Return the handle for the head of the queue.
     * The element is removed from the queue.
     */
    Handle<V> poll() ;

    /** Return the handle for the head of the queue.
     * The element is not removed from the queue.
     */
    Handle<V> peek() ;
} 
