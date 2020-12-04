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

package corba.unarrow;

import org.omg.CosNaming.*;
import org.omg.CosNaming.NamingContextPackage.*;
import org.omg.CORBA.*;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class Client
{
    static Hello helloRef;
    static Bye byeRef;

    static void verifyBehavior(String args[]) {
        try{
            // create and initialize the ORB
            ORB orb = ORB.init(args, System.getProperties());

            // get the root naming context
            org.omg.CORBA.Object objRef = 
                    orb.resolve_initial_references("NameService");
            NamingContext ncRef = NamingContextHelper.narrow(objRef);
     
            // resolve the Object Reference in Naming
            NameComponent nc = new NameComponent("Hello", "");
            NameComponent path[] = {nc};
            helloRef = HelloHelper.unchecked_narrow(ncRef.resolve(path));

            System.out.println("Using unchecked_narrow obtained a handle on server object: " + helloRef);
            helloRef.sayHello();
            System.out.println("Success in sayHello !!");

            // resolve the Object Reference in Naming
            nc = new NameComponent("Bye", "");
            path[0] = nc;
            byeRef = ByeHelper.unchecked_narrow(ncRef.resolve(path));

            System.out.println("Using unchecked_narrow obtained a handle on server object: " + byeRef);
            byeRef.sayBye();
            System.out.println("Success in sayBye !!");

            nc = new NameComponent("Hello", "");
            path[0] = nc;

            // Call narrow using the wrong name to make sure that we get
            // an exception thrown
            try {
                byeRef = ByeHelper.narrow(ncRef.resolve(path));
                // If this succeeds throw exception
                throw new Exception("Failure: Narrow not throwing exception, it should");
            } catch (org.omg.CORBA.BAD_PARAM bp) {
                System.out.println("Success: Getting an org.omg.CORBA.BAD_PARAM exception !!");
            }

            // Now use the wrong name with unchecked narrow
            // We should see no exception thrown in this call as
            // per the spec
            byeRef = ByeHelper.unchecked_narrow(ncRef.resolve(path));

            System.out.println("Using wrong path in unchecked_narrow: obtained a handle on server object: " + byeRef);

            // Now do the invoke as though it is a byeRef
            // We should get a BAD_OPERATION exception
            try {
                byeRef.sayBye();
                // If this succeeds throw exception
                throw new Exception("Failure: Invocation should not succeed");
            } catch (org.omg.CORBA.BAD_OPERATION bo) {
                System.out.println("Success: Getting an org.omg.CORBA.BAD_OPERATION exception !!");
            }
        } catch (Exception e) {
            System.out.println("ERROR : " + e) ;
            e.printStackTrace(System.out);
            System.exit(1);
        }
    }

    static void verifyMethodExists(Class helperClass, String methodName, Class[] params, Class returnType) {
        try {
            Method m = helperClass.getDeclaredMethod(methodName, params);
            System.out.println("Success: Verified the method name and parameter types !!");
            if (m.getReturnType() != returnType)
                throw new Exception("Incorrect return type");
            else
                System.out.println("Success: Verified the return type " + m.getReturnType());
            int modifier = m.getModifiers();
            if (Modifier.isPublic(modifier) && Modifier.isStatic(modifier))
                System.out.println("Success: Verified the method modifiers !!");
            else
                throw new Exception("Method modifiers incorrect");
        } catch (Exception e) {
            System.out.println("ERROR : " + e) ;
            e.printStackTrace(System.out);
            System.exit(1);
        }
    }

    static void verifyMethodCount(Class helperClass, String methodName, int numberOfMethods) {
        try {
            Method[] allMethods = helperClass.getDeclaredMethods();
            int total = 0;
            for (int i = 0; i < allMethods.length; i++) {
                if (allMethods[i].getName().equals(methodName)) {
                    total++;
                    if (total > numberOfMethods) {
                        throw new Exception("Too many " + methodName + "  methods found !!");
                    }
                }
            }
            System.out.println("Success: Method count for " + methodName + " verified !!");
        } catch (Exception e) {
            System.out.println("ERROR : " + e) ;
            e.printStackTrace(System.out);
            System.exit(1);
        }
    }

    static void verifyMethodSignatures() {


        // In this test we will verify the method signatures of the
        // Helper classes generated for the interfaces defined in hello.idl
        
        //
        // Foo is an abtract interface so the following method should exist
        // in the Helper class:
        // 
        // public static <typename> unchecked_narrow( java.lang.Object obj) {...}
        // 
        
        verifyMethodExists(corba.unarrow.FooHelper.class, "unchecked_narrow",
                new Class[]{java.lang.Object.class}, corba.unarrow.Foo.class);

        verifyMethodCount(corba.unarrow.FooHelper.class, "unchecked_narrow", 1);

        //
        // Bar is a non-abstract interface with Foo as the base interface.
        // The following 2 methods should exist in the Helper class for this:
        //
        // public static <typename> unchecked_narrow( org.omg.CORBA.Object obj) {...}
        //
        // public static <typename> unchecked_narrow( java.lang.Object obj) {...}
        //
        

        verifyMethodExists(corba.unarrow.BarHelper.class, "unchecked_narrow",
                new Class[]{java.lang.Object.class}, corba.unarrow.Bar.class);

        verifyMethodExists(corba.unarrow.BarHelper.class, "unchecked_narrow",
                new Class[]{org.omg.CORBA.Object.class}, corba.unarrow.Bar.class);

        verifyMethodCount(corba.unarrow.FooHelper.class, "unchecked_narrow", 2);

        //
        // Hello is a non-abtract interface, so the following method should exist
        // in the Helper class for it:
        //
        // public static <typename> unchecked_narrow( org.omg.CORBA.Object obj) {...}
        //

        verifyMethodExists(corba.unarrow.HelloHelper.class, "unchecked_narrow",
                new Class[]{org.omg.CORBA.Object.class}, corba.unarrow.Hello.class);

        verifyMethodCount(corba.unarrow.FooHelper.class, "unchecked_narrow", 1);

    }

    public static void main(String args[])
    {
        verifyBehavior(args);
        verifyMethodSignatures();
    }

}
