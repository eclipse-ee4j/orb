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

import java.util.*;
import java.io.*;

import ClientRequestInterceptor.*;

/**
 * Tests POA Remote invocation
 */
public class POAClient 
    extends ClientCommon
    implements InternalProcess 
{
    // Reference to hello object
    private hello helloRef;
    
    // Reference to hello object to be forwarded to:
    private hello helloRefForward;

    public static void main(String args[]) {
        try {
            (new POAClient()).run( System.getProperties(),
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

        out.println( "================================" );
        out.println( "Invoking ORB for POA Remote test" );
        out.println( "================================" );

        out.println( "+ Creating ORB..." );
        createORB( args );

        try {
            // Test ClientInterceptor
            testClientInterceptor();

            // Test POA Special operations
            testSpecialOperations();
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
        else if( methodName.equals( "_is_a" ) ) {
            helloRef._is_a( "IDL:ServerRequestInterceptor/goodbye:1.0" );
        }
        else if( methodName.equals( "_get_interface_def" ) ) {
           try {
                helloRef._get_interface_def();
            }
            catch( NO_IMPLEMENT e ) {
                // This is expected in our ORB.
            }
        }
        else if( methodName.equals( "_non_existent" ) ) {
            helloRef._non_existent();
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
        out.println( "      - Hello1" );
        helloRef = resolve( orb, "Hello1" );
        out.println( "      - Hello1Forward" );
        helloRefForward = resolve( orb, "Hello1Forward" );
        // The initializer will store the location the interceptors should
        // use during a forward request:
        TestInitializer.helloRefForward = helloRefForward;
        out.println( "      - enabling interceptors..." );
        SampleClientRequestInterceptor.enabled = true;
    }

    /**
     * Implementation borrwed from corba.socket.HelloClient.java test
     */
    private hello resolve(ORB orb, String name)
        throws Exception
    {
        // Get the root naming context
        org.omg.CORBA.Object objRef = 
            orb.resolve_initial_references("NameService");
        NamingContext ncRef = NamingContextHelper.narrow(objRef);
        
        // resolve the Object Reference in Naming
        NameComponent nc = new NameComponent(name, "");
        NameComponent path[] = {nc};
        hello helloRef = helloHelper.narrow(ncRef.resolve(path));
        
        return helloRef;
    }
    
}



