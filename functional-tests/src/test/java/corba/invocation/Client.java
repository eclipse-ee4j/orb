/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package corba.invocation;

import org.omg.CORBA.ORB;
import org.omg.CORBA.Object;
import org.omg.CORBA.SystemException;
import java.util.*;

import com.sun.corba.ee.spi.logging.ORBUtilSystemException;
import com.sun.corba.ee.spi.misc.ORBConstants;

public class Client implements Runnable {

    private String[] args;
    private static final ORBUtilSystemException wrapper =
        ORBUtilSystemException.self ;

    public Client(String[] args) {
        this.args = args;
    }

    public static void main(String args[]) {
        new Client(args).run();
    }

    public void run() {

        try {

            Properties props = new Properties() ;
            //props.put("com.sun.corba.ee.ORBDebug", "transport,subcontract");
            props.setProperty(ORBConstants.TRANSPORT_TCP_CONNECT_TIMEOUTS_PROPERTY, "250:1000:100");
            ORB orb = ORB.init(args, props);

            String corbalocURL =
                System.getProperty(InvocationTest.URL_PROPERTY);

            Object obj = orb.string_to_object(corbalocURL);

            if (obj == null) {
                throw new RuntimeException("string_to_object(" +
                                           corbalocURL + ")");
            }

            try {
                Hello helloRef = HelloHelper.narrow( obj );

                String msg = "FAILURE: call incorrectly succeeded";
                System.out.println("------------------------------------");
                System.out.println(msg);
                System.out.println("------------------------------------");
                throw new Exception(msg);

            } catch (org.omg.CORBA.COMM_FAILURE e) {
                SystemException connectException =
                    wrapper.connectFailure( new RuntimeException(),
                        "foo", "bar", "baz");
                if (e.getClass().isInstance(connectException)
                    && e.minor == connectException.minor
                    && e.completed == connectException.completed)
                {
                    System.out.println("------------------------------------");
                    System.out.println("SUCCESS");
                    System.out.println("------------------------------------");
                } else {
                    System.out.println("------------------------------------");
                    System.out.println("FAILURE");
                    System.out.println("------------------------------------");
                    e.printStackTrace(System.out);
                    RuntimeException rte = 
                        new RuntimeException("Incorrect exception");
                    rte.initCause(e);
                    throw rte;
                }
            }

        } catch (Exception e) {
             e.printStackTrace(System.err);
             System.exit(1);
        }
    }
}



