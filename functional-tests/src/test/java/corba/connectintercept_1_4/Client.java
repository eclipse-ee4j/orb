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

//
// Created       : by Everett Anderson.
// Last Modified : 2004 Apr 14 (Wed) 19:25:53 by Harold Carr.
//

package corba.connectintercept_1_4;

import java.util.Properties;

import org.omg.CORBA.ORB;

import com.sun.corba.ee.spi.legacy.connection.ORBSocketFactory;
import com.sun.corba.ee.spi.misc.ORBConstants;

public class Client
{
    public static final String baseMsg = Client.class.getName();
    
    public static final String defaultFactoryClassName =
        //REVISIT Common.DEFAULT_FACTORY_CLASS
        "com.sun.corba.ee.impl.legacy.connection.DefaultSocketFactory";

    public static void main(String args[])
    {
        try {
            Properties props = new Properties();

            props.setProperty(Common.ORBClassKey, MyPIORB.class.getName());

            props.setProperty(ORBConstants.PI_ORB_INITIALIZER_CLASS_PREFIX +
                              ClientORBInitializer.class.getName(),
                              "dummy");

            //
            // Case 1.
            //

            System.out.println();
            System.out.println("Case 1:  Default factory");
            System.out.println();

            testFactory(args, 
                        props,
                        defaultFactoryClassName
                        );


            //
            // Case 2.
            //

            System.out.println();
            System.out.println("Case 2:  Custom factory");
            System.out.println();

            props.put(ORBConstants.LEGACY_SOCKET_FACTORY_CLASS_PROPERTY,
                      Common.CUSTOM_FACTORY_CLASS);

            testFactory(args, 
                        props,
                        Common.CUSTOM_FACTORY_CLASS);

            // 
            // Success.
            //

            System.out.println();
            System.out.println(baseMsg + ".main: Test PASSED.");

        } catch (Exception e) {
            System.out.println(baseMsg + ".main: Test FAILED: " + e) ;
            e.printStackTrace(System.out);
            System.exit (1);
        }
    }

    public static void testFactory(String args[], 
                                   Properties props,
                                   String factoryName)
        throws Exception
    {
        ORB orb = ORB.init(args, props);

        Common.upDownReset();

        resolveAndInvoke(orb, Common.serverName1);

        // Invoke on another object in same server to observe
        // already connected behavior.
        resolveAndInvoke(orb, Common.serverName2);


        // Make sure that the factory that was used matches the name given.
        ORBSocketFactory socketFactory = 
           ((com.sun.corba.ee.spi.orb.ORB)orb).getORBData().getLegacySocketFactory();
        if (socketFactory == null) {
            if (factoryName.equals(defaultFactoryClassName)) {
                // OK - default does not use socket factory any longer.
                ;
            } else {
                // Not the default - so expect a socket factory.
                throw new Exception(baseMsg + "unexpected null socketFactory");
            }
        } else {
            String orbSocketFactoryName = socketFactory.getClass().getName();
            if (! factoryName.equals(orbSocketFactoryName)) {
                throw new Exception(baseMsg + ".testFactory: "
                                    + "Wrong socket factory class: "
                                    + orbSocketFactoryName
                                    + " should be "
                                    + factoryName);
            }
        }
        orb.shutdown(false);
        orb.destroy();
    }

    public static void resolveAndInvoke (ORB orb, String name)
        throws
            Exception
    {
        ExI exIRef;

        System.out.println();
        System.out.println("BEGIN: invoke on " + name);

        exIRef = ExIHelper.narrow(resolve("First", name, orb));

        // The second resolve is to observe caching behavior.

        exIRef = ExIHelper.narrow(resolve("Second", name, orb));

        // The multiple invokes are to observe using various
        // endpoints in the component data (and to observe caching behavior).

        invoke("First", exIRef);
        invoke("Second", exIRef);
        invoke("Third", exIRef);
        invoke("Fourth", exIRef);
        invoke("Fifth", exIRef);

        System.out.println("END: invoke on " + name);
    }

    public static org.omg.CORBA.Object resolve(String msg,
                                               String name, 
                                               ORB orb)
        throws Exception
    {
        // List initial references.

        System.out.println();
        System.out.println("BEGIN: " + msg + " list_initial_references.");

        String services[] = orb.list_initial_services();
        for (int i = 0; i < services.length; i++) {
            System.out.print(" " + services[i]);
        }
        System.out.println();

        System.out.println("END: " + msg + " list_initial_references.");


        // Resolve.

        System.out.println();
        System.out.println("BEGIN: " + msg + " resolve.");

        org.omg.CORBA.Object ref
            = ExIHelper.narrow(Common.getNameService(orb)
                               .resolve(Common.makeNameComponent(name)));

        System.out.println("END: " + msg + " resolve.");
        return ref;
    }

    public static void invoke(String msg, ExI exIRef)
    {
        System.out.println();
        System.out.println("BEGIN: " + msg + " invocation.");

        exIRef.sayHello();

        System.out.println("END: " + msg + " invocation.");
    }
}

// End of file.
