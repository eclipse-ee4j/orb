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

package com.sun.corba.ee.impl.transport;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.transport.ContactInfo;

import com.sun.corba.ee.spi.trace.Transport;
import com.sun.corba.ee.spi.transport.Connection;
import com.sun.corba.ee.spi.transport.OutboundConnectionCache;

import org.glassfish.gmbal.ManagedObject ;
import org.glassfish.gmbal.AMXMetadata ;
import org.glassfish.gmbal.Description ;

/**
 * @author Harold Carr
 */
@Transport
@ManagedObject
@Description( "Cache of connections originated by the ORB" ) 
@AMXMetadata( type="corba-outbound-connection-cache-mon", group="monitoring" )
public class OutboundConnectionCacheImpl
    extends
        ConnectionCacheBase
    implements
        OutboundConnectionCache
{
    protected Map<ContactInfo, Connection> connectionCache;
    private OutboundConnectionCacheProbeProvider pp =
        new OutboundConnectionCacheProbeProvider() ;

    public OutboundConnectionCacheImpl(ORB orb, ContactInfo contactInfo)
    {
        super(orb, contactInfo.getConnectionCacheType(),
              ((ContactInfo)contactInfo).getMonitoringName());
        this.connectionCache = new HashMap<ContactInfo,Connection>();
    }

    @Transport
    public Connection get(ContactInfo contactInfo)
    {
        synchronized (backingStore()) {
            cacheStatisticsInfo();
            return connectionCache.get(contactInfo);
        }
    }
    
    @Transport
    public void put(ContactInfo contactInfo, Connection connection)
    {
        synchronized (backingStore()) {
            connectionCache.put(contactInfo, connection);
            connection.setConnectionCache(this);
            pp.connectionOpenedEvent( contactInfo.toString(), connection.toString() ) ;
            cacheStatisticsInfo();
        }
    }

    @Transport
    public void remove(ContactInfo contactInfo)
    {
        synchronized (backingStore()) {
            if (contactInfo != null) {
                Connection connection = connectionCache.remove(contactInfo);
                pp.connectionClosedEvent( contactInfo.toString(), connection.toString() ) ;
            }
            cacheStatisticsInfo();
        }
    }

    ////////////////////////////////////////////////////
    //
    // Implementation
    //

    public Collection values()
    {
        return connectionCache.values();
    }

    protected Object backingStore()
    {
        return connectionCache;
    }

    @Override
    public String toString()
    {
        return "CorbaOutboundConnectionCacheImpl["
            + connectionCache
            + "]";
    }
}

// End of file.
