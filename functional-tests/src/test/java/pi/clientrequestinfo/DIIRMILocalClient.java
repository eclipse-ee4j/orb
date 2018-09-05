/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package pi.clientrequestinfo;

import org.omg.CORBA.*;
import org.omg.CosNaming.*;
import com.sun.corba.ee.impl.corba.AnyImpl;
import com.sun.corba.ee.spi.misc.ORBConstants;
import com.sun.corba.ee.impl.interceptors.*;
import org.omg.PortableInterceptor.*;
import corba.framework.*;

import java.util.*;
import java.io.*;

import ClientRequestInfo.*;

/**
 * Tests DII RMI Local invocation (with a co-located orb)
 */
public class DIIRMILocalClient 
    extends ClientCommon
    implements InternalProcess 
{
    // Reference to hello object
    private helloDIIClientStub helloRef;
    
    // Reference to hello object to be forwarded to:
    private helloDIIClientStub helloRefForward;

    // Object to synchronize on to wait for server to start:
    private java.lang.Object syncObject;

    public static void main(String args[]) {
        final String[] arguments = args;
        try {
            System.out.println( "===================================" );
            System.out.println( "Creating ORB for DII RMI Local test" );
            System.out.println( "===================================" );

            final DIIRMILocalClient client = new DIIRMILocalClient();

            TestInitializer.out = System.out;
            client.out = System.out;
            client.err = System.err;

            // For this test, start both the client and the server using
            // the same ORB.
            System.out.println( "+ Creating ORB for client and server..." );
            client.createORB( args );

            System.out.println( "+ Starting Server..." );
            client.syncObject = new java.lang.Object();
            new Thread() {
                public void run() {
                    try {
                        (new OldRMILocalServer()).run( 
                                                client.orb, client.syncObject,
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
            synchronized( client.syncObject ) {
                try {
                    client.syncObject.wait();
                }
                catch( InterruptedException e ) {
                    // ignore.
                }
            }

            // Start client:
            System.out.println( "+ Starting Client..." );
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
        try {
            // Test ClientInterceptor
            testClientRequestInfo();
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
        else if( methodName.equals( "saySystemException" ) ) {
            helloRef.saySystemException();
        }
        else if( methodName.equals( "sayUserException" ) ) {
            helloRef.sayUserException();
        }
        else if( methodName.equals( "sayOneway" ) ) {
            helloRef.sayOneway();
        }
        else if( methodName.equals( "sayArguments" ) ) {
            helloRef.sayArguments( "one", 2, true );
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
     * Perform ClientRequestInfo tests
     */
    protected void testClientRequestInfo () 
        throws Exception 
    {
        super.testClientRequestInfo();

        // Test DII dynamic calls:
        testDynamic();
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
        // The initializer will store the location the interceptors should
        // use during a normal request:
        TestInitializer.helloRef = helloRef.getObject();
        out.println( "      - Hello1Forward" );
        helloRefForward = resolve( orb, "Hello1Forward" );
        // The initializer will store the location the interceptors should
        // use during a forward request:
        TestInitializer.helloRefForward = helloRefForward.getObject();
        out.println( "      - enabling interceptors..." );
        SampleClientRequestInterceptor.enabled = true;
    }

    /**
     * Implementation borrwed from corba.socket.HelloClient.java test
     */
    private helloDIIClientStub resolve(ORB orb, String name)
        throws Exception
    {
        // Get the root naming context
        org.omg.CORBA.Object objRef = 
            orb.resolve_initial_references("NameService");
        NamingContext ncRef = NamingContextHelper.narrow(objRef);
        
        // resolve the Object Reference in Naming
        NameComponent nc = new NameComponent(name, "");
        NameComponent path[] = {nc};
        org.omg.CORBA.Object helloRef = ncRef.resolve( path );
        
        return new helloDIIClientStub( orb, helloRef );
    }

    /**
     * One-way test not applicable for RMI case.  Override it.
     */
    protected void testOneWay() throws Exception {
        out.println( "+ OneWay test not applicable for RMI.  Skipping..." );
    }

    /**
     * Test DII-specific features
     */
    protected void testDynamic() 
        throws Exception
    {                                                       
        out.println( "+ Testing DII ClientRequestInfo methods..." );

        InterceptorStrategy interceptorStrategy =
            new DynamicStrategy();
        InvokeStrategy invokeStrategy = new InvokeDynamic();
        setParameters( interceptorStrategy, invokeStrategy );
        runTestCase( "dii_dynamic" );
    }                           

    /**
     * When asked to run exception tests, run in DII mode.
     * This will skip any UserException-related tests.
     */
    protected void testException() 
        throws Exception
    {                                                       
        out.println( "+ Testing received_exception[_id]() in DII mode..." );

        InterceptorStrategy interceptorStrategy =
            new ExceptionStrategy( true );
        InvokeStrategy invokeStrategy = new InvokeExceptions();
        setParameters( interceptorStrategy, invokeStrategy );
        runTestCase( "received_exception[_id]" );
    }                                        

    /**
     * Executes the test case set up with the parameters in setParameters
     */
    protected void runTestCase( String testName )
        throws Exception
    {
        super.runTestCase( testName );

        out.println( "    + Resetting servants on server side" );
        out.println( "      - disabling interceptors..." );
        SampleClientRequestInterceptor.enabled = false;
        helloRef.resetServant();
        out.println( "      - enabling interceptors..." );
        SampleClientRequestInterceptor.enabled = true;
    }

}



