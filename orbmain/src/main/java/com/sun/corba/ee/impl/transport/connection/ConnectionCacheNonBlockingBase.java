/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.transport.connection;


import java.util.concurrent.atomic.AtomicInteger ;

import com.sun.corba.ee.spi.transport.connection.Connection ;

import com.sun.corba.ee.spi.transport.concurrent.ConcurrentQueueFactory ;

abstract class ConnectionCacheNonBlockingBase<C extends Connection> 
    extends ConnectionCacheBase<C> {

    protected final AtomicInteger totalBusy ;   // Number of busy connections
    protected final AtomicInteger totalIdle ;   // Number of idle connections

    ConnectionCacheNonBlockingBase( String cacheType, int highWaterMark,
        int numberToReclaim, long ttl ) {

        super( cacheType, highWaterMark, numberToReclaim) ;

        this.totalBusy = new AtomicInteger() ;
        this.totalIdle = new AtomicInteger() ;

        this.reclaimableConnections = 
            // XXX make this the non-blocking version once we write it.
            ConcurrentQueueFactory.<C>makeBlockingConcurrentQueue( ttl ) ;
    }

    public long numberOfConnections() {
        return totalIdle.get() + totalBusy.get() ;
    }

    public long numberOfIdleConnections() {
        return totalIdle.get() ;
    }

    public long numberOfBusyConnections() {
        return totalBusy.get() ;
    }

    public long numberOfReclaimableConnections() {
        return reclaimableConnections.size() ;
    }
}
 
