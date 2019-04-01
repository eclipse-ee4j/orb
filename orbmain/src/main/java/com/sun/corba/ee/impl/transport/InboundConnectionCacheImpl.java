/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.transport;

import java.util.ArrayList;
import java.util.Collection;

import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.transport.Acceptor;
import com.sun.corba.ee.spi.transport.Connection;
import com.sun.corba.ee.spi.transport.InboundConnectionCache;

import com.sun.corba.ee.spi.trace.Transport;

import org.glassfish.gmbal.ManagedObject;
import org.glassfish.gmbal.AMXMetadata;
import org.glassfish.gmbal.Description;

/**
 * @author Harold Carr
 */
@Transport
@ManagedObject
@Description("Cache of connections accepted by the ORB")
@AMXMetadata(type = "corba-inbound-connection-cache-mon", group = "monitoring")
public class InboundConnectionCacheImpl extends ConnectionCacheBase implements InboundConnectionCache {
    protected Collection<Connection> connectionCache;
    private InboundConnectionCacheProbeProvider pp = new InboundConnectionCacheProbeProvider();

    public InboundConnectionCacheImpl(ORB orb, Acceptor acceptor) {
        super(orb, acceptor.getConnectionCacheType(), ((Acceptor) acceptor).getMonitoringName());
        this.connectionCache = new ArrayList<Connection>();
    }

    public Connection get(Acceptor acceptor) {
        throw wrapper.methodShouldNotBeCalled();
    }

    @Transport
    public void put(Acceptor acceptor, Connection connection) {
        synchronized (backingStore()) {
            connectionCache.add(connection);
            connection.setConnectionCache(this);
            cacheStatisticsInfo();
            pp.connectionOpenedEvent(acceptor.toString(), connection.toString());
        }
    }

    @Transport
    public void remove(Connection connection) {
        synchronized (backingStore()) {
            connectionCache.remove(connection);
            cacheStatisticsInfo();
            pp.connectionClosedEvent(connection.toString());
        }
    }

    ////////////////////////////////////////////////////
    //
    // Implementation
    //

    public Collection values() {
        return connectionCache;
    }

    protected Object backingStore() {
        return connectionCache;
    }
}

// End of file.
