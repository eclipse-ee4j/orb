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


import com.sun.corba.ee.spi.transport.connection.Connection ;

import com.sun.corba.ee.spi.transport.concurrent.ConcurrentQueueFactory;

abstract class ConnectionCacheBlockingBase<C extends Connection> 
    extends ConnectionCacheBase<C> {

    protected int totalBusy ;   // Number of busy connections
    protected int totalIdle ;   // Number of idle connections

    ConnectionCacheBlockingBase( String cacheType, int highWaterMark,
        int numberToReclaim, long ttl ) {

        super( cacheType, highWaterMark, numberToReclaim) ;

        this.totalBusy = 0 ;
        this.totalIdle = 0 ;

        this.reclaimableConnections = 
            ConcurrentQueueFactory.<C>makeConcurrentQueue( ttl ) ;
    }

    public synchronized long numberOfConnections() {
        return totalIdle + totalBusy ;
    }

    public synchronized long numberOfIdleConnections() {
        return totalIdle ;
    }

    public synchronized long numberOfBusyConnections() {
        return totalBusy ;
    }

    public synchronized long numberOfReclaimableConnections() {
        return reclaimableConnections.size() ;
    }
}
 
