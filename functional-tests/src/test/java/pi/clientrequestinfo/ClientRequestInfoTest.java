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

package pi.clientrequestinfo;

import corba.framework.*;
import java.util.*;

/**
 * Tests ClientRequestInfo as per Portable Interceptors spec
 * orbos/99-12-02, section 5.4.  See pi/assertions.html for Assertions
 * covered in this test.
 */
public class ClientRequestInfoTest
    extends CORBATest 
{
    // Set to true if at least one test fails.
    private boolean failed = false;

    Controller orbd;

    public static String[] rmicClasses = {
        "pi.clientrequestinfo.helloRMIIIOP"
    };

    protected void doTest() 
        throws Throwable 
    {
        startORBD();
        System.out.println();
        System.out.println( "      \t\t\t\tLocal\t\tRemote" );

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
        Controller client;

        try {
            // Start only a client - the client will create the server.
            client = createClient( 
                "pi.clientrequestinfo.POALocalClient",
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
            server = createServer( "pi.clientrequestinfo.POAServer",
                                              "poa-server" );
            server.start();
            client = createClient( "pi.clientrequestinfo.POAClient",
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
        Controller client;

        try {
            // Start only a client - the client will create the server.
            client = createClient( "pi.clientrequestinfo.DIIPOALocalClient",
                                              "diipoalocal" );
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
            server = createServer( "pi.clientrequestinfo.POAServer",
                                              "dii-poa-server" );
            server.start();
            client = createClient( "pi.clientrequestinfo.DIIPOAClient",
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
        Controller client;

        try {
            // Start only a client - the client will create the server.
            client = createClient( "pi.clientrequestinfo.RMILocalClient",
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
            server = createServer( "pi.clientrequestinfo.RMIServer",
                                   "rmi-server" );
            server.start();
            client = createClient( "pi.clientrequestinfo.RMIClient",
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
            client = createClient( "pi.clientrequestinfo.DIIRMILocalClient",
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
            server = createServer( "pi.clientrequestinfo.OldRMIServer",
                                   "dii-rmi-server" );
            server.start();
            client = createClient( "pi.clientrequestinfo.DIIPOAClient",
                                   "dii-rmi-client" );
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
