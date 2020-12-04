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
// Created       : 2003 Dec 11 (Thu) 11:03:27 by Harold Carr.
// Last Modified : 2003 Dec 19 (Fri) 10:36:14 by Harold Carr.
//

package corba.legacyorbclasses;

import java.util.Properties;
import corba.framework.Controller;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.*;
import org.omg.CosNaming.NamingContextPackage.*;
import com.sun.corba.ee.spi.misc.ORBConstants;

public class Client 
{
    public static final String baseMsg = Client.class.getName();
    public static final String main = baseMsg + ".main";

    public static final String ORBClassKey = 
        "org.omg.CORBA.ORBClass";
    public static final String ORBSingletonClassKey =
        "org.omg.CORBA.ORBSingletonClass";

    public static void main(String av[])
    {
        try {
            // ORBSingletons
            // Note: this negative test must come first, since you
            // can only create one singleton in a JVM.
            createORB(false, false,  "x", null);
            createORB(true,  false,
                      "com.sun.corba.ee.internal.corba.ORBSingleton",
                      com.sun.corba.ee.internal.corba.ORBSingleton.class);

            // FULL ORBs
            createORB(false, true,  "x", null);         
            createORB(true,  true,  
                      "com.sun.corba.ee.impl.orb.ORBImpl",
                      com.sun.corba.ee.impl.orb.ORBImpl.class);
            createORB(true,  true,
                      "com.sun.corba.ee.internal.Interceptors.PIORB",
                      com.sun.corba.ee.internal.Interceptors.PIORB.class);
            createORB(true,  true,  
                      "com.sun.corba.ee.internal.POA.POAORB",
                      com.sun.corba.ee.internal.POA.POAORB.class);
            createORB(true,  true,  
                      "com.sun.corba.ee.internal.iiop.ORB",
                      com.sun.corba.ee.internal.iiop.ORB.class);

            System.out.println("Test PASSED.");

        } catch (Throwable t) {
            System.out.println(main + ": unexpected exception: " + t);
            System.out.println("Test FAILED.");
            System.exit(1);
        }
        System.exit(Controller.SUCCESS);
    }

    private static void createORB(boolean shouldExist, 
                                  boolean isFullORB,
                                  String className,
                                  Class clazz)
        throws
            Exception
    {
        ORB orb = null;
        creating(className);
        try {
            if (isFullORB) {
                System.getProperties()
                    .setProperty(ORBClassKey, className);
                // NOTE: without setting this explicitly it is getting
                // the default and failing.  Not sure why this is needed
                // in this test but not in others.
                System.getProperties()
                    .setProperty(ORBConstants.INITIAL_PORT_PROPERTY, "1049");
                orb = ORB.init((String[])null, System.getProperties());
            } else {
                System.getProperties()
                    .setProperty(ORBSingletonClassKey,className);
                orb = ORB.init();
            }

            created(orb);
            checkShouldNotExist(shouldExist, className);
            checkType(clazz, orb);

            // Do something to make sure the ORB works.

            if (isFullORB) {
                NamingContext nameService =
                    NamingContextHelper.narrow(
                        orb.resolve_initial_references("NameService"));
                NameComponent nc = new NameComponent("FOO", "");
                NameComponent path[] = { nc };
                nameService.rebind(path, nameService);
            } else {
                orb.create_any();
            }
        } catch (Exception e) {
            if (shouldExist) {
                throw e;
            }
        }
    }

    public static void creating(String className)
    {
        System.out.println(baseMsg + ".createORB: creating: " + className);
    }

    public static void created(ORB orb)
    {
        System.out.println(baseMsg + ".createORB: created: " + orb);
    }

    public static void checkShouldNotExist(boolean shouldExist,
                                           String className)
        throws
            Exception
    {
        if (! shouldExist) {
            throw new Exception("should not exist: " + className);
        }
    }

    public static void checkType(Class clazz, ORB orb)
        throws
            Exception
    {
        // If we get here we created an ORB as expected.
        // Be sure it is the one we wanted to create.
        if (! clazz.isInstance(orb)) {
            throw new Exception("Expected: " + clazz + " got: " + orb);
        }
    }
}

// End of file.
