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

import com.sun.corba.ee.spi.transport.Connection;

import com.sun.corba.ee.spi.ior.IOR ;
import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.transport.ContactInfoList;
import com.sun.corba.ee.spi.transport.TransportManager;
import com.sun.corba.ee.spi.transport.SocketInfo;

import com.sun.corba.ee.impl.misc.ORBUtility;
import com.sun.corba.ee.impl.transport.ContactInfoBase;

/**
 * @author Harold Carr
 */
public class ContactInfoImpl
    extends ContactInfoBase
    implements SocketInfo
{
    protected boolean isHashCodeCached = false;
    protected int cachedHashCode;

    protected String socketType;
    protected String hostname;
    protected int    port;

    // XREVISIT 
    // See SocketOrChannelAcceptorImpl.createMessageMediator
    // See SocketFactoryContactInfoImpl.constructor()
    // See SocketOrChannelContactInfoImpl.constructor()
    protected ContactInfoImpl()
    {
    }

    protected ContactInfoImpl(
        ORB orb,
        ContactInfoList contactInfoList)
    {
        this.orb = orb;
        this.contactInfoList = contactInfoList;
    }

    public ContactInfoImpl(
        ORB orb,
        ContactInfoList contactInfoList,
        String socketType,
        String hostname,
        int port)
    {
        this(orb, contactInfoList);
        this.socketType = socketType;
        this.hostname = hostname;
        this.port     = port;
    }

    // XREVISIT
    public ContactInfoImpl(
        ORB orb,
        ContactInfoList contactInfoList,
        IOR effectiveTargetIOR,
        short addressingDisposition,
        String socketType,
        String hostname,
        int port)
    {
        this(orb, contactInfoList, socketType, hostname, port);
        this.effectiveTargetIOR = effectiveTargetIOR;
        this.addressingDisposition = addressingDisposition;
    }

    public boolean isConnectionBased()
    {
        return true;
    }

    public boolean shouldCacheConnection()
    {
        return true;
    }

    public String getConnectionCacheType()
    {
        return TransportManager.SOCKET_OR_CHANNEL_CONNECTION_CACHE;
    }

    public Connection createConnection()
    {
        Connection connection =
            new ConnectionImpl(orb, this,
                                              socketType, hostname, port);
        return connection;
    }

    ////////////////////////////////////////////////////
    //
    // spi.transport.CorbaContactInfo
    //

    public String getMonitoringName()
    {
        return "SocketConnections";
    }

    public String getType()
    {
        return socketType;
    }

    public String getHost()
    {
        return hostname;
    }

    public int getPort()
    {
        return port;
    }

    ////////////////////////////////////////////////////
    //
    // java.lang.Object
    //

    // NOTE: hashCode should only check type/host/port, otherwise
    // RMI-IIOP Failover will break.  See IIOPPrimaryToContactInfoImpl.java
    // in the app server or in the Corba unit tests.
    
    @Override
    public int hashCode() 
    {
        if (! isHashCodeCached) {
            cachedHashCode = socketType.hashCode() ^ hostname.hashCode() ^ port;
            isHashCodeCached = true;
        }
        return cachedHashCode;
    }

    // NOTE: equals should only check type/host/port, otherwise
    // RMI-IIOP Failover will break.  See IIOPPrimaryToContactInfoImpl.java
    // in the app server or in the Corba unit tests.
    
    @Override
    public boolean equals(Object obj) 
    {
        if (obj == null) {
            return false;
        } else if (!(obj instanceof ContactInfoImpl)) {
            return false;
        }

        ContactInfoImpl other =
            (ContactInfoImpl) obj;

        if (port != other.port) {
            return false;
        }
        if (!hostname.equals(other.hostname)) {
            return false;
        }
        if (socketType == null) {
            if (other.socketType != null) {
                return false;
            }
        } else if (!socketType.equals(other.socketType)) {
            return false;
        }
        return true;
    }

    public String toString()
    {
        return
            "SocketOrChannelContactInfoImpl[" 
            + socketType + " "
            + hostname + " "
            + port
            + "]";
    }

    ////////////////////////////////////////////////////
    //
    // Implementation
    //

    protected void dprint(String msg) 
    {
        ORBUtility.dprint("SocketOrChannelContactInfoImpl", msg);
    }
}

// End of file.
