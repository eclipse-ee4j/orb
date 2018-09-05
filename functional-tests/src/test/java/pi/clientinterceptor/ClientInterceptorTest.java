/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package pi.clientinterceptor;

import corba.framework.*;
import java.util.*;

/**
 * Tests ClientInterceptor as per Portable Interceptors spec
 * orbos/99-12-02, section 5.2.  See pi/assertions.html for Assertions
 * covered in this test.
 * <p>
 * The test works as follows:
 * <ul>
 *   <li>First, a test ORB initializer registers three client request
 *       interceptors with the ORB.</li>
 * </ul>
 */
public class ClientInterceptorTest
    extends CORBATest 
{
    // Set to true if at least one test fails.
    private boolean failed = false;

    Controller orbd;

    protected void doTest() 
        throws Throwable 
    {
        System.out.println();
        System.out.println( "      \t\t\t\tLocal\t\tRemote" );

        startORBD( );        

        beginTest( "[POA]\t\t\t" );
        testPOALocal();
        endTest( "\t\t" );
        testPOARemote();
        endTest( "\n" );

        beginTest( "[POA DII]\t\t\t" );
        testPOADIILocal();
        endTest( "\t\t" );
        testPOADIIRemote();
        endTest( "\n" );

        beginTest( "[RMI]\t\t\t" );
        testRMILocal();
        endTest( "\t\t" );
        testRMIRemote();
        endTest( "\n" );

        beginTest( "[ClientDelegate DII]\t" );
        testClientDelegateDIILocal();
        endTest( "\t\t" );
        testClientDelegateDIIRemote();
        endTest( "\n" );
        stopORBD( );        

        System.out.println();
        System.out.print( "      Final Result: " );
        if( failed ) {
            throw new RuntimeException( "Errors detected" );
        }
    }

    private void testPOALocal()
        throws Throwable
    {
        Controller client;

        try {
            // Start only a client - the client will create the server.
            client = createClient( "pi.clientinterceptor.POALocalClient",
                                   "poalocal" );
            client.start();
            client.waitFor();
            printEndTest( client, null );
            client.stop();
        }
        finally {
        }
    }

    private void testPOARemote()
        throws Throwable
    {
        Controller client, server;

        try {
            server = createServer( "pi.clientinterceptor.POAServer",
                                   "poa-server" );
            server.start();
            client = createClient( "pi.clientinterceptor.POAClient",
                                   "poa-client" );
            client.start();
            client.waitFor();
            printEndTest( client, server );
            client.stop();
            server.stop();
        }
        finally {
        }
    }

    private void testPOADIILocal()
        throws Throwable
    {
        Controller client, server;

        try {
            // Start only a client - the client will create the server.
            client = createClient( 
                "pi.clientinterceptor.DIIPOALocalClient", "diipoalocal" );
            client.start();
            client.waitFor();
            printEndTest( client, null );
            client.stop();
        }
        finally {
        }
    }

    private void testPOADIIRemote()
        throws Throwable
    {
        Controller client, server;

        try {
            server = createServer( "pi.clientinterceptor.POAServer",
                                              "dii-poa-server" );
            server.start();
            client = createClient( "pi.clientinterceptor.DIIPOAClient",
                                              "dii-poa-client" );
            client.start();
            client.waitFor();
            printEndTest( client, server );
            client.stop();
            server.stop();
        }
        finally {
        }
    }

    private void testRMILocal() 
        throws Throwable
    {
        try {
            Controller client;

            // Start only a client - the client will create the server.
            client = createClient( "pi.clientinterceptor.RMILocalClient",
                                              "rmilocal" );
            client.start();
            client.waitFor();
            printEndTest( client, null );
            client.stop();
        }
        finally {
        }
    }

    private void testRMIRemote() 
        throws Throwable
    {
        Controller client, server;

        try {
            server = createServer( "pi.clientinterceptor.RMIServer",
                                   "rmi-server" );
            server.start();
            client = createClient( "pi.clientinterceptor.RMIClient",
                                   "rmi-client" );
            client.start();
            client.waitFor();
            printEndTest( client, server );
            client.stop();
            server.stop();
        }
        finally {
        }
    }

    private void testClientDelegateDIILocal() 
        throws Throwable
    {
        Controller client;

        try {
            // Start only a client - the client will create the server.
            client = createClient( "pi.clientinterceptor.DIIRMILocalClient",
                                   "diirmilocal" );
            client.start();
            client.waitFor();
            printEndTest( client, null );
            client.stop();
        }
        finally {
        }
    }

    private void testClientDelegateDIIRemote() 
        throws Throwable
    {
        Controller client, server;

        try {
            server = createServer( "pi.clientinterceptor.OldRMIServer",
                                              "dii-oldrmi-server" );
            server.start();
            client = createClient( "pi.clientinterceptor.DIIPOAClient",
                                              "dii-oldrmi-client" );
            client.start();
            client.waitFor();
            printEndTest( client, server );
            client.stop();
            server.stop();
        }
        finally {
        }
    }

    private void beginTest( String name ) 
        throws Exception
    {
        System.out.print( "      " + name );
    }

    private void endTest( String terminator ) 
        throws Exception
    {
        System.out.print( terminator );
    }

    private void startORBD() 
        throws Exception
    {
        orbd = createORBD();
        orbd.start();
    }

    private void stopORBD()
        throws Exception
    {
        orbd.stop();
        pause();
    }

    private void printEndTest( Controller client, Controller server ) 
        throws Throwable
    {
        if( (server != null) && server.finished() ) {
            System.out.print( "FAILED, Server crashed" );
            failed = true;
        }
        else if( client.exitValue() != Controller.SUCCESS ) {
            System.out.print( "FAILED, Client exit value = " + 
                client.exitValue() );
            failed = true;
        }
        else {
            System.out.print( "PASSED" );
        }
    }

    // Pause a little to allow all processes to fully terminate.
    private void pause() {
        try {
            Thread.sleep( 2000 );
        }
        catch( InterruptedException e ) {
            // ignore.
        }
    }
}

