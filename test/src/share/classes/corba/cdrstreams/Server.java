/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package corba.cdrstreams;

import org.omg.CORBA.portable.*;
import javax.rmi.PortableRemoteObject;
import org.omg.CosNaming.*;
import org.omg.CosNaming.NamingContextPackage.*;
import org.omg.CORBA.*;
import java.util.Properties;
import org.omg.PortableServer.*;

import com.sun.corba.ee.spi.misc.ORBConstants ;

public class Server
{
    public static void main(String args[])
    {
        try {
            Properties props = new Properties( System.getProperties() ) ;
            props.setProperty( ORBConstants.DEBUG_PROPERTY, 
                "subcontract,giop,transport" ) ;
            ORB orb = ORB.init(args, props );
      
            // Get rootPOA
            POA rootPOA = (POA)orb.resolve_initial_references("RootPOA");
            rootPOA.the_POAManager().activate();

            GraphProcessorImpl impl = new GraphProcessorImpl();
            javax.rmi.CORBA.Tie tie = javax.rmi.CORBA.Util.getTie( impl ) ; 

            byte[] id = rootPOA.activate_object( 
                                                 (org.omg.PortableServer.Servant)tie ) ;
            org.omg.CORBA.Object obj = rootPOA.id_to_reference( id ) ;

            // get the root naming context
            org.omg.CORBA.Object objRef = 
                orb.resolve_initial_references("NameService");
            NamingContext ncRef = NamingContextHelper.narrow(objRef);
      
            // bind the Object Reference in Naming
            NameComponent nc = new NameComponent("GraphProcessor", "");
            NameComponent path[] = {nc};
            
            ncRef.rebind(path, obj);
            
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
