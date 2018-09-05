/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package corba.unarrow;

import org.omg.CosNaming.*;
import org.omg.CosNaming.NamingContextPackage.*;
import org.omg.CORBA.*;
import java.util.Properties ;
import org.omg.PortableServer.*;

/**
 * Servant implementation
 */
class HelloServant extends HelloPOA
{
    public String sayHello()
    {
        return "Hello world!";
    }
}

class ByeServant extends ByePOA
{
    public String sayBye()
    {
        return "Bye world!";
    }
}


public class Server
{
    public static void main(String args[])
    {
        try {
      
            ORB orb = ORB.init(args, System.getProperties());
      
            // Get rootPOA
            POA rootPOA = (POA)orb.resolve_initial_references("RootPOA");
            rootPOA.the_POAManager().activate();
      
            // create servants and register it with the ORB
            HelloServant helloRef = new HelloServant();
            ByeServant byeRef = new ByeServant();
      
            byte[] helloId = rootPOA.activate_object(helloRef);
      
            // get the root naming context
            org.omg.CORBA.Object objRef = 
                orb.resolve_initial_references("NameService");
            NamingContext ncRef = NamingContextHelper.narrow(objRef);
      
            // bind the Object Reference in Naming
            NameComponent nc = new NameComponent("Hello", "");
            NameComponent path[] = {nc};
      
            org.omg.CORBA.Object ref = rootPOA.id_to_reference(helloId);
            
            ncRef.rebind(path, ref);
            
            byte[] byeId = rootPOA.activate_object(byeRef);

            // bind the Object Reference in Naming
            nc = new NameComponent("Bye", "");
            path[0] = nc;
      
            ref = rootPOA.id_to_reference(byeId);
            
            ncRef.rebind(path, ref);

            // Emit the handshake the test framework expects
            // (can be changed in Options by the running test)
            System.out.println ("Server is ready.");

            // Wait for clients
            orb.run();
            
        } catch (Exception e) {
            System.err.println("ERROR: " + e);
            e.printStackTrace(System.out);

            // Make sure to exit with a value greater than 0 on
            // error.
            System.exit(1);
        }
    }
}
