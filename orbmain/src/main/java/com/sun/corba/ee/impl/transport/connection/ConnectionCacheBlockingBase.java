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

package com.sun.corba.ee.impl.transport.connection;

import com.sun.corba.ee.spi.transport.connection.Connection;

import com.sun.corba.ee.spi.transport.concurrent.ConcurrentQueueFactory;

abstract class ConnectionCacheBlockingBase<C extends Connection> extends ConnectionCacheBase<C> {

    protected int totalBusy; // Number of busy connections
    protected int totalIdle; // Number of idle connections

    ConnectionCacheBlockingBase(String cacheType, int highWaterMark, int numberToReclaim, long ttl) {

        super(cacheType, highWaterMark, numberToReclaim);

        this.totalBusy = 0;
        this.totalIdle = 0;

        this.reclaimableConnections = ConcurrentQueueFactory.<C>makeConcurrentQueue(ttl);
    }

    public synchronized long numberOfConnections() {
        return totalIdle + totalBusy;
    }

    public synchronized long numberOfIdleConnections() {
        return totalIdle;
    }

    public synchronized long numberOfBusyConnections() {
        return totalBusy;
    }

    public synchronized long numberOfReclaimableConnections() {
        return reclaimableConnections.size();
    }
}
