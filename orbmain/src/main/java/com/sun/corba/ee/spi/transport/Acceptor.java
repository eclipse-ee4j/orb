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

import com.sun.corba.ee.impl.encoding.CDROutputObject;
// REVISIT - impl/poa specific:
import com.sun.corba.ee.impl.oa.poa.Policies;
import com.sun.corba.ee.spi.ior.IORTemplate;
import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.protocol.MessageMediator;

import java.net.ServerSocket;
import java.net.Socket;

import org.glassfish.gmbal.Description ;
import org.glassfish.gmbal.ManagedAttribute ;
import org.glassfish.gmbal.ManagedObject ;

/**
 * @author Harold Carr
 */
@ManagedObject 
@Description( "An Acceptor represents an endpoint on which the ORB handles incoming connections" ) 
public abstract interface Acceptor
{
    @ManagedAttribute
    @Description( "The TCP port of this Acceptor" )  
    int getPort() ;

    @ManagedAttribute
    @Description( "The name of the IP interface for this Acceptor" ) 
    String getInterfaceName() ;

    @ManagedAttribute
    @Description( "The type of requests that this Acceptor handles" ) 
    String getType() ;

    @ManagedAttribute
    @Description( "True if this acceptor is used to lazily start the ORB" ) 
    boolean isLazy() ;

    void addToIORTemplate(IORTemplate iorTemplate, Policies policies,
                                 String codebase);
    String getMonitoringName();

    /**
     * Used to initialize an <code>Acceptor</code>.
     *
     * For example, initialization may mean to create a
     * {@link java.nio.channels.ServerSocketChannel ServerSocketChannel}.
     *
     * Note: this must be prepared to be be called multiple times.
     *
     * @return <code>true</code> when it performs initializatin
     * actions (typically the first call.
     */
    boolean initialize();

    /**
     * Used to determine if an <code>Acceptor</code> has been initialized.
     *
     * @return <code>true</code>. if the <code>Acceptor</code> has been
     * initialized.
     */
    boolean initialized();

    String getConnectionCacheType();

    void setConnectionCache(InboundConnectionCache connectionCache);

    InboundConnectionCache getConnectionCache();

    /**
     * Used to determine if the <code>Acceptor</code> should register
     * with a Selector to handle accept events.
     *
     * For example, this may be <em>false</em> in the case of Solaris Doors
     * which do not actively listen.
     *
     * @return <code>true</code> if the <code>Acceptor</code> should be
     * registered with a Selector.
     */
    boolean shouldRegisterAcceptEvent();

    /** Blocks until a new Socket is available on the acceptor's port.
     * @return the new socket
     */
    Socket getAcceptedSocket(); 

    /** Handle a newly accepted Socket.
     * @param channel socket to handle
     */
    void processSocket( Socket channel );

    /**
     * Close the <code>Acceptor</code>.
     */
    void close();

    EventHandler getEventHandler();

    CDROutputObject createOutputObject(ORB broker, MessageMediator messageMediator);
    
    ServerSocket getServerSocket();
}

// End of file.
