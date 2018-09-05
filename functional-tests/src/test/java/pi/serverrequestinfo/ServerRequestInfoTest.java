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

import corba.framework.*;
import java.util.*;

/**
 * Tests ServerRequestInfo as per Portable Interceptors spec
 * orbos/99-12-02, section 5.4.  See pi/assertions.html for Assertions
 * covered in this test.
 */
public class ServerRequestInfoTest
    extends CORBATest 
{
    public static String[] idlFiles = { "serverrequestinfo.idl" };

    // Set to true if at least one test fails.
    private boolean failed = false;

    Controller orbd;

    /*
    public static String[] javaFiles = { 
        "ClientCommon.java",
        "ServerCommon.java", 
        "POAClient.java", 
        "POAServer.java",
        "POALocalClient.java", 
        "POALocalServer.java",
        "DSIPOALocalServer.java",
        "POARemoteClient.java", 
        "POARemoteServer.java",
        "DSIPOARemoteServer.java",
        "RMIClient.java",
        "RMIServer.java",
        "DSIRMIClient.java",
        "DSIRMIServer.java",
        "RMILocalClient.java",
        "DSIRMILocalClient.java",
        "RMILocalServer.java",
        "DSIRMILocalServer.java",
        "RMIRemoteClient.java",
        "DSIRMIRemoteClient.java",
        "RMIRemoteServer.java",
        "DSIRMIRemoteServer.java",
        "TestInitializer.java",
        "SampleServerRequestInterceptor.java",
        "DSIImpl.java",
        "helloDelegate.java",
        "helloIF.java",
        "helloServant.java",
        "helloDSIServant.java",
        "helloDSIDeprecatedServant.java",
        "helloRMIIIOP.java",
        "PolicyFactoryHundred.java",
        "PolicyHundred.java",

        // Interception strategies:
        "InterceptorStrategy.java",
        "RequestId1Strategy.java",
        "AttributesValidStrategy.java",
        "AdapterIdStrategy.java",
        "OneWayStrategy.java",
        "ForwardReferenceStrategy.java",
        "ServiceContextStrategy.java",
        "ExceptionStrategy.java",
        "RequestInfoStackStrategy.java",
        "GetServerPolicyStrategy.java",

        // Invocation strategies:
        "InvokeStrategy.java",
        "InvokeVisitAll.java",
        "InvokeExceptions.java",
        "InvokeOneWay.java",
        "InvokeVisitAllForward.java"
    };

    public static String[] rmicClasses = {
        "pi.serverrequestinfo.helloRMIIIOP"
    };
    */

    protected void doTest() 
        throws Throwable 
    {
        /*
        Options.addIDLCompilerArgs( "-fall" );
        Options.setJavaFiles(javaFiles);
        Options.setIDLFiles(idlFiles);
        Options.addRMICArgs( "-nolocalstubs -iiop -keep -g" );
        Options.setRMICClasses( rmicClasses );

        compileIDLFiles();
        compileRMICFiles();
        compileJavaFiles();
        */

        startORBD(); 
        System.out.println();
        System.out.println( "      \t\t\t\tLocal\t\tRemote" );

        beginTest( "[POA]\t\t\t" );
        testPOALocal();
        endTest( "\t\t" );
        testPOARemote();
        endTest( "\n" );

        beginTest( "[POA DSI]\t\t\t" );
        testPOADSILocal();
        endTest( "\t\t" );
        testPOADSIRemote();
        endTest( "\n" );

        beginTest( "[RMI]\t\t\t" );
        testRMILocal();
        endTest( "\t\t" );
        testRMIRemote();
        endTest( "\n" );

        beginTest( "[ServerRequestDispatcher DSI]\t" );
        testServerRequestDispatcherDSILocal();
        endTest( "\t\t" );
        testServerRequestDispatcherDSIRemote();
        endTest( "\n" );
        stopORBD();

        System.out.println();
        System.out.print( "      Final Result: " );
        if( failed ) {
            throw new RuntimeException( "Errors detected" );
        }
    }

    private void testPOALocal()
        throws Throwable
    {
        Controller server;

        try {

            // Start only a server - the server will create the client in this
            // test.  Create it as a client so no handshake is tested for.
            server = createClient( "pi.serverrequestinfo.POALocalServer",
                                   "poalocal" );
            server.start();
            server.waitFor();
            printEndTest( server, null );
            server.stop();
        }
        finally {
        }
    }

    private void testPOARemote()
        throws Throwable
    {
        Controller client, server;

        try {

            server = createServer( "pi.serverrequestinfo.POARemoteServer",
                                              "poa-server" );
            server.start();
            client = createClient( "pi.serverrequestinfo.POARemoteClient",
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

    private void testPOADSILocal()
        throws Throwable
    {
        Controller server;

        try {
            // Start only a server - the server will create the client in this
            // test.  Create it as a client so no handshake is tested for.
            server = createClient( "pi.serverrequestinfo.DSIPOALocalServer",
                                   "dsipoalocal" );
            server.start();
            server.waitFor();
            printEndTest( server, null );
            server.stop();
        }
        finally {
        }
    }

    private void testPOADSIRemote()
        throws Throwable
    {
        Controller client, server;

        try {
            server = createServer( "pi.serverrequestinfo.DSIPOARemoteServer",
                                              "dsi-poa-server" );
            server.start();
            client = createClient( "pi.serverrequestinfo.POARemoteClient",
                                              "dsi-poa-client" );
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
        Controller server;

        try {

            // Start only a server - the server will create the client in this
            // test.  Create it as a client so no handshake is tested for.
            server = createClient( "pi.serverrequestinfo.RMILocalServer",
                                   "rmilocal" );
            server.start();
            server.waitFor();
            printEndTest( server, null );
            server.stop();
        }
        finally {
        }
    }

    private void testRMIRemote()
        throws Throwable
    {
        Controller client, server;

        try {
            server = createServer( "pi.serverrequestinfo.RMIRemoteServer",
                                   "rmi-server" );
            server.start();
            client = createClient( "pi.serverrequestinfo.RMIRemoteClient",
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

    private void testServerRequestDispatcherDSILocal()
        throws Throwable
    {
        Controller server;

        try {

            // Start only a server - the server will create the client in this
            // test.  Create it as a client so no handshake is tested for.
            server = createClient( "pi.serverrequestinfo.DSIRMILocalServer",
                                   "dsirmilocal" );
            server.start();
            server.waitFor();
            printEndTest( server, null );
            server.stop();
        }
        finally {
        }
    }


    private void testServerRequestDispatcherDSIRemote()
        throws Throwable
    {
        Controller client, server;

        try {

            server = createServer( "pi.serverrequestinfo.DSIRMIRemoteServer",
                                   "dsi-rmi-server" );
            server.start();
            client = createClient( "pi.serverrequestinfo.DSIRMIRemoteClient",
                                   "dsi-rmi-client" );

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

