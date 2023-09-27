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

package com.sun.corba.ee.impl.legacy.connection;

import java.util.Collection;
import java.util.Iterator;

import org.omg.CORBA.INTERNAL;
import org.omg.CORBA.CompletionStatus;

import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.legacy.connection.LegacyServerSocketEndPointInfo;
import com.sun.corba.ee.spi.legacy.connection.LegacyServerSocketManager;

import com.sun.corba.ee.impl.misc.ORBUtility;
import com.sun.corba.ee.spi.logging.ORBUtilSystemException;

public class LegacyServerSocketManagerImpl implements LegacyServerSocketManager {
    protected ORB orb;
    private static final ORBUtilSystemException wrapper = ORBUtilSystemException.self;

    public LegacyServerSocketManagerImpl(ORB orb) {
        this.orb = orb;
    }

    ////////////////////////////////////////////////////
    //
    // LegacyServerSocketManager
    //

    // Only used in ServerManagerImpl.
    public int legacyGetTransientServerPort(String type) {
        return legacyGetServerPort(type, false);
    }

    // Only used by POAPolicyMediatorBase.
    public synchronized int legacyGetPersistentServerPort(String socketType) {
        if (orb.getORBData().getServerIsORBActivated()) {
            // this server is activated by orbd
            return legacyGetServerPort(socketType, true);
        } else if (orb.getORBData().getPersistentPortInitialized()) {
            // this is a user-activated server
            return orb.getORBData().getPersistentServerPort();
        } else {
            throw wrapper.persistentServerportNotSet();
        }
    }

    // Only used by PI IORInfoImpl.
    public synchronized int legacyGetTransientOrPersistentServerPort(String socketType) {
        return legacyGetServerPort(socketType, orb.getORBData().getServerIsORBActivated());
    }

    // Used in RepositoryImpl, ServerManagerImpl, POAImpl,
    // POAPolicyMediatorBase, TOAImpl.
    // To get either default or bootnaming endpoint.
    public synchronized LegacyServerSocketEndPointInfo legacyGetEndpoint(String name) {
        Iterator iterator = getAcceptorIterator();
        while (iterator.hasNext()) {
            LegacyServerSocketEndPointInfo endPoint = cast(iterator.next());
            if (endPoint != null && name.equals(endPoint.getName())) {
                return endPoint;
            }
        }
        throw new INTERNAL("No acceptor for: " + name);
    }

    // Check to see if the given port is equal to any of the ORB Server Ports.
    // Used in IIOPProfileImpl, ORBImpl.
    public boolean legacyIsLocalServerPort(int port) {
        // If port is 0 (which signifies in CSIv2 that clear text
        // communication is not allowed), we must return true, because
        // this check is not meaningful.
        if (port == 0) {
            return true;
        }

        Iterator iterator = getAcceptorIterator();
        while (iterator.hasNext()) {
            LegacyServerSocketEndPointInfo endPoint = cast(iterator.next());
            if (endPoint != null && endPoint.getPort() == port) {
                return true;
            }
        }
        return false;
    }

    ////////////////////////////////////////////////////
    //
    // Implementation.
    //

    private int legacyGetServerPort(String socketType, boolean isPersistent) {
        Iterator endpoints = getAcceptorIterator();
        while (endpoints.hasNext()) {
            LegacyServerSocketEndPointInfo ep = cast(endpoints.next());
            if (ep != null && ep.getType().equals(socketType)) {
                if (isPersistent) {
                    return ep.getLocatorPort();
                } else {
                    return ep.getPort();
                }
            }
        }
        return -1;
    }

    private Iterator getAcceptorIterator() {
        Collection acceptors = orb.getCorbaTransportManager().getAcceptors(null, null);
        if (acceptors != null) {
            return acceptors.iterator();
        }

        throw wrapper.getServerPortCalledBeforeEndpointsInitialized();
    }

    private LegacyServerSocketEndPointInfo cast(Object o) {
        if (o instanceof LegacyServerSocketEndPointInfo) {
            return (LegacyServerSocketEndPointInfo) o;
        }
        return null;
    }

    protected void dprint(String msg) {
        ORBUtility.dprint("LegacyServerSocketManagerImpl", msg);
    }
}

// End of file.
