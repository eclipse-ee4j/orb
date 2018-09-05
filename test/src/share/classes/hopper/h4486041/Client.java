/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package hopper.h4486041;

import org.omg.CORBA.ORB;
import java.util.Properties;

public class Client
{
    public static final String ORBClassKey =
        "org.omg.CORBA.ORBClass";

    public static final String ORBSingletonClassKey =
        "org.omg.CORBA.ORBSingletonClass";

    public static int numberOfErrors = 0;

    public static void main(String[] av)
    {
        try {

            Properties properties = new Properties();

            // --------------------------------------

            properties.put(ORBClassKey, "NotFound");
            expectException("NotFound", av, properties,
                            ClassNotFoundException.class, false, false);

            // --------------------------------------

            properties.put(ORBClassKey, "hopper.h4486041.TestORB");
            expectNormal("TestORB Good", av, properties, false);

            // --------------------------------------

            properties.put(TestORB.ThrowError, "dummy");
            expectException("TestORB ORBInitException", av, properties,
                            ORBInitException.class, true, false);

            // --------------------------------------

            System.getProperties().put(ORBSingletonClassKey, 
                                       "hopper.h4486041.TestORB");
            expectNormal("TestORB Singleton Good", null, null, true);

            // --------------------------------------

            /* NOTE:
             * set_parameters is not called for singletons so
             * this test will not work.
            System.getProperties().put(TestORB.ThrowError, "dummy");
            expectException("TestORB Singleton ORBInitException", null, null,
                            ORBInitException.class, true, true);
            */

            // --------------------------------------

            if (numberOfErrors > 0) {
                throw new Throwable("Test found errors.");
            }

        } catch (Throwable t) {
            t.printStackTrace(System.out);
            System.exit(1);
        }
    }

    //
    // isSetParameters: if the exception happens during set_parameters
    // then the exception is not in INITIALIZE from create_impl.
    // It is directly from set_parameters.
    //
    public static void expectException(String message,
                                       String[] av,
                                       Properties properties,
                                       Class expectedException, 
                                       boolean isSetParameters,
                                       boolean isSingleton)
    {
        System.out.println();
        System.out.println("------------------------------------------------");
        System.out.println("Begin expectException: " + message);
        try {
            if (isSingleton) {
                ORB orb = ORB.init();
            } else {
                ORB orb = ORB.init(av, properties);
            }
            System.out.println("\tERROR: Should not see this.");
            System.out.println("\t\tExpected exception: " + expectedException);
            numberOfErrors++;
        } catch (Throwable t) {
            Throwable cause;
            if (isSetParameters) {
                cause = t;
            } else {
                cause = t.getCause();
            }
            System.out.println("\tExpected cause: " + expectedException);
            System.out.println("\tCause: " + cause);
            if (cause == null ||
                (! cause.getClass().equals(expectedException)))
            {
                numberOfErrors++;
                System.out.println("\tERROR: Wrong cause.");
            } else {
                System.out.println("\tOK");
            }
            
        }
        System.out.println("End expectException: " + message);
        System.out.println("------------------------------------------------");
    }

    public static void expectNormal(String message, 
                                    String[] av,
                                    Properties properties,
                                    boolean isSingleton)
    {
        System.out.println();
        System.out.println("------------------------------------------------");
        System.out.println("Begin expectNormal: " + message);
        try {
            if (isSingleton) {
                ORB orb = ORB.init();
            } else {
                ORB orb = ORB.init(av, properties);
            }
            System.out.println("\tOK");
        } catch (Throwable t) {
            numberOfErrors++;
            System.out.println("\tERROR: Should not see this");
            System.out.println("\t\tUnexpected exception: "+ t);
        }
        System.out.println("End expectNormal: " + message);
        System.out.println("------------------------------------------------");
    }
}
                
// End of file.
