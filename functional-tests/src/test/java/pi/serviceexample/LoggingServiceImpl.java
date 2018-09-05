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
// Created       : 2001 May 23 (Wed) 11:57:05 by Harold Carr.
// Last Modified : 2001 Sep 24 (Mon) 21:26:29 by Harold Carr.
//

package pi.serviceexample;

import org.omg.CORBA.ORB;
import org.omg.CosNaming.*;
import org.omg.CosNaming.NamingContextPackage.*;
import org.omg.PortableServer.POA;

import java.util.Properties;

class LoggingServiceImpl
    extends LoggingServicePOA
{
    public static ORB orb;

    //
    // The IDL operations.
    //

    public void log(String a1)
    {
        System.out.println(a1);
    }

    //
    // The server.
    //

    public static void main(String[] av)
    {
        try {
            if (orb == null) {
                orb = ORB.init(av, null);
            }
            
            POA rootPOA =  (POA) orb.resolve_initial_references("RootPOA");
            rootPOA.the_POAManager().activate();
            
            byte[] objectId =
                rootPOA.activate_object(new LoggingServiceImpl());
            org.omg.CORBA.Object ref = rootPOA.id_to_reference(objectId);

            NamingContext nameService = 
                NamingContextHelper.narrow(
                    orb.resolve_initial_references("NameService"));
            NameComponent path[] =
                { new NameComponent("LoggingService", "") };
            nameService.rebind(path, ref);

            // Only relevant for colocated example.
            ColocatedServers.colocatedBootstrapDone = true;

            System.out.println("Server is ready.");

            orb.run();

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
        System.exit(0);
    }
}

// End of file.

