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
