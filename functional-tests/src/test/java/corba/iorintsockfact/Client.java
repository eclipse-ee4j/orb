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
// Created       : 2002 Jul 19 (Fri) 14:50:37 by Harold Carr.
// Last Modified : 2003 Jun 03 (Tue) 18:11:37 by Harold Carr.
//

package corba.iorintsockfact;

import java.util.Properties;

import org.omg.CORBA.ORB;

/**
 * @author Harold Carr
 */
public class Client
{
    public static final String baseMsg = Client.class.getName();

    public static boolean foundAlternateIIOPAddressComponent = false;
    
    public static void main(String args[])
    {
        try {
            Properties props = new Properties();

            props.put(Common.SOCKET_FACTORY_CLASS_PROPERTY,
                      Common.CUSTOM_FACTORY_CLASS);

            ORB orb = ORB.init(args, props);

            I iRef =
                IHelper.narrow(
                    Common.getNameService(orb)
                    .resolve(Common.makeNameComponent(Common.serverName1)));

            System.out.println(iRef.m("Hello"));

            if (! foundAlternateIIOPAddressComponent) {
                System.out.println("DID NOT FIND AlternateIIOPAddressComponent");
                System.exit(1);
            }

            orb.shutdown(false);
            orb.destroy();

            System.out.println();
            System.out.println(baseMsg + ".main: Test complete.");

        } catch (Exception e) {
            System.out.println(baseMsg + e) ;
            e.printStackTrace(System.out);
            System.exit (1);
        }
    }
}

// End of file.
