/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package hopper.h4661596;

import corba.framework.CORBATest;
import java.util.*;
import org.omg.CORBA.TCKind;
import org.omg.CORBA.TypeCode;
import org.omg.CORBA.WStringValueHelper;

/**
 * Checks to make sure WStringValueHelper.type() returns
 * a TypeCode reflecting a boxed wstring.
 *
 * Since we can't get the WStringValueHelper from rip-int
 * without Xbootclasspath, this will only run on 1.4.1 or
 * greater workspaces.
 */
public class WStringValueHelperTest extends CORBATest
{
    public static final String INDENT = "      ";

    // The actual test:

    public void checkWStringValueHelper() throws Exception {
        System.out.print(INDENT
                         + "  Checking WStringValueHelper from JDK...");

        TypeCode tc = WStringValueHelper.type();

        while (tc.kind().equals(TCKind.tk_alias))
            tc = tc.content_type();

        if (!tc.kind().equals(TCKind.tk_value_box) ||
            !tc.content_type().kind().equals(TCKind.tk_wstring)) {
            Exception exc = new Exception("Bad TypeCode from WStringValueHelper: "
                                + " kind: " 
                                + tc.content_type().kind().value());
            throw exc ;
        }

        System.out.println("PASSED");
    }


    // This has nothing to do with the logic of the 
    // test.  It is only to allow us to only run this on
    // hopper or greater JDKs.
    public boolean jdkIsHopperOrGreater() throws Exception {
        
        // Should probably use Merlin's new perl-like
        // feature.

        try {

            String version 
                = System.getProperty("java.version");

            System.out.println(INDENT
                               + " JDK version: " + version);

            StringTokenizer stok
                = new StringTokenizer(version, ". -_b", false);

            int major = Integer.parseInt(stok.nextToken());
            if (major > 1)
                return true;

            if (!stok.hasMoreTokens())
                return false;

            int dot1 = Integer.parseInt(stok.nextToken());
            if (dot1 > 4)
                return true;

            if (!stok.hasMoreTokens())
                return false;

            int dot2 = Integer.parseInt(stok.nextToken());
            if (dot2 == 0)
                return false;

            return true;

        } catch (NoSuchElementException nsee) {
            throw new Exception("Error determining version: "
                                + nsee);
        } catch (NumberFormatException nfe) {
            throw new Exception("Error determining version: "
                                + nfe);
        }
    }

    protected void doTest() throws Throwable {
        System.out.println();

        System.out.println(INDENT
                           + "Verifying JDK is Hopper or greater...");
        try {
            if (!jdkIsHopperOrGreater()) {
                System.out.println(INDENT
                                   + "* WARNING: "
                                   + " This test can only be run on Hopper or greater JDKs.  Skipping test.");
                return;
            }
                
        } catch (Exception ex) {
            System.out.println(INDENT
                               + "* Error determing JDK version.  Can only run on Hopper or greater JDKs.  Skipping test.  Error was: " + ex);
            return;
        }

        checkWStringValueHelper();
    }
}
