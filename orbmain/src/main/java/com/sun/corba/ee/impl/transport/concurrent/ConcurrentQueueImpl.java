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

package com.sun.corba.ee.impl.transport.concurrent ;

import com.sun.corba.ee.spi.transport.concurrent.ConcurrentQueue ;

public class ConcurrentQueueImpl<V> implements ConcurrentQueue<V> {
    // This implementation of ConcurrentQueue is unsynchronized, for use in
    // other implementations that manage concurrency with locks.
    //
    // Structure: Head points to a node containing a null value, which is a special marker.
    // head.next is the first element, head.prev is the last.  The queue is empty if
    // head.next == head.prev == head.
    final Entry<V> head = new Entry<V>( null, 0 ) ;
    int count = 0 ;
    private long ttl ;

    public ConcurrentQueueImpl( long ttl ) {
        head.next = head ;
        head.prev = head ;
        this.ttl = ttl ;
    }

    private final class Entry<V> {
        Entry<V> next = null ;
        Entry<V> prev = null ;
        private HandleImpl<V> handle ;
        private long expiration ;

        Entry( V value, long expiration ) {
            handle = new HandleImpl<V>( this, value, expiration ) ;
            this.expiration = expiration ;
        }

        HandleImpl<V> handle() {
            return handle ;
        }
    }

    private final class HandleImpl<V> implements Handle<V> {
        private Entry<V> entry ;
        private final V value ;
        private boolean valid ;
        private long expiration ;

        HandleImpl( Entry<V> entry, V value, long expiration ) {
            this.entry = entry ;
            this.value = value ;
            this.valid = true ;
            this.expiration = expiration ;
        }

        Entry<V> entry() {
            return entry ;
        }

        public V value() {
            return value ;
        }

        /** Delete the element corresponding to this handle 
         * from the queue.  Takes constant time.
         */
        public boolean remove() {
            if (!valid) {
                return false ;
            }

            valid = false ;

            entry.next.prev = entry.prev ;
            entry.prev.next = entry.next ;
            count-- ;

            entry.prev = null ;
            entry.next = null ;
            entry.handle = null ;
            entry = null ;
            valid = false ;
            return true ;
        }

        public long expiration() {
            return expiration ;
        }
    }

    public int size() {
        return count ;
    }

    /** Add a new element to the tail of the queue.
     * Returns a handle for the element in the queue.
     */
    public Handle<V> offer( V arg ) {
        if (arg == null)
            throw new IllegalArgumentException( "Argument cannot be null" ) ;

        Entry<V> entry = new Entry<V>( arg, System.currentTimeMillis() + ttl ) ;
        
        entry.next = head ;
        entry.prev = head.prev ;
        head.prev.next = entry ;
        head.prev = entry ;
        count++ ;

        return entry.handle() ;
    }

    /** Return an element from the head of the queue.
     * The element is removed from the queue.
     */
    public Handle<V> poll() {
        Entry<V> first = null ;

        first = head.next ;
        if (first == head) {
            return null ;
        }

        final Handle<V> result = first.handle() ;
        result.remove() ;
        return result ;
    }

    public Handle<V> peek() {
        Entry<V> first = head.next ;
        if (first == head) 
            return null ;
        else
            return first.handle() ;
    }
} 
