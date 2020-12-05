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

/**
 * Common base class for POA Server test files.
 */
public abstract class POAServer 
    extends ServerCommon 
{
    static final String ROOT_POA = "RootPOA";
    
    POA rootPOA;
    POA persistentPOA;

    static final String hello2Id = "qwerty";
    String hello2IOR;

    TestServantLocator servantLocator;
    
    // To be invoked by subclass after orb is created.
    public void run( Properties environment, String args[], PrintStream out,
                     PrintStream err, Hashtable extra) 
        throws Exception
    {
        try {
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

            // Create a persistent, non-retaining POA with a ServantLocator:
            out.println( "+ Creating persistent POA with ServantLocator..." );
            persistentPOA = createPersistentPOA();

            out.println( "+ Creating and binding Hello2 reference..." );
            hello2IOR = createReference( persistentPOA, "Hello2", 
                                         hello2Id.getBytes() );

            handshake();
            
            // Test ServerInterceptor
            testServerInterceptor();

            // Test ServantManager throwing a ForwardRequest.
            testServantManager();

            // Test POA special operations
            testSpecialOps();
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
     * Tests that if a ServantManager throws a ForwardRequest, everything
     * works as expected.
     */
    void testServantManager()
        throws Exception
    {
        out.println();
        out.println( "Running ServantManager test" );
        out.println( "===========================" );

        out.println( "+ Testing standard invocation where ServantLocator " +
            "redirects..." );
        testInvocation( "testInvocationServantLocatorRedirect",
            SampleServerRequestInterceptor.MODE_NORMAL,
            "rs1rs2rs3so3so2so1rs1rs2rs3rr1rr2rr3sr3sr2sr1", 
            "sayHello2@" + hello2IOR, "[Hello1]", false );

        // ServantLocator forwards to Hello1 and send_other throws 
        // SystemException.
        out.println( "+ Testing invocation where ServantLocator redirects " +
            "and send_other throws SystemException..." );
        servantLocator.resetFirstTime();
        testInvocation( "testInvocationServantLocatorRedirectSendOtherSystemException",
            SampleServerRequestInterceptor.MODE_SO_SYSTEM_EXCEPTION,
            "rs1rs2rs3so3so2se1", 
            "sayHello2@" + hello2IOR, "", true );

        // ServantLocator forwards to Hello1 and send_other redirects to
        // helloRefForward.
        out.println( "+ Testing invocation where ServantLocator redirects " +
            "and send_other further redirects..." );
        servantLocator.resetFirstTime();
        testInvocation( "testInvocationServantLocatorRedirectSendOtherRedirect",
            SampleServerRequestInterceptor.MODE_SO_FORWARD_REQUEST,
            "rs1rs2rs3so3so2so1rs1rs2rs3rr1rr2rr3sr3sr2sr1", 
            "sayHello2@" + hello2IOR, "[Hello1Forward]", false );
    }

    /**
     * Tests the special operations _is_a, _get_interface_def, and 
     * _non_existent.
     */
    void testSpecialOps()
        throws Exception
    {
        out.println();
        out.println( "Running Special Operations Tests" );
        out.println( "================================" );

        out.println( "+ Testing _is_a..." );
        SampleServerRequestInterceptor.dontIgnoreIsA = true;
        testInvocation( "testInvocationIsA",
            SampleServerRequestInterceptor.MODE_NORMAL,
            "rs1rs2rs3rr1rr2rr3sr3sr2sr1",
            "_is_a", "", false );

        // We do not implement this interface in our ORB. 
        // Thus, the send_exception.  We pass in false for exception
        // expected because this is not the exception we normally look for.
        out.println( "+ Testing _get_interface_def..." );
        testInvocation( "testInvocationGetInterfaceDef",
            SampleServerRequestInterceptor.MODE_NORMAL,
            "rs1rs2rs3rr1rr2rr3se3se2se1",
            "_get_interface_def", "", false );

        out.println( "+ Testing _non_existent..." );
        testInvocation( "testInvocationNonExistent",
            SampleServerRequestInterceptor.MODE_NORMAL,
            "rs1rs2rs3rr1rr2rr3sr3sr2sr1",
            "_non_existent", "", false );
    }

    /**
     * Borrowed from rmipoacounter test and modified:
     */
    POA createPersistentPOA()
        throws Exception
    {
        // create a persistent, non-retaining POA
        Policy[] tpolicy = new Policy[3];
        tpolicy[0] = rootPOA.create_lifespan_policy(
            LifespanPolicyValue.PERSISTENT );
        tpolicy[1] = rootPOA.create_request_processing_policy(
            RequestProcessingPolicyValue.USE_SERVANT_MANAGER );
        tpolicy[2] = rootPOA.create_servant_retention_policy(
            ServantRetentionPolicyValue.NON_RETAIN );

        POA tpoa = rootPOA.create_POA("PersistentPOA", null, tpolicy);

        // register the ServantLocator with the POA, then activate POA
        servantLocator = new TestServantLocator( out, orb, 
            TestInitializer.helloRef );
        tpoa.set_servant_manager( servantLocator );
        tpoa.the_POAManager().activate();
        return tpoa;
    }

    /**
     * Creates an object with an id, but does not bind it to the naming 
     * service.  Returns the ior for that object.
     */
    String createReference( POA poa, String name, byte[] id ) 
        throws Exception
    {
        org.omg.CORBA.Object obj = poa.create_reference_with_id( id, 
            "IDL:ServerRequestInterceptor/hello:1.0" );
        return orb.object_to_string( obj );
    }

    /**
     * Implementation borrowed from corba.socket.HelloServer test
     */
    public org.omg.CORBA.Object createAndBind ( POA poa, String name, 
                                                String symbol )
        throws Exception
    {
        // create servant and register it with the ORB
        helloServant helloRef = new helloServant( out, symbol );

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

}
