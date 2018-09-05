/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package corba.poatest;

import org.omg.CosNaming.*;
import org.omg.CosNaming.NamingContextPackage.*;
import org.omg.CORBA.*;
import org.omg.PortableServer.*;
import org.omg.PortableServer.POA;
import HelloA.*;

import java.util.Properties;

class helloServant extends helloPOA
{
    public void shutdown()
    {
        System.err.println("In helloServant.shutdown, exiting..");
        System.exit(0);
    }

    public void sayHello()
    {
                System.out.println("\nHello world !!\n");

    }
}

public class helloServer {

    public static void main(String args[])
    {
        try{
            // create and initialize the ORB
                Properties props = new Properties();
            ORB orb = ORB.init(args, System.getProperties());

            POA rootpoa = (POA)orb.resolve_initial_references("RootPOA");
            rootpoa.the_POAManager().activate();

            POA childpoa = rootpoa.create_POA("childPOA", null,null);
            childpoa.the_POAManager().activate();
            
            // create servant and register it with the ORB
            helloServant helloRef = new helloServant();
            childpoa.activate_object(helloRef);
            
            // get the root naming context
            org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
            NamingContext ncRef = NamingContextHelper.narrow(objRef);

            org.omg.CORBA.Object ref = childpoa.servant_to_reference(helloRef);
            hello href = helloHelper.narrow(ref);

            // bind the Object Reference in Naming
            NameComponent nc = new NameComponent("Hello", "");
            NameComponent path[] = {nc};
            ncRef.rebind(path, href);

            System.out.println("Server is ready.");

            // wait for invocations from clients
            java.lang.Object sync = new java.lang.Object();
            synchronized (sync) {
                sync.wait();
                                System.out.println("helloServant contacted");
            }

        } catch (Exception e) {
            System.err.println("ERROR: " + e);
            e.printStackTrace(System.out);
            System.exit(1);
        }
    }
}

