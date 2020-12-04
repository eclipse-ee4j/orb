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

package hopper.h4549085;

import org.omg.PortableServer.*;
import org.omg.CosNaming.*;
import org.omg.CORBA.*;
import java.util.*;

public class Client
{
    public static void testMultibyteString(Tester tester)
        throws Exception
    {
        System.out.println("Testing multibyte string...");

        String multibyte = "\u3044Test of multibyte \u3044 string.\u3044";

        String result = tester.process(multibyte);

        if (!multibyte.equals(result))
            throw new Exception("Not equal: " + result);

        System.out.println("PASSED");
    }

    public static void main(String args[])
    {
        try {
            ORB orb = ORB.init(args, System.getProperties());

            // get the root naming context
            org.omg.CORBA.Object objRef = 
                orb.resolve_initial_references("NameService");
            NamingContext ncRef = NamingContextHelper.narrow(objRef);
 
            // resolve the Object Reference in Naming
            NameComponent nc = new NameComponent("Tester", "");
            NameComponent path[] = {nc};
            Tester tester = TesterHelper.narrow(ncRef.resolve(path));

            Client.testMultibyteString(tester);

            orb.shutdown(true);

        } catch (Throwable t) {
            t.printStackTrace();
            System.exit (1);
        }
    }
}
