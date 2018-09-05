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
// Created       : by Everett Anderson.
// Last Modified : 2004 Apr 14 (Wed) 19:26:04 by Harold Carr.
//

package corba.connectintercept_1_4;

import java.util.Properties ;

import org.omg.CORBA.ORB;
import org.omg.CORBA.Policy;
import org.omg.PortableServer.LifespanPolicyValue;
import org.omg.PortableServer.POA;

import com.sun.corba.ee.spi.misc.ORBConstants;

class ExIServant extends ExIPOA
{
    public ORB orb;

    public ExIServant(ORB orb) 
    {
        this.orb = orb;
    }

    public String sayHello()
    {
        return "Hello world!";
    }
}

public class ServerCommon
{
    public static final String baseMsg = ServerCommon.class.getName();

    public static ORB orb;
    public static POA rootPoa;
    public static POA childPoa;

    // The same server code is used conditionally for both
    // persistent and transient servers.
    public static boolean isTransient;


    public static void main(String av[])
    {
        if (av[0].equals(Common.Transient)) {
            isTransient = true;
        } else if (av[0].equals(Common.Persistent)) {
            isTransient = false;
        } else {
            System.out.println(baseMsg + ".main: unknown: " + av[0]);
            System.exit(-1);
        }
            
        try {

            Properties props = System.getProperties();

            props.setProperty(Common.ORBClassKey, MyPIORB.class.getName());

            props.setProperty(ORBConstants.PI_ORB_INITIALIZER_CLASS_PREFIX +
                              ServerORBInitializer.class.getName(),
                              "dummy");

            props.setProperty(ORBConstants.LEGACY_SOCKET_FACTORY_CLASS_PROPERTY,
                              Common.CUSTOM_FACTORY_CLASS);

            String value;
            if (isTransient) {
                // It makes sense to assign specific ports for
                // transient servers.
                value =
                    Common.MyType1 + ":" + Common.MyType1TransientPort + "," +
                    Common.MyType2 + ":" + Common.MyType2TransientPort + "," +
                    Common.MyType3 + ":" + Common.MyType3TransientPort;
            } else {
                // It makes sense to assign emphemeral ports
                // to persistent servers since the ORBD will most
                // likely be assigned the fixed ports.
                value =
                    Common.MyType1 + ":" + Common.MyType1PersistentPort + "," +
                    Common.MyType2 + ":" + Common.MyType2PersistentPort + "," +
                    Common.MyType3 + ":" + Common.MyType3PersistentPort;
            }
            props.setProperty(ORBConstants.LISTEN_SOCKET_PROPERTY, value);

            // REVISIT: not sure why I have to explicitly set these here
            // but not in other tests.
            props.setProperty(ORBConstants.INITIAL_PORT_PROPERTY, "1049");

            orb = ORB.init(av, props);

            createAndBind(Common.serverName1);
            createAndBind(Common.serverName2);
      
            System.out.println ("Server is ready.");

            orb.run();
            
        } catch (Exception e) {
            System.out.println(baseMsg + ".main: ERROR: " + e);
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
            rootPoa =
              (POA)orb.resolve_initial_references(ORBConstants.ROOT_POA_NAME);
            rootPoa.the_POAManager().activate();

            // Create POAs.

            Policy[] policies = new Policy[1];

            // Create child POA
            policies[0] =
                isTransient ?
                rootPoa.create_lifespan_policy(LifespanPolicyValue.TRANSIENT):
                rootPoa.create_lifespan_policy(LifespanPolicyValue.PERSISTENT);
            childPoa =rootPoa.create_POA("childPoa", null, policies);
            childPoa.the_POAManager().activate();
        }

        // REVISIT - bind a root and transient.

        // create servant and register it with the ORB
        ExIServant exIServant = new ExIServant(orb);
        byte[] id = childPoa.activate_object(exIServant);
        org.omg.CORBA.Object ref = childPoa.id_to_reference(id);

        Common.getNameService(orb).rebind(Common.makeNameComponent(name), ref);
    }
}

// End of file.
