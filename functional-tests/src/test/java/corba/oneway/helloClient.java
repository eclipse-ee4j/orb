/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package corba.oneway;

import HelloApp.hello;
import HelloApp.helloHelper;
import java.util.Properties ;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContext;
import org.omg.CosNaming.NamingContextHelper;

public class helloClient 
{
    public static void main(String args[])
    {
        try{
            // create and initialize the ORB
            Properties props = new Properties() ;
            props.put( "org.omg.CORBA.ORBClass", 
                       System.getProperty("org.omg.CORBA.ORBClass"));
            ORB orb = ORB.init(args, props);

            // get the root naming context
            org.omg.CORBA.Object objRef = 
                orb.resolve_initial_references("NameService");
            NamingContext ncRef = NamingContextHelper.narrow(objRef);
 
            // resolve the Object Reference in Naming
            NameComponent nc = new NameComponent("Hello", "");
            NameComponent path[] = {nc};
            hello helloRef = helloHelper.narrow(ncRef.resolve(path));

            // call the hello server object and print results
            System.out.println("Invoking oneway method...");
            helloRef.sayHello();
            System.out.println("OK! Returned from oneway call !!");

            // call shutdown
            System.out.println("Invoking shutdown...");
            helloRef.shutdown();
            System.out.println("OK! Returned from shutdown!!");

        } catch (Exception e) {
            System.out.println("ERROR : " + e) ;
            e.printStackTrace(System.out);
            System.exit (1);
        }
    }
}
