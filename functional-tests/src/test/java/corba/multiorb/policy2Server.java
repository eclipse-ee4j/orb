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

package corba.multiorb;

import java.util.Properties;
import org.omg.CosNaming.NamingContextHelper;
import org.omg.PortableServer.IdAssignmentPolicyValue;
import org.omg.PortableServer.ThreadPolicyValue;
import org.omg.PortableServer.LifespanPolicyValue;
import org.omg.PortableServer.IdUniquenessPolicyValue;
import org.omg.PortableServer.ServantRetentionPolicyValue;
import org.omg.PortableServer.RequestProcessingPolicyValue;
import org.omg.PortableServer.ImplicitActivationPolicyValue;
import org.omg.CORBA.Policy;
import org.omg.PortableServer.Servant;

import com.sun.corba.ee.spi.misc.ORBConstants ;
import examples.policy_2POA;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContext;
import org.omg.PortableServer.POA;

class policy2_servantA extends policy_2POA
{
        
        private int countValue;
        
        public policy2_servantA() 
        {
                countValue = 0;
        }

        /**
         * Implementation of the servant object.
         * The funtion intakes no parameter
         * and returns an int value incremented by one.
         */
        
        public int increment()
        {
                return ++countValue;
        }
}

class policy2_servantB extends policy_2POA
{
        
        private int countValue;
        
        public policy2_servantB() 
        {
                countValue = 1000;
        }

        /**
         * Implementation of the servant object.
         * The funtion intakes no parameter
         * and returns an int value incremented by one.
         */
        
        public int increment()
        {
                return ++countValue;
        }
}

public class policy2Server
{
        
        private static final String msgPassed = "policy_2: **PASSED**";
        
        private static final String msgFailed = "policy_2: **FAILED**";
        
        public static void main( String args[] )
        {
                try
                {
                        Properties prop = new Properties();
                        prop.setProperty("org.omg.CORBA.ORBClass", System.getProperty("org.omg.CORBA.ORBClass"));
//                      System.out.println( "POLICIES : ORB_CTRL_MODEL,PERSISTENT,UNIQUE_ID,SYSTEM_ID,"
//                          + "RETAIN,USE_ACTIVE_OBJECT_MAP_ONLY,NO_IMPLICIT_ACTIVATION" );
                        prop.setProperty( ORBConstants.OLD_ORB_ID_PROPERTY, "sunorb1");
                        prop.setProperty( ORBConstants.ORB_SERVER_ID_PROPERTY, "257");
                        prop.setProperty( ORBConstants.PERSISTENT_SERVER_PORT_PROPERTY, "10032");
//                      System.out.println(ORBConstants.OLD_ORB_ID_PROPERTY 
//                          + prop.getProperty(ORBConstants.OLD_ORB_ID_PROPERTY));
                        ORB orb1 = ORB.init( args, prop );
                        
                        prop = new Properties();
                        prop.setProperty("org.omg.CORBA.ORBClass", System.getProperty("org.omg.CORBA.ORBClass"));
                        prop.setProperty( ORBConstants.OLD_ORB_ID_PROPERTY, "sunorb2");
                        prop.setProperty( ORBConstants.ORB_SERVER_ID_PROPERTY, "257");
                        prop.setProperty( ORBConstants.PERSISTENT_SERVER_PORT_PROPERTY, "20032");
//                      System.out.println(ORBConstants.OLD_ORB_ID_PROPERTY 
//                          + prop.getProperty(ORBConstants.OLD_ORB_ID_PROPERTY));
                        ORB orb2 = ORB.init( args, prop );

                        //create the rootPOA and activate it as first element of the array
                        // creating and initializing POAs/Objects in First ORB

                        policy2_servantA acs1 = new policy2_servantA();
                        policy2_servantB acs2 = new policy2_servantB();
                        createAndPublishObjects(orb1, acs1, "Object1");
                        createAndPublishObjects(orb2, acs2, "Object2");
                        System.out.println("Server is ready.");
                        java.lang.Object sync = new java.lang.Object();
                        synchronized( sync )
                        {
                                sync.wait();
                        }
                }
                catch( Exception exp )
                {
                        exp.printStackTrace();
                        System.out.println( msgFailed + "\n" );
                }

        }

        public static void createAndPublishObjects(org.omg.CORBA.ORB orb, Servant servantObj, String Name) throws Exception
        {

                        POA rootPoa = (POA)orb.resolve_initial_references( "RootPOA" );
                        rootPoa.the_POAManager().activate();
                        
                        // Create a POA 
                        POA childpoa = null;
                        
                        // create policy for the new POA.
                        Policy[] policy = new Policy[7];
                        policy[0] = rootPoa.create_id_assignment_policy( IdAssignmentPolicyValue.SYSTEM_ID );
                        policy[1] = rootPoa.create_thread_policy( ThreadPolicyValue.ORB_CTRL_MODEL );
                        policy[2] = rootPoa.create_lifespan_policy( LifespanPolicyValue.PERSISTENT );
                        policy[3] = rootPoa.create_id_uniqueness_policy( IdUniquenessPolicyValue.UNIQUE_ID );
                        policy[4] = rootPoa.create_servant_retention_policy( ServantRetentionPolicyValue.RETAIN );
                        policy[5] = rootPoa.create_request_processing_policy( RequestProcessingPolicyValue.USE_ACTIVE_OBJECT_MAP_ONLY );
                        policy[6] = rootPoa.create_implicit_activation_policy( ImplicitActivationPolicyValue.NO_IMPLICIT_ACTIVATION );
                        
                        // get the root naming context
                        org.omg.CORBA.Object obj = orb.resolve_initial_references( "NameService" );
                        NamingContext rootContext = NamingContextHelper.narrow( obj );
                        
                        // create the child poa and activate it
                        childpoa = rootPoa.create_POA( "policy_2", null, policy );
                        childpoa.the_POAManager().activate();
                        childpoa.activate_object( (Servant)servantObj );
                        
                        // Binding to NamingService
                        System.out.println( "Binding to NamingService" );
                        NameComponent nc = new NameComponent( Name, "" );
                        NameComponent path[] = 
                        {
                                nc
                        };
                        org.omg.CORBA.Object obj1 = childpoa.servant_to_reference( (Servant)servantObj );
                        rootContext.rebind( path, obj1 );



        }

        public static void shutdown()
        {
        }

        public static void install()
        {
        }

        public static void uninstall()
        {
        }
}
