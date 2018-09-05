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
 * Common base class for POA Server test files.
 */
public abstract class POAServer 
    extends ServerCommon 
    implements helloDelegate.ClientCallback
{
    static final String ROOT_POA = "RootPOA";
    
    POA rootPOA;

    POA childPOA1;
    POA childPOA2;
    org.omg.CORBA.Object childPOA1Object;
    org.omg.CORBA.Object childPOA2Object;
    byte[] childPOA1Id;
    byte[] childPOA2Id;

    // true if this is a dynamic implementation, or false if not
    boolean dsi;

    public POAServer() {
        this( false );
    }

    public POAServer( boolean dsi ) {
        this.dsi = dsi;
    }

    // To be invoked by subclass after orb is created.
    public void run( Properties environment, String args[], PrintStream out,
                     PrintStream err, Hashtable extra) 
        throws Exception
    {
        try {
            this.out = out;

            // Get the root POA:
            rootPOA = null;
            out.println( "+ Obtaining handle to root POA and activating..." );
            try {
                rootPOA = (POA)orb.resolve_initial_references( ROOT_POA );
            }
            catch( InvalidName e ) {
                err.println( ROOT_POA + " is an invalid name." );
                throw e;
            }
            rootPOA.the_POAManager().activate();
            
            // Set up hello object:
            out.println( "+ Creating and binding Hello1 object..." );
            TestInitializer.helloRef = createAndBind( rootPOA, "Hello1", 
                                                      "[Hello1]" );

            out.println( "+ Creating and binding Hello1Forward object..." );
            TestInitializer.helloRefForward = createAndBind( rootPOA, 
                                                             "Hello1Forward",
                                                             "[Hello1Forward]" ); 

            // Create 2 additional POAs, each with different IDs, so we can test
            // adapter_id:
            out.println( "+ Creating 2 additional POAs with different IDs..." );
            createChildPOA( 1 );
            createChildPOA( 2 );

            handshake();
            
            // Test ServerRequestInfo
            testServerRequestInfo();

            // Test adapter_id
            testAdapterId();

            // Test get_server_policy
            testGetServerPolicy();
        } finally {
            finish() ;

            // Notify client it's time to exit.
            exitClient();

            // wait for invocations from clients
            waitForClients();
        }
    }

    // Output handshake or wake up main.
    abstract void handshake();

    // Wait for invocations from clients.
    abstract void waitForClients();

    /**
     * Implementation borrowed from corba.socket.HelloServer test
     */
    public org.omg.CORBA.Object createAndBind ( POA poa, String name, 
                                                String symbol )
        throws Exception
    {
        // create servant and register it with the ORB
        Servant helloRef;
        
        if( dsi ) {
            helloRef = new helloDSIServant( orb, out, symbol, this );
        }
        else {
            helloRef = new helloServant( out, symbol, this );
        }

        byte[] id = poa.activate_object(helloRef);
        org.omg.CORBA.Object ref = poa.id_to_reference(id);
      
        // get the root naming context
        org.omg.CORBA.Object objRef = 
            orb.resolve_initial_references("NameService");
        NamingContext ncRef = NamingContextHelper.narrow(objRef);
      
        // bind the Object Reference in Naming
        NameComponent nc = new NameComponent(name, "");
        NameComponent path[] = {nc};
            
        ncRef.rebind(path, ref);

        return ref;
    }

    /**
     * Passes in the appropriate valid and invalid repository ids for POA
     */
    protected void testAttributesValid() 
        throws Exception
    {
        testAttributesValid( 
            "IDL:ServerRequestInfo/hello:1.0",
            "IDL:ServerRequestInfo/goodbye:1.0" );
    }


    /**
     * Special test case for adapter_id that makes an invocation on
     * two separate objects, using two separate POAs each with a different
     * adapter ID.  Checks to make sure adapter_id returns the correct
     * value for each.
     */
    protected void testAdapterId()
        throws Exception
    {
        out.println( "+ Testing adapter_id with child POA 1..." );
        InterceptorStrategy interceptorStrategy =
            new AdapterIdStrategy( childPOA1Id );
        InvokeStrategy invokeStrategy = new InvokeVisitAll( "child1." );
        setParameters( interceptorStrategy, invokeStrategy );
        runTestCase( "adapter_id_1" );

        out.println( "+ Testing adapter_id with child POA 2..." );
        interceptorStrategy = new AdapterIdStrategy( childPOA2Id );
        invokeStrategy = new InvokeVisitAll( "child2." );
        setParameters( interceptorStrategy, invokeStrategy );
        runTestCase( "adapter_id_2" );
    }

    /**
     * Special test case for get_server_policy that verifies we can retrieve
     * policies from ServerRequestInfo
     */
    protected void testGetServerPolicy() 
        throws Exception
    {
        out.println( "+ Testing get_server_policy..." );
        InterceptorStrategy interceptorStrategy =
            new GetServerPolicyStrategy();
        InvokeStrategy invokeStrategy = new InvokeVisitAll( "child1." );
        setParameters( interceptorStrategy, invokeStrategy );
        runTestCase( "get_server_policy" );
    }

    /**
     * Creates a child POA and remember its adapter id.  Attach some policies
     * to the POA so we can test get_server_policy later on.
     */
    private void createChildPOA( int n ) 
        throws Exception
    {
        Policy[] policies = new Policy[2];

        policies[0] = rootPOA.create_id_uniqueness_policy(
            IdUniquenessPolicyValue.MULTIPLE_ID );
        Any value = orb.create_any();
        value.insert_long( 99 );
        policies[1] = orb.create_policy( 100, value );

        POA tpoa = rootPOA.create_POA( "childPOA" + n, null, policies );
        tpoa.the_POAManager().activate();
        org.omg.CORBA.Object obj = 
            createAndBind( tpoa, "HelloChild" + n, "[HelloChild" + n + "]" );
        byte[] id = tpoa.id();

        out.println( "  - Adpater id is " + id );

        if( n == 1 ) {
            childPOA1 = tpoa;
            childPOA1Object = obj;
            childPOA1Id = id;
        }
        else {
            childPOA2 = tpoa;
            childPOA2Object = obj;
            childPOA2Id = id;
        }
    }

    // ClientCallback interface for request info stack test:
    public String sayHello() {
        String result = "";

        out.println( 
            "    + ClientCallback: resolving and invoking sayHello()..." );
        try {
            hello helloRef = POAClient.resolve( orb, "Hello1" );
            result = helloRef.sayHello();
        }
        catch( Exception e ) {
            e.printStackTrace();
            throw new RuntimeException( "ClientCallback: exception thrown." );
        }

        return result;

    }

    public void saySystemException() {
        out.println( 
            "    + ClientCallback: resolving and invoking " + 
            "saySystemException()..." );
        try {
            hello helloRef = POAClient.resolve( orb, "Hello1" );
            helloRef.saySystemException();
        }
        catch( SystemException e ) {
            // expected.
            throw e;
        }
        catch( Exception e ) {
            e.printStackTrace();
            throw new RuntimeException( "ClientCallback: exception thrown." );
        }
    }

}
