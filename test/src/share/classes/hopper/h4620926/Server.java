/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package hopper.h4620926;

import org.omg.CORBA.*;
import org.omg.PortableServer.*;
import org.omg.CosNaming.*;
import test.*;
import java.util.Properties ;

class HelloServant extends HelloPOA {
    public String sayHello() {
        return "Hello";
    }
}

public class Server {

    public static int delay = 100;

    public static void main(String[] args) {

        try {

            // try {
                // delay = Integer.parseInt(args[0]);
            // } catch (Exception e) { }
            
            Properties props = new Properties() ;
            props.setProperty( "com.sun.corba.ee.ORBDebug", "poa" ) ;
            ORB orb = ORB.init(args, props);

            POA rootPOA = POAHelper.narrow(
                orb.resolve_initial_references("RootPOA"));
            Policy[] policy = new Policy[2];
            policy[0] = rootPOA.create_request_processing_policy(
                RequestProcessingPolicyValue.USE_SERVANT_MANAGER);
            policy[1] = rootPOA.create_id_assignment_policy(
                IdAssignmentPolicyValue.USER_ID);

            POA childPOA = rootPOA.create_POA("Child", null, policy);
            childPOA.set_servant_manager(new MyServantActivator());
            System.out.println("Set servant manager");
        
            String str = "ABCRef";
            org.omg.CORBA.Object obj = childPOA.create_reference_with_id(
                str.getBytes(), "IDL:test/Hello:1.0");
            childPOA.the_POAManager().activate();

            Hello ref = HelloHelper.narrow(obj);
            NamingContext namingContext = NamingContextHelper.narrow(
            orb.resolve_initial_references("NameService"));
            NameComponent[] name = { new NameComponent("Hello", "") };

            namingContext.rebind(name, ref);
            System.out.println("Servant registered");

            System.out.println("Server is ready.");

            orb.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class MyServantActivator extends LocalObject implements ServantActivator {

    public Servant incarnate(byte[] oid, POA adapter) {

        System.out.println("Incarnating Object - " + new String(oid) +
                           " in POA - " + adapter.the_name());
        try {
            System.out.println("Sleeping for " + Server.delay + "msecs");
            Thread.sleep(Server.delay);
        } catch (Exception e) { }
        return new HelloServant();
    }

    public void etherealize(byte[] oid, POA adapter, Servant servant,
                            boolean cleanUpInProgress, 
                            boolean remaingActivations) {
        System.out.println("Etherealizing Object ");
    }
}
