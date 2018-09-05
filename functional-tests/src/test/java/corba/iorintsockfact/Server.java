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
// Created       : 2002 Jul 19 (Fri) 14:48:59 by Harold Carr.
// Last Modified : 2002 Jul 22 (Mon) 12:05:48 by Harold Carr.
//

package corba.iorintsockfact;

import java.util.Properties ;

import org.omg.CORBA.ORB;
import org.omg.CORBA.Policy;
import org.omg.PortableServer.LifespanPolicyValue;
import org.omg.PortableServer.POA;

class IServant extends IPOA
{
    public IServant()
    {
    }

    public String m(String x)
    {
        return "Server echoes: " + x;
    }
}

/**
 * @author Harold Carr
 */
public class Server
{
    public static final String baseMsg = Common.class.getName();

    public static ORB orb;
    public static POA rootPoa;
    public static POA childPoa;

    public static void main(String av[])
    {
        try {

            Properties props = System.getProperties();

            props.setProperty("org.omg.PortableInterceptor.ORBInitializerClass." + ServerORBInitializer.class.getName(),
                              "dummy");

            props.put(Common.SOCKET_FACTORY_CLASS_PROPERTY,
                      Common.CUSTOM_FACTORY_CLASS);

            orb = ORB.init(av, props);

            createAndBind(Common.serverName1);
      
            System.out.println ("Server is ready.");

            orb.run();
            
        } catch (Exception e) {
            System.out.println(baseMsg + e);
            e.printStackTrace(System.out);
            System.exit(1);
        }
    }

    public static void createAndBind (String name)
        throws
            Exception
    {
        if (rootPoa == null) {

            // Get rootPOA

            rootPoa = (POA)
                orb.resolve_initial_references("RootPOA");
            rootPoa.the_POAManager().activate();

            // Create child POAs.

            Policy[] policies = new Policy[1];

            // Create child POA
            policies[0] =
                rootPoa.create_lifespan_policy(LifespanPolicyValue.TRANSIENT);
            childPoa = rootPoa.create_POA("childPoa", null, policies);
            childPoa.the_POAManager().activate();
        }

        // create servant and register it with the ORB

        IServant iServant = new IServant();
        byte[] id = childPoa.activate_object(iServant);
        org.omg.CORBA.Object ref = childPoa.id_to_reference(id);

        Common.getNameService(orb).rebind(Common.makeNameComponent(name), ref);
    }
}

// End of file.
