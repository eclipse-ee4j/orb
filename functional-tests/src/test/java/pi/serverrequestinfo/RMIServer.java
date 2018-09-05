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
import java.rmi.*;
import javax.rmi.*;
import javax.naming.*;

public abstract class RMIServer 
    extends ServerCommon 
    implements helloDelegate.ClientCallback
{
    InitialContext initialNamingContext;

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
            testServerRequestInfo();
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
        helloRMIIIOP obj = new helloRMIIIOP( out, symbol, this );
        initialNamingContext.rebind( name, obj );

        java.lang.Object o = initialNamingContext.lookup( name );
        helloIF helloRef = (helloIF)PortableRemoteObject.narrow( o, 
            helloIF.class );
        return (org.omg.CORBA.Object)helloRef;
    }

    /**
     * One-way test not applicable for RMI case.  Override it.
     */
    protected void testOneWay() throws Exception {
        out.println( "+ OneWay test not applicable for RMI.  Skipping..." );
    }

    /**
     * Passes in the appropriate valid and invalid repository ids for RMI
     */
    protected void testAttributesValid() 
        throws Exception
    {
        testAttributesValid( 
            "RMI:pi.serverrequestinfo.helloIF:0000000000000000",
            "RMI:pi.serverrequestinfo.goodbyeIF:0000000000000000" );
    }

    
    // ClientCallback interface for request info stack test:
    public String sayHello() {
        String result = "";

        out.println( 
            "    + ClientCallback: resolving and invoking sayHello()..." );
        try {
            helloIF helloRef = resolve( "Hello1" );
            result = helloRef.sayHello();
        }
        catch( Exception e ) {
            e.printStackTrace();
            throw new RuntimeException( "ClientCallback: Exception thrown." );
        }

        return result;
    }

    public void saySystemException() {
        out.println( 
            "    + ClientCallback: resolving and invoking " + 
            "saySystemException()..." );
        try {
            helloIF helloRef = resolve( "Hello1" );
            try {
                helloRef.saySystemException();
            }
            catch( RemoteException e ) {
                throw e.detail;
            }
        }
        catch( SystemException e ) {
            // expected.
            throw e;
        }
        catch( Throwable e ) {
            System.err.println( "ClientCallback: Exception " + 
                e.getClass().getName() + " thrown." );
            e.printStackTrace();
            throw new RuntimeException( "ClientCallback: Exception thrown." );
        }
    }

    /**
     * Resolves name using RMI
     */
    helloIF resolve(String name)
        throws Exception
    {
        java.lang.Object obj = initialNamingContext.lookup( name );
        helloIF helloRef = (helloIF)PortableRemoteObject.narrow(
            obj, helloIF.class );

        return helloRef;
    }
}

