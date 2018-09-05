/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package pi.serverrequestinfo;

import org.omg.CORBA.*;
import org.omg.CORBA.ORBPackage.*;
import org.omg.CosNaming.*;
import org.omg.PortableServer.*;
import org.omg.PortableServer.POAPackage.*;
import org.omg.PortableServer.ServantLocatorPackage.*;
import org.omg.PortableInterceptor.*;
import com.sun.corba.ee.impl.interceptors.*;
import corba.framework.*;
import com.sun.corba.ee.spi.misc.ORBConstants;

import java.util.*;
import java.io.*;

import ServerRequestInfo.*;

/**
 * Tests DSI POA Local invocations (with a co-located orb)
 */
public class DSIPOALocalServer 
    extends POAServer
{
    // Object to synchronize on to wait for server to start:
    private java.lang.Object syncObject;
    
    public DSIPOALocalServer() {
        // True means this is a DSI server.
        super( true );
    }

    public static void main(String args[]) {
        final String[] arguments = args;
        try {
            final DSIPOALocalServer server = new DSIPOALocalServer();

            TestInitializer.out = System.out;
            server.out = System.out;
            server.err = System.err;

            server.out.println( "===================================" );
            server.out.println( "Creating ORB for DSI POA Local test" );
            server.out.println( "===================================" );

            // For this test, start both the client and the server using
            // the same ORB.
            System.out.println( "+ Creating ORB for client and server..." );
            Properties props = new Properties();
            server.createORB( args, props );

            System.out.println( "+ Starting Server..." );
            server.syncObject = new java.lang.Object();
            new Thread() {
                public void run() {
                    try {
                        server.run(
                            System.getProperties(),
                            arguments, System.out,
                            System.err, null );
                    }
                    catch( Exception e ) {
                        System.err.println( "SERVER CRASHED:" );
                        e.printStackTrace( System.err );
                        System.exit( 1 );
                    }
                }
            }.start();

            // Wait for server to start...
            synchronized( server.syncObject ) {
                try {
                    server.syncObject.wait();
                }
                catch( InterruptedException e ) {
                    // ignore.
                }
            }

            // Start client:
            System.out.println( "+ Starting Client..." );
            POALocalClient client = new POALocalClient( server.orb );
            client.run( System.getProperties(),
                        args, System.out, System.err, null );
            System.exit( 0 );
        }
        catch( Exception e ) {
            e.printStackTrace( System.err );
            System.exit( 1 );
        }
    }

    public void run( Properties environment, String args[], PrintStream out,
                     PrintStream err, Hashtable extra) 
        throws Exception
    {
        super.run( environment, args, out, err, extra );
    }

    void handshake() {
        // notify main that client can launch now:
        synchronized( syncObject ) {
            syncObject.notify();
        }
    }

    void waitForClients() {
        // NOP for this test.
    }


}
