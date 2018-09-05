/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.spi.transport;

import java.util.Collection;

import com.sun.corba.ee.spi.ior.IORTemplate;
import com.sun.corba.ee.spi.ior.ObjectAdapterId;

import com.sun.corba.ee.impl.protocol.giopmsgheaders.Message ;
//
// REVISIT - impl/poa specific:
import com.sun.corba.ee.impl.oa.poa.Policies;

import com.sun.corba.ee.spi.orb.ORB;
import org.glassfish.gmbal.Description ;
import org.glassfish.gmbal.ManagedAttribute ;
import org.glassfish.gmbal.ManagedObject ;
import org.glassfish.gmbal.AMXMetadata ;

/**
 * @author Harold Carr
 */
@ManagedObject
@Description( "The Transport Manager for the ORB" )
@AMXMetadata( isSingleton=true ) 
public interface TransportManager {

    public ByteBufferPool getByteBufferPool(int id);

    @ManagedAttribute
    @Description( "The Selector, which listens for all I/O events" )
    public Selector getSelector();

    public Selector getSelector(int id);

    public void close();

    public static final String SOCKET_OR_CHANNEL_CONNECTION_CACHE =
        "SocketOrChannelConnectionCache";

    @ManagedAttribute
    @Description( "List of all Acceptors in this ORB" ) 
    public Collection<Acceptor> getAcceptors() ;

    public Collection<Acceptor> getAcceptors(String objectAdapterManagerId,
                                   ObjectAdapterId objectAdapterId);

    // REVISIT - POA specific policies
    public void addToIORTemplate(IORTemplate iorTemplate, 
                                 Policies policies,
                                 String codebase,
                                 String objectAdapterManagerId,
                                 ObjectAdapterId objectAdapterId);

    // Methods for GIOP debugging support

    /** Return a MessageTraceManager for the current thread.
     * Each thread that calls getMessageTraceManager gets its own
     * independent copy.
     */
    MessageTraceManager getMessageTraceManager() ;

    public OutboundConnectionCache getOutboundConnectionCache(
        ContactInfo contactInfo);

    @ManagedAttribute
    @Description( "Outbound Connection Cache (client initiated connections)" )
    public Collection<OutboundConnectionCache> getOutboundConnectionCaches();

    public InboundConnectionCache getInboundConnectionCache(Acceptor acceptor);

    // Only used for MBeans
    @ManagedAttribute
    @Description( "Inbound Connection Cache (server accepted connections)" )
    public Collection<InboundConnectionCache> getInboundConnectionCaches();

    public void registerAcceptor(Acceptor acceptor);

    public void unregisterAcceptor(Acceptor acceptor);

}
    
// End of file.
