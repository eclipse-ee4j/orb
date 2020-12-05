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
