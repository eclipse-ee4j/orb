/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package corba.framework;

import org.omg.CORBA.ORB;
import org.omg.CORBA.Object;
import org.omg.CORBA.ORBPackage.InvalidName;

public class ResolveNS {

    private static boolean orbd = false;

    public static void main(String[] args) {
        if (args[0].equals("orbd")) {
            orbd = true;
        }

        try {
            org.omg.CORBA.Object nameService = null;
            org.omg.CORBA.Object tnameService = null;

            ORB orb = ORB.init(args, System.getProperties());
            try {
                nameService = orb.resolve_initial_references("NameService");
            } catch (InvalidName in) { }

            try {
                tnameService =
                    orb.resolve_initial_references("TNameService");
            } catch (InvalidName in) { }

            if ((nameService == null) || (orbd && (tnameService == null))
                || (!orbd && (tnameService != null))) {
                System.exit(1);
            }
            System.exit(0);
        } catch (Throwable t) {
            System.exit(2);
        }
    }
}
