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
// Created       : 2002 Oct 16 (Wed) 08:32:02 by Harold Carr.
// Last Modified : 2003 Mar 17 (Mon) 20:51:40 by Harold Carr.
//

package mantis.m4764130;

import java.util.Properties;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.*;
import org.omg.PortableServer.POA;
import com.sun.corba.ee.spi.misc.ORBConstants;

class HelloImpl
    extends
        HelloPOA
{
    public HelloImpl() 
    {
    }

    public String hello(String x)
    {
        return x;
    }
}

public class Server
{
    public static void main(String[] args)
    {
        try{
            Properties props = new Properties();
            props.setProperty(ORBConstants.PI_ORB_INITIALIZER_CLASS_PREFIX +
                              Interceptor.class.getName(),
                              "dummy");
            ORB orb = ORB.init(args, props);

            // Get rootPOA
            POA rootPOA = (POA)orb.resolve_initial_references("RootPOA");
            rootPOA.the_POAManager().activate();

            HelloImpl hello = new HelloImpl();
            byte[] id = rootPOA.activate_object(hello);
            org.omg.CORBA.Object ref = rootPOA.id_to_reference(id);

            NamingContext namingContext = 
                NamingContextHelper.narrow(orb.resolve_initial_references(
                    "NameService"));
            NameComponent nc = new NameComponent("Server", "");
            NameComponent path[] = { nc };
            namingContext.rebind( path , ref );

            System.out.println("Server is ready.");

            orb.run();

        } catch (Exception ex) {
            System.err.println("ERROR: " + ex);
            ex.printStackTrace();
            System.exit(1);
        }
    }
}

// End of file.
