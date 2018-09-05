/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

//
// Created       : 2003 Sep 28 (Sun) 09:06:43 by Harold Carr.
// Last Modified : 2003 Sep 29 (Mon) 06:53:22 by Harold Carr.
//

package corba.connections;

import java.util.Collection;
import java.util.Iterator;

import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.transport.Acceptor;
import com.sun.corba.ee.spi.transport.TransportManager;
import com.sun.corba.ee.spi.transport.ConnectionCache;
import com.sun.corba.ee.spi.presentation.rmi.StubAdapter ;

import corba.hcks.U;

import org.glassfish.gmbal.GmbalException ;
import org.glassfish.gmbal.AMXClient ;
import org.glassfish.gmbal.ManagedObjectManager ;

public class ConnectionStatistics
{
    private final ORB orb ;
    private final TransportManager ctm ;

    public ConnectionStatistics( ORB orb ) {
        this.orb = orb ;
        this.ctm = orb.getCorbaTransportManager() ;
    }

    private void handleAttribute( StringBuffer result, AMXClient amxc,
        String attributeName ) {

        try {
            Object value = amxc.getAttribute( attributeName ) ;

            pac(result, attributeName + " " + value );
        } catch (GmbalException exc) {
            pac(result, "--------------------------------------------------");
            pac(result, "ERROR: Missing: " + attributeName ) ;
            pac(result, "--------------------------------------------------");
            System.exit(1);
        } 
    }

    private void handleConnectionCache( StringBuffer result,
        ConnectionCache connectionCache ) {

        pac(result, connectionCache.getMonitoringName());

        AMXClient amxc = orb.mom().getAMXClient( connectionCache ) ;
        if (amxc == null) {
            pac(result, "--------------------------------------------------");
            pac(result, "ERROR: Missing: " + connectionCache.getMonitoringName());
            pac(result, "--------------------------------------------------");
            System.exit(1);
        }

        handleAttribute( result, amxc, "totalconnections" ) ;
        handleAttribute( result, amxc, "connectionsidle" ) ;
        handleAttribute( result, amxc, "connectionsbusy" ) ;
    }

    public String outbound(String msg, ORB orb) {
        ManagedObjectManager mom = orb.mom() ;

        StringBuffer result = new StringBuffer("");
        pac(result, "==================================================");
        pac(result, msg + " OUTBOUND:");

        for (ConnectionCache cache : ctm.getOutboundConnectionCaches() ) {
            handleConnectionCache( result, cache ) ;
        }

        return result.toString();
    }

    public String inbound(String msg, ORB orb) {
        ManagedObjectManager mom = orb.mom() ;

        StringBuffer result = new StringBuffer("");
        pac(result, "==================================================");
        pac(result, msg + " INBOUND:");

        for (ConnectionCache cache : ctm.getInboundConnectionCaches() ) {
            handleConnectionCache( result, cache ) ;
        }

        return result.toString();
    }

    // Print And Collect
    private void pac(StringBuffer result, String append) {
        U.sop(append);
        result.append(append).append('\n');
    }
}

// End of file.
