/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.activation ;

import java.util.Collection ;
import java.util.Iterator ;

import org.omg.CORBA.CompletionStatus ;

import com.sun.corba.ee.spi.activation.Locator ;
import com.sun.corba.ee.spi.activation.Activator ;
import com.sun.corba.ee.spi.activation.LocatorHelper ;
import com.sun.corba.ee.spi.activation.ActivatorHelper ;
import com.sun.corba.ee.spi.activation.EndPointInfo ;

import com.sun.corba.ee.spi.orb.ORBData ;
import com.sun.corba.ee.spi.orb.ORB ;

import com.sun.corba.ee.spi.transport.SocketInfo;

import com.sun.corba.ee.spi.logging.ORBUtilSystemException ;

import com.sun.corba.ee.impl.orb.ORBConfiguratorImpl;
import com.sun.corba.ee.spi.legacy.connection.LegacyServerSocketEndPointInfo;

import com.sun.corba.ee.spi.misc.ORBConstants ;

public class ORBConfiguratorPersistentImpl extends ORBConfiguratorImpl {
    private ORBUtilSystemException wrapper = ORBUtilSystemException.self ;

    @Override
    protected void persistentServerInitialization( ORB orb )
    {
        ORBData data = orb.getORBData() ;

        // determine the ORBD port so that persistent objrefs can be
        // created.
        if (data.getServerIsORBActivated()) {
            try {
                Locator locator = LocatorHelper.narrow(
                    orb.resolve_initial_references(
                        ORBConstants.SERVER_LOCATOR_NAME )) ;
                Activator activator = ActivatorHelper.narrow(
                    orb.resolve_initial_references(
                        ORBConstants.SERVER_ACTIVATOR_NAME )) ;
                Collection serverEndpoints =
                    orb.getCorbaTransportManager().getAcceptors(null, null);
                EndPointInfo[] endpointList =
                    new EndPointInfo[serverEndpoints.size()];
                Iterator iterator = serverEndpoints.iterator();
                int i = 0 ;
                while (iterator.hasNext()) {
                    Object n = iterator.next();
                    if (! (n instanceof LegacyServerSocketEndPointInfo)) {
                        continue;
                    }
                    LegacyServerSocketEndPointInfo ep =
                        (LegacyServerSocketEndPointInfo) n;
                    // REVISIT - use exception instead of -1.
                    int port = locator.getEndpoint(ep.getType());
                    if (port == -1) {
                        port = locator.getEndpoint(SocketInfo.IIOP_CLEAR_TEXT);
                        if (port == -1) {
                            throw new Exception(
                                "ORBD must support IIOP_CLEAR_TEXT");
                        }
                    }

                    ep.setLocatorPort(port);

                    endpointList[i++] =
                        new EndPointInfo(ep.getType(), ep.getPort());
                }

                activator.registerEndpoints(
                    data.getPersistentServerId(), data.getORBId(),
                        endpointList);
            } catch (Exception ex) {
                throw wrapper.persistentServerInitError( ex ) ;
            }
        }
    }
}
