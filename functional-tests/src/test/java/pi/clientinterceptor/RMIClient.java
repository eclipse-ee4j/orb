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

import org.omg.CORBA.*;
import org.omg.CosNaming.*;
import com.sun.corba.ee.impl.corba.AnyImpl;
import com.sun.corba.ee.spi.misc.ORBConstants;
import com.sun.corba.ee.impl.interceptors.*;
import org.omg.PortableInterceptor.*;
import corba.framework.*;

import java.rmi.*;

import java.util.*;
import java.io.*;
import javax.naming.*;
import javax.rmi.*;

public class RMIClient 
    extends ClientCommon
    implements InternalProcess 
{
    // Reference to hello object
    private helloIF helloRef;
    
    // Reference to hello object to be forwarded to:
    private helloIF helloRefForward;

    // Initial naming context
    InitialContext initialNamingContext;

    // Names for JNDI lookup:
    public static final String NAME1 = "hello2";
    public static final String NAME2 = "hello2Forward";

    public static void main(String args[]) {
        try {
            (new RMIClient()).run( System.getProperties(),
                                args, System.out, System.err, null );
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
        TestInitializer.out = out;
        this.out = out;
        this.err = err;

        out.println( "===================================" );
        out.println( "Instantiating ORB for RMI/IIOP test" );
        out.println( "===================================" );

        out.println( "+ Creating ORB..." );
        createORB( args );

        // Inform the JNDI provider of the ORB to use and create intial
        // naming context:
        out.println( "+ Creating initial naming context..." );
        Hashtable env = new Hashtable();
        env.put( "java.naming.corba.orb", orb );
        initialNamingContext = new InitialContext( env );

        try {
            // Test ClientInterceptor
            testClientInterceptor();
        } finally {
            finish() ;
        }
    }

    /**
     * Clear invocation flags of helloRef and helloRefForward
     */
    protected void clearInvoked() 
        throws Exception
    {
        helloRef.clearInvoked();
        helloRefForward.clearInvoked();
    }

    /**
     * Invoke the method with the given name on the object
     */
    protected void invokeMethod( String methodName )
        throws Exception
    {
        try {
            // Make an invocation:
            if( methodName.equals( "sayHello" ) ) {
                helloRef.sayHello();
            }
            else if( methodName.equals( "sayException" ) ) {
                helloRef.saySystemException();
            }
            else if( methodName.equals( "sayOneway" ) ) {
                helloRef.sayOneway();
            }
        }
        catch( RemoteException e ) {
            throw (Exception)e.detail;
        }
    }

    /**
     * Return true if the method was invoked
     */
    protected boolean wasInvoked() 
        throws Exception 
    {
        return helloRef.wasInvoked();
    }

    /**
     * Return true if the method was forwarded
     */
    protected boolean didForward() 
        throws Exception
    {
        return helloRefForward.wasInvoked();
    }

    /**
     * Perform ClientRequestInterceptor tests
     */
    protected void testClientInterceptor() 
        throws Exception 
    {
        super.testClientInterceptor();
    }

    /**
     * Re-resolves all references to eliminate any cached ForwardRequests
     * from the last invocation
     */
    protected void resolveReferences() 
        throws Exception 
    {
        out.println( "    + resolving references..." );
        out.println( "      - disabling interceptors..." );
        SampleClientRequestInterceptor.enabled = false;
        // Resolve the hello object.
        out.println( "      - " + NAME1 );
        helloRef = resolve( NAME1 );
        out.println( "      - " + NAME2 );
        helloRefForward = resolve( NAME2 );
        // The initializer will store the location the interceptors should
        // use during a forward request:
        TestInitializer.helloRefForward = 
            (org.omg.CORBA.Object)helloRefForward;
        out.println( "      - enabling interceptors..." );
        SampleClientRequestInterceptor.enabled = true;
    }

    /**
     * Implementation borrwed from corba.socket.HelloClient.java test
     */
    private helloIF resolve(String name)
        throws Exception
    {
        java.lang.Object obj = initialNamingContext.lookup( name );
        helloIF helloRef = (helloIF)PortableRemoteObject.narrow( 
            obj, helloIF.class );
        
        return helloRef;
    }

    /** 
     * Overridden from ClientCommon.  Oneway calls are not supported.
     */
    protected void testInvocation( String name, int mode, 
                                   String correctOrder,
                                   String methodName,
                                   boolean shouldInvokeTarget,
                                   boolean exceptionExpected,
                                   boolean forwardExpected ) 
        throws Exception 
    {
        if( !methodName.equals( "sayOneway" ) ) {
            super.testInvocation( name, mode, correctOrder, methodName,
                                  shouldInvokeTarget,
                                  exceptionExpected,
                                  forwardExpected );
        }
    }

}
