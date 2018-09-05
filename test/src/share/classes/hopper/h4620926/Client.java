/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package hopper.h4620926;

import org.omg.CORBA.*;
import org.omg.CosNaming.*;
import java.util.*;
import test.*;
        
public class Client extends Thread {
    private static int numErrors = 0;

    private static synchronized void errorOccurred( int clientNo,
        Exception exc)
    {
        numErrors++ ;
        System.out.println(
            "\nError in ClientNo " + clientNo + ": " + exc );

        System.err.println(
            "\nError in ClientNo " + clientNo + ": " + exc );

        exc.printStackTrace();
    }

    private static int counter = 0;
    private int clientNo = 0;
    Hello ref = null;

    public Client(ORB orb) throws Exception {
        counter++;
        clientNo = counter;
        System.out.println("Creating client - " + clientNo);
        NamingContext namingContext = NamingContextHelper.narrow(
            orb.resolve_initial_references("NameService"));
        NameComponent[] name = { new NameComponent("Hello", "") };
        ref = HelloHelper.narrow(namingContext.resolve(name));
    }

    public void run() {
        for (int i = 0; i < 3; i++) {
            try {
                System.out.println("Client - " + clientNo + " : " 
                                   + ref.sayHello());
                Thread.sleep(2000);
            } catch (Exception e) {
                errorOccurred(clientNo, e) ;
                System.exit( 1 );
            }
        }
        System.out.println( "TEST PASSED" );
        System.out.flush( );
    }

    public static void main(String[] args) {
        Client[] c = null;
        int noOfThreads = 5;

        // try {
            // noOfThreads = Integer.parseInt(args[0]);
        // } catch (NumberFormatException e) { }

        try {
            c = new Client[noOfThreads];
            for (int i = 0; i < noOfThreads; i++) {
                ORB orb = ORB.init(args, null);
                c[i] = new Client(orb);
            }

            for (int i = 0; i < noOfThreads; i++) {
                c[i].start();
            }

            for (int i = 0; i < noOfThreads; i++) {
                c[i].join();
            }
        } catch (Exception e) {
            errorOccurred( -1, e );
        }

        System.exit( numErrors>0 ? 1 : 0 ) ;
    }
}
