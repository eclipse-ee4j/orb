/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package pi.serverinterceptor;

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

import ServerRequestInterceptor.*;
import java.rmi.*;
import javax.rmi.*;
import javax.naming.*;

public abstract class DSIRMIServer 
    extends ServerCommon 
{
    InitialContext initialNamingContext;

    private static final String hello2Id = "qwerty";
    private String hello2IOR;

    private TestServantLocator servantLocator;
    
    public void run( Properties environment, String args[], PrintStream out,
                     PrintStream err, Hashtable extra) 
        throws Exception
    {
        try {
            out.println( "+ Creating Initial naming context..." );
            // Inform the JNDI provider of the ORB to use and create
            // initial naming context:
            Hashtable env = new Hashtable();
            env.put( "java.naming.corba.orb", orb );
            initialNamingContext = new InitialContext( env );

            // Set up hello object:
            out.println( "+ Creating and binding Hello1 object..." );
            TestInitializer.helloRef = createAndBind( "Hello1", 
                                                      "[Hello1]" );

            out.println( "+ Creating and binding Hello1Forward object..." );
            TestInitializer.helloRefForward = createAndBind( "Hello1Forward",
                                                             "[Hello1Forward]" ); 

            handshake();

            // Test ServerInterceptor
            testServerInterceptor();
        } finally {
            finish() ;

            // Notify client it's time to exit.
            exitClient();
            waitForClients();
        }
    }

    abstract void handshake();

    abstract void waitForClients();

    /**
     * Creates and binds a hello object using RMI
     */
    public org.omg.CORBA.Object createAndBind ( String name, 
                                                String symbol )
        throws Exception
    {
        // create and register it with RMI
        helloDSIDeprecatedServant obj = new helloDSIDeprecatedServant( 
            orb, out, symbol );
        orb.connect( obj );
        initialNamingContext.rebind( name, obj );

        java.lang.Object o = initialNamingContext.lookup( name );
        return (org.omg.CORBA.Object)PortableRemoteObject.narrow( o, 
            org.omg.CORBA.Object.class );
    }

    /** 
     * Overridden from ServerCommon.  Oneway calls are not supported in RMI.
     */
    void testInvocation( String name, 
                         int mode, 
                         String correctOrder,
                         String methodName,
                         String correctMethodOrder,
                         boolean exceptionExpected )
        throws Exception 
    {
        // Rebind each time so that location forward information is
        // wiped out.  See CDRInputStream1_0 readObject.  This is necessary 
        // because the local case will always return the exact same object
        // on the client side otherwise.

        // Set up hello object:
        out.println( "+ Creating and binding Hello1 object..." );
        TestInitializer.helloRef = createAndBind( "Hello1", 
                                                  "[Hello1]" );

        out.println( "+ Creating and binding Hello1Forward object..." );
        TestInitializer.helloRefForward = createAndBind( "Hello1Forward",
                                                         "[Hello1Forward]" ); 


        if( !methodName.equals( "sayOneway" ) ) {
            super.testInvocation( name, mode, correctOrder, methodName,
                                  correctMethodOrder, exceptionExpected );
        }
    }
}

