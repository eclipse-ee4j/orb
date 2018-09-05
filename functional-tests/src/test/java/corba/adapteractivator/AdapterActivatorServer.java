/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package corba.adapteractivator ;

import org.omg.PortableServer.POA ;
import org.omg.PortableServer.AdapterActivator ;
import org.omg.PortableServer.LifespanPolicyValue ;
import org.omg.PortableServer.IdAssignmentPolicyValue ;

import corba.framework.RTMConstants;
import org.omg.CORBA.LocalObject;
import org.omg.CORBA.ORB;
import org.omg.CORBA.Policy;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContext;
import org.omg.CosNaming.NamingContextHelper;

public class AdapterActivatorServer {

        private POA rootPoa,poa1,poa2;
        private HelloImpl serv1=null;
        private HelloImpl serv2=null;
        private byte[] oid1 = "Object1".getBytes() ;
        private byte[] oid2 = "Object2".getBytes() ;

        private ORB orb=null;

    public AdapterActivatorServer(String[] args) {

                System.out.println("AdapterActivatorServer : execute");

                try {

                        orb = ORB.init(args,System.getProperties());
                        rootPoa = getRootPoa(orb);

                        // Registering the AdapterActivator      with rootpoa that creates poa1
                        System.out.println("Registering the AdapterActivator with rootpoa that creates poa1 ");
                        rootPoa.the_activator(new Poa1AdapterActivator());

                        // Creating HelloServant
                        serv1 = new HelloImpl();
                        serv2 = new HelloImpl();
                        System.out.println("AdapterActivatorServer: HelloImpl object created !" );

                        // Creating CloseServant
                        CloseImpl closeServ = new CloseImpl(orb);
                        System.out.println("AdapterActivatorServer: CloseImpl object created !" );

                        // Activating CloseServant with RootPOA
                        rootPoa.activate_object(closeServ);

                        // creating poa1 and activating poa1
                        createPoa1();

                        // creating poa2 and activating poa2
                        createPoa2();

                        //create a object reference from the servant object
                        System.out.println("Creating Object reference for Helloservant object");
                        org.omg.CORBA.Object helloRef1 = poa1.servant_to_reference(serv1);
                        org.omg.CORBA.Object helloRef2 = poa2.servant_to_reference(serv2);

                        System.out.println("Creating Object reference for Closeservant object");
                        org.omg.CORBA.Object closeRef = rootPoa.servant_to_reference(closeServ);

                        // Get root Naming Context
                        org.omg.CORBA.Object obj = orb.resolve_initial_references("NameService");
                        NamingContext rootContext = NamingContextHelper.narrow(obj);

                        // Create a Name Components and bind to root Context
                        System.out.println("AdapterActivatorServer : publish Hello activated with Poa1");
                        NameComponent nc = new NameComponent("HelloServer1","");
                        NameComponent name[] =  {nc};
                        rootContext.rebind(name,helloRef1);

                        System.out.println("AdapterActivatorServer : publish Hello activated with Poa2");
                        NameComponent nc1 = new NameComponent("HelloServer2","");
                        NameComponent name1[] =         {nc1};
                        rootContext.rebind(name1,helloRef2);

                        System.out.println("AdapterActivatorServer : publish Close activated with RootPOA");
                        NameComponent nc2 = new NameComponent("CloseServer","");
                        NameComponent name2[] =         {nc2};
                        rootContext.rebind(name2,closeRef);

                        // destroying the Poa1
                        poa1.destroy(true,true);

                        poa2.destroy(true,true);

                        // Activating Poa
                        System.out.println("Activating the Root POA");
                        rootPoa.the_POAManager().activate();

                        //Force the server to wait for client requests
                        System.out.println(RTMConstants.SERVER_READY);
                        orb.run();
                }
                catch(org.omg.CORBA.SystemException ex) {
                        System.out.println(" Unexpected exception "+ex.toString());
                        return;
                }
                catch (Exception e) {
                        System.out.println("Unexpected Exception" + e.toString() + "\n");
                        return;
                }
        }


        private void createPoa1() {
                Policy[] poaPolicies = new Policy[] {
                    rootPoa.create_lifespan_policy( LifespanPolicyValue.PERSISTENT ),
                    rootPoa.create_id_assignment_policy( IdAssignmentPolicyValue.USER_ID )
                } ;

                try {
                        System.out.println("Creating poa1 on rootpoa");
                        poa1=rootPoa.create_POA("Poa1",null,poaPolicies);
                        System.out.println("Created Poa1");
                        poa1.activate_object_with_id(oid1, serv1);
                        poa1.the_POAManager().activate();
                }
                catch (Exception e) {
                        System.out.println("Unexpected Exception" + e.toString());
                }

        } // createPoa1()

        private void createPoa2() {
                Policy[] poaPolicies = new Policy[] {
                    rootPoa.create_lifespan_policy( LifespanPolicyValue.PERSISTENT ),
                    rootPoa.create_id_assignment_policy( IdAssignmentPolicyValue.USER_ID )
                } ;

                try {
                        System.out.println("Creating poa2 on rootpoa");
                        poa2=rootPoa.create_POA("Poa2",null,poaPolicies);
                        System.out.println("Created Poa2");
                        poa2.activate_object_with_id(oid2, serv2);
                        poa2.the_POAManager().activate();
                }
                catch (Exception e) {
                        System.out.println("Unexpected Exception" + e.toString());
                }

        } // createPoa2()

        private POA getRootPoa(ORB orb) {

                POA poa=null;
                try {
                        System.out.println("Getting Root POA from ORB");
                        poa =(POA)orb.resolve_initial_references("RootPOA");

                        if (poa == null)
                                System.out.println("POA is null");
                }
                catch(org.omg.CORBA.ORBPackage.InvalidName i) {
                        System.out.println("Unexpected exception in obtaining RootPoa"+i.toString());
                }
                catch (Exception e) {
                        System.out.println("Unexpected Exception" + e.toString());
                }
                return poa;
        } // getRootPoa()


        public static void main( String [] args ) {
                AdapterActivatorServer server = new AdapterActivatorServer(args) ;
    } // main()


        class Poa1AdapterActivator extends LocalObject implements AdapterActivator {

                public Poa1AdapterActivator() {

                }

                // Unknown adapter creates Poa if Poa name is Poa1 otherwise it return false

                public boolean unknown_adapter(POA parent,String name){
                        try {
                                if(name.equals("Poa1")) {
                                        System.out.println("Creating Poa1 in unknown_adapter method");
                                        createPoa1();
                                        return true;
                                }
                        }
                        catch(org.omg.CORBA.SystemException ex) {
                                System.out.println("Unexpected System Exception " + ex.toString());
                                return false;
                        }
                        catch(Exception ex){
                                System.out.println("Unexpected Exception " + ex.toString());
                                return false;
                        }
                        return false;
                }
        }
}
