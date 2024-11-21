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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.sun.corba.ee.spi.transport.Selector;

import com.sun.corba.ee.spi.ior.IORTemplate;
import com.sun.corba.ee.spi.ior.ObjectAdapterId;
import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.transport.ByteBufferPool;
import com.sun.corba.ee.spi.transport.Acceptor;
import com.sun.corba.ee.spi.transport.TransportManager;
import com.sun.corba.ee.spi.transport.MessageTraceManager;

// REVISIT - impl/poa specific:
import com.sun.corba.ee.impl.oa.poa.Policies;

import com.sun.corba.ee.impl.protocol.giopmsgheaders.Message;
import com.sun.corba.ee.spi.trace.Transport;

import com.sun.corba.ee.spi.transport.ContactInfo;
import com.sun.corba.ee.spi.transport.InboundConnectionCache;
import com.sun.corba.ee.spi.transport.OutboundConnectionCache;

import org.glassfish.external.probe.provider.StatsProviderManager ;
import org.glassfish.external.probe.provider.PluginPoint ;
import org.glassfish.pfl.tf.spi.annotation.InfoMethod;

/**
 * @author Harold Carr
 */
// Note that no ObjectKeyName attribute is needed, because there is only
// one CorbaTransportManager per ORB.
@Transport
public class TransportManagerImpl
    implements
        TransportManager
{
    protected ORB orb;
    protected List<Acceptor> acceptors;
    protected final Map<String,OutboundConnectionCache> outboundConnectionCaches;
    protected final Map<String,InboundConnectionCache> inboundConnectionCaches;
    protected Selector selector;
    
    public TransportManagerImpl(ORB orb)
    {
        this.orb = orb;
        acceptors = new ArrayList<Acceptor>();
        outboundConnectionCaches = new HashMap<String,OutboundConnectionCache>();
        inboundConnectionCaches = new HashMap<String,InboundConnectionCache>();
        selector = new SelectorImpl(orb);
        if (orb.mom() != null) {
            orb.mom().register( orb, this ) ;
        }
    }

    public ByteBufferPool getByteBufferPool(int id)
    {
        throw new RuntimeException(); 
    }

    public OutboundConnectionCache getOutboundConnectionCache(
        ContactInfo contactInfo)
    {
        synchronized (contactInfo) {
            if (contactInfo.getConnectionCache() == null) {
                OutboundConnectionCache connectionCache = null;
                synchronized (outboundConnectionCaches) {
                    connectionCache = outboundConnectionCaches.get(
                        contactInfo.getConnectionCacheType());
                    if (connectionCache == null) {
                        // REVISIT: Would like to be able to configure
                        // the connection cache type used.
                        connectionCache = 
                            new OutboundConnectionCacheImpl(orb,
                                                                 contactInfo);

                        // We need to clean up the multi-cache support:
                        // this really only works with a single cache.
                        if (orb.mom() != null) {
                            orb.mom().register( this, connectionCache ) ;
                        }
                        StatsProviderManager.register( "orb", PluginPoint.SERVER,
                            "orb/transport/connectioncache/outbound", connectionCache ) ;

                        outboundConnectionCaches.put(
                            contactInfo.getConnectionCacheType(),
                            connectionCache);
                    }
                }
                contactInfo.setConnectionCache(connectionCache);
            }
            return contactInfo.getConnectionCache();
        }
    }

    public Collection<OutboundConnectionCache> getOutboundConnectionCaches()
    {
        return outboundConnectionCaches.values();
    }

    public Collection<InboundConnectionCache> getInboundConnectionCaches()
    {
        return inboundConnectionCaches.values();
    }

    public InboundConnectionCache getInboundConnectionCache(
        Acceptor acceptor)
    {
        synchronized (acceptor) {
            if (acceptor.getConnectionCache() == null) {
                InboundConnectionCache connectionCache = null;
                synchronized (inboundConnectionCaches) {
                    connectionCache = inboundConnectionCaches.get(
                            acceptor.getConnectionCacheType());
                    if (connectionCache == null) {
                        // REVISIT: Would like to be able to configure
                        // the connection cache type used.
                        connectionCache = 
                            new InboundConnectionCacheImpl(orb,
                                                                acceptor);
                        if (orb.mom() != null) {
                            orb.mom().register( this, connectionCache ) ;
                        }
                        StatsProviderManager.register( "orb", PluginPoint.SERVER,
                            "orb/transport/connectioncache/inbound", connectionCache ) ;

                        inboundConnectionCaches.put(
                            acceptor.getConnectionCacheType(),
                            connectionCache);
                    }
                }
                acceptor.setConnectionCache(connectionCache);
            }
            return acceptor.getConnectionCache();
        }
    }

    public Selector getSelector() {
        return selector ;
    }

    public Selector getSelector(int id) 
    {
        return selector;
    }

    @Transport
    public synchronized void registerAcceptor(Acceptor acceptor) {
        acceptors.add(acceptor);
    }

    @Transport
    public synchronized void unregisterAcceptor(Acceptor acceptor) {
        acceptors.remove(acceptor);
    }

    @Transport
    public void close()
    {
        for (OutboundConnectionCache cc : outboundConnectionCaches.values()) {
            StatsProviderManager.unregister( cc ) ;
            cc.close() ;
        }
        for (InboundConnectionCache cc : inboundConnectionCaches.values()) {
            StatsProviderManager.unregister( cc ) ;
            cc.close() ;
        }
        getSelector(0).close();
    }

    ////////////////////////////////////////////////////
    //
    // CorbaTransportManager
    //

    public Collection<Acceptor> getAcceptors() {
        return getAcceptors( null, null ) ;
    }

    @InfoMethod
    private void display( String msg ) { }

    @Transport
    public Collection<Acceptor> getAcceptors(String objectAdapterManagerId,
                                   ObjectAdapterId objectAdapterId)
    {
        // REVISIT - need to filter based on arguments.

        // REVISIT - initialization will be moved to OA.
        // Lazy initialization of acceptors.
        for (Acceptor acc : acceptors) {
            if (acc.initialize()) {
                display( "initializing acceptors" ) ;
                if (acc.shouldRegisterAcceptEvent()) {
                    orb.getTransportManager().getSelector(0)
                        .registerForEvent(acc.getEventHandler());
                }
            }
        }
        return acceptors;
    }

    // REVISIT - POA specific policies
    @Transport
    public void addToIORTemplate(IORTemplate iorTemplate, 
                                 Policies policies,
                                 String codebase,
                                 String objectAdapterManagerId,
                                 ObjectAdapterId objectAdapterId)
    {
        Iterator iterator = 
            getAcceptors(objectAdapterManagerId, objectAdapterId).iterator();
        while (iterator.hasNext()) {
            Acceptor acceptor = (Acceptor) iterator.next();
            acceptor.addToIORTemplate(iorTemplate, policies, codebase);
        }
    }

    private ThreadLocal currentMessageTraceManager =
        new ThreadLocal() {
            public Object initialValue() 
            {
                return new MessageTraceManagerImpl( ) ;
            }
        } ;

    public MessageTraceManager getMessageTraceManager() 
    {
        return (MessageTraceManager)(currentMessageTraceManager.get()) ;
    }
}

// End of file.
