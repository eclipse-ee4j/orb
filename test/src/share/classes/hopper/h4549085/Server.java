/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package hopper.h4549085;

import org.omg.PortableServer.*;
import org.omg.CORBA.*;
import org.omg.CosNaming.*;

public class Server extends TesterPOA 
{
    public String process(String input) {
        return input;
    }

    public static void main(String args[]) {
        try {
            ORB orb = ORB.init(args, System.getProperties());

            // Get rootPOA
            POA rootPOA = (POA)orb.resolve_initial_references("RootPOA");
            rootPOA.the_POAManager().activate();
      
            // create servant and register it with the ORB
            Server server = new Server();
      
            byte[] id = rootPOA.activate_object(server);
      
            // get the root naming context
            org.omg.CORBA.Object objRef = 
                orb.resolve_initial_references("NameService");
            NamingContext ncRef = NamingContextHelper.narrow(objRef);
      
            // bind the Object Reference in Naming
            NameComponent nc = new NameComponent("Tester", "");
            NameComponent path[] = {nc};
      
            org.omg.CORBA.Object ref = rootPOA.id_to_reference(id);
            
            ncRef.rebind(path, ref);
            
            // Emit the handshake the test framework expects
            // (can be changed in Options by the running test)
            System.out.println ("Server is ready.");

            // Wait for clients
            orb.run();

        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }
}
                
