/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
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
