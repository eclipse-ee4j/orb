/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package corba.enuminterop  ;

import corba.framework.TestngRunner;
import java.io.PrintStream;
import java.rmi.RemoteException ;

import org.omg.CORBA.ORB;

import javax.rmi.PortableRemoteObject;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContext;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextHelper;

import org.testng.Assert ;
import org.testng.annotations.Test ;

/**
 * This tests that enums can be correctly deserialized when sent from the JDK ORB (no EnumDesc support)
 * to GlassFish, which supports EnumDesc.  We may also add a config flag to allow testing between two
 * GlassFish ORB instances.
 *
 * Basic test: have server run on JDK ORB (or GF with noEnumDesc configuration), and
 * then see if the client can correctly receive an echoed enum from the server.
 */
public class Client
{
    private PrintStream out ;
    private PrintStream err ;
    private NamingContextExt nctx = null;
    private Echo echo = null;
    private ORB orb;

    private static String[] args;

    public static void main( String[] args ) 
    {
        Client.args = args ;
        TestngRunner runner = new TestngRunner() ;
        runner.registerClass( Client.class ) ;
        runner.run() ;
    }

    public Client() throws Exception {
        this.out = System.out;
        this.err = System.err;

        orb = ORB.init( args, null );

        org.omg.CORBA.Object objRef =
            orb.resolve_initial_references("NameService");

        NamingContext ncRef = NamingContextHelper.narrow(objRef);
        NameComponent nc = new NameComponent(Server.REF_NAME, "");
        NameComponent[] path = {nc};
                                                                            
        echo = (Echo)PortableRemoteObject.narrow(ncRef.resolve(path),
                                                   Echo.class);
    }

    @Test
    public void testEcho() throws RemoteException {
        Echo.Day result = (Echo.Day)echo.echoObject( "Sunday" ) ;
        Assert.assertSame( result, Echo.Day.Sunday ) ;
    }

    @Test
    public void testEchoDay() throws RemoteException {
        Echo.Day result = echo.echoDay( "Tuesday" ) ;
        Assert.assertSame( result, Echo.Day.Tuesday ) ;
    }
}
