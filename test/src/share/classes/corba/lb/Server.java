/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

//
// Created       : 2005 Oct 05 (Wed) 13:54:09 by Harold Carr.
// Last Modified : 2005 Oct 06 (Thu) 11:32:08 by Harold Carr.
//

package corba.lb;

import java.util.Properties;
import java.util.Hashtable;

import javax.naming.InitialContext ;

import javax.rmi.PortableRemoteObject ;

import org.omg.CORBA.ORB;
import org.omg.CORBA.Policy ;

import org.omg.PortableServer.POA;
import org.omg.PortableServer.Servant;
import org.omg.PortableServer.LifespanPolicyValue;
import org.omg.PortableServer.IdUniquenessPolicyValue;
import org.omg.PortableServer.IdAssignmentPolicyValue;
import org.omg.PortableServer.ImplicitActivationPolicyValue;
import org.omg.PortableServer.ServantRetentionPolicyValue;
import org.omg.PortableServer.RequestProcessingPolicyValue;

import com.sun.corba.ee.spi.misc.ORBConstants ;

/**
 * @author Harold Carr
 */
public class Server
{
    static {
        // This is needed to guarantee that this test will ALWAYS use dynamic
        // RMI-IIOP.  Currently the default is dynamic when renamed to "ee",
        // but static in the default "se" packaging, and this test will
        // fail without dynamic RMI-IIOP.
        System.setProperty( ORBConstants.USE_DYNAMIC_STUB_PROPERTY, "true" ) ;
    }

    private static final String baseMsg = Server.class.getName();
    private static final String RootPOA = "RootPOA";
    private static final long SERVER_RUN_LENGTH = 1000 * 60; // 1 minute

    public static void main(String[] av)
    {
        try {
            Properties props = new Properties();
            // props.setProperty("com.sun.corba.ee.ORBDebug","subcontract,transport");

            ORB orb = ORB.init(av, props);

            POA rootPOA = (POA) orb.resolve_initial_references("RootPOA");
            Policy[] policies = new Policy[] {
                rootPOA.create_lifespan_policy( 
                    LifespanPolicyValue.PERSISTENT ),
                rootPOA.create_id_uniqueness_policy( 
                    IdUniquenessPolicyValue.UNIQUE_ID ),
                rootPOA.create_id_assignment_policy( 
                    IdAssignmentPolicyValue.USER_ID ),
                rootPOA.create_implicit_activation_policy( 
                    ImplicitActivationPolicyValue.NO_IMPLICIT_ACTIVATION ),
                rootPOA.create_servant_retention_policy(
                    ServantRetentionPolicyValue.RETAIN ),
                rootPOA.create_request_processing_policy(
                    RequestProcessingPolicyValue.USE_ACTIVE_OBJECT_MAP_ONLY )
            } ;

            POA testPOA = rootPOA.create_POA( "testPOA", rootPOA.the_POAManager(), 
                policies ) ;

            Servant servant = (Servant)
                javax.rmi.CORBA.Util.getTie(new TestServant());

            // Print this out just before activate the POAManager, because
            // activating the POAManager causes request to be handled.
            System.out.println("--------------------------------------------");
            System.out.println("Server is ready.");
            System.out.println("--------------------------------------------");

            createWithServantAndBind(Common.ReferenceName, servant, 
                                     testPOA, orb);
            // This should come AFTER we have set up the servant, otherwise a
            // request could be processed before the server is ready.
            rootPOA.the_POAManager().activate();

            // testLocalCalls( orb ) ;

            orb.run() ;
        } catch (Exception e) {
            e.printStackTrace(System.out);
            System.out.println("--------------------------------------------");
            System.out.println("!!!! Server exiting INCORRECTLY...");
            System.out.println("--------------------------------------------");
            System.exit(1);
        }
    }

    private static void testLocalCalls( ORB orb ) {
        System.out.println("--------------------------------------------");
        System.out.println("Beginning local invocation test...");
        System.out.println("--------------------------------------------");

        try {
            Hashtable env = new Hashtable();
            env.put("java.naming.corba.orb", orb);

            InitialContext initialContext = new InitialContext(env);
            Test test = (Test)PortableRemoteObject.narrow(
                initialContext.lookup(Common.ReferenceName), Test.class );

            System.out.println( "test.is_local() = " +
                ((org.omg.CORBA.portable.ObjectImpl)test)._is_local() ) ;
            
            for (int ctr=0; ctr<10; ctr++) {
                System.out.println( "Local invocation with argument " + ctr ) ;
                int result = test.echo( ctr ) ;
                if (result != ctr)
                    throw new Exception( "Result does not match argument" ) ;
            }
        } catch (Exception exc) {
            throw new RuntimeException( exc ) ;
        }

        System.out.println("--------------------------------------------");
        System.out.println("Local invocation test complete");
        System.out.println("--------------------------------------------");
    }

    public static org.omg.CORBA.Object 
        createWithServantAndBind (String  name,
                                  Servant servant,
                                  POA     poa,
                                  ORB     orb)
        throws
            Exception
    {
        byte[] id = name.getBytes();
        poa.activate_object_with_id(id, servant);
        org.omg.CORBA.Object ref = poa.id_to_reference(id);
        Common.rebind(name, ref, orb);
        return ref;
    }

} 

// End of file.
