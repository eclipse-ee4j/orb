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

