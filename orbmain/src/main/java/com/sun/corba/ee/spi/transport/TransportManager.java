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

package com.sun.corba.ee.spi.transport;

//
// REVISIT - impl/poa specific:
import com.sun.corba.ee.impl.oa.poa.Policies;
import com.sun.corba.ee.spi.ior.IORTemplate;
import com.sun.corba.ee.spi.ior.ObjectAdapterId;

import java.util.Collection;

import org.glassfish.gmbal.AMXMetadata ;
import org.glassfish.gmbal.Description ;
import org.glassfish.gmbal.ManagedAttribute ;
import org.glassfish.gmbal.ManagedObject ;

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
     * 
     * @return MessageTraceManager for the current thread
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
