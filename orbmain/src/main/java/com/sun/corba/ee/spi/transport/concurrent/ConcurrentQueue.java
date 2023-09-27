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

/**
 * A class that provides a very simply unbounded queue. The main requirement here is that the class support constant
 * time (very fast) deletion of arbitrary elements. An instance of this class must be thread safe, either by locking or
 * by using a wait-free algorithm (preferred). The interface is made as simple is possible to make it easier to produce
 * a wait-free implementation.
 */
public interface ConcurrentQueue<V> {
    /**
     * A Handle provides the capability to delete an element of a ConcurrentQueue very quickly. Typically, the handle is
     * stored in the element, so that an element located from another data structure can be quickly deleted from a
     * ConcurrentQueue.
     */
    public interface Handle<V> {
        /**
         * Return the value that corresponds to this handle.
         * 
         * @return the value
         */
        V value();

        /**
         * Delete the element corresponding to this handle from the queue. Takes constant time. Returns true if the removal
         * succeeded, or false if it failed. which can occur if another thread has already called poll or remove on this
         * element.
         * 
         * @return if operation succeeded
         */
        boolean remove();

        /**
         * Time at which the element will expire
         * 
         * @return time in milliseconds since 1/1/70 when this item expires.
         */
        long expiration();
    }

    /**
     * Return the number of elements in the queue.
     * 
     * @return the number of elements
     */
    int size();

    /**
     * Add a new element to the tail of the queue. Returns a handle for the element in the queue.
     * 
     * @param arg element to add
     * @return handle for element
     */
    Handle<V> offer(V arg);

    /**
     * Return the handle for the head of the queue. The element is removed from the queue.
     * 
     * @return handle for head of queue
     */
    Handle<V> poll();

    /**
     * Return the handle for the head of the queue. The element is not removed from the queue.
     * 
     * @return handle for head of queue
     */
    Handle<V> peek();
}
