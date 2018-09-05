/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

//
// Created       : 2005 Oct 05 (Wed) 14:43:22 by Harold Carr.
// Last Modified : 2005 Oct 06 (Thu) 11:59:21 by Harold Carr.
//

package corba.retryplugin;

import java.util.Hashtable;
import java.util.Properties;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;

import org.omg.CORBA.COMM_FAILURE;
import org.omg.CORBA.ORB;

import com.sun.corba.ee.impl.plugin.hwlb.RetryClientRequestInterceptor ;

import com.sun.corba.ee.spi.misc.ORBConstants ;

/**
 * @author Harold Carr
 */
public class Client
{
    static {
        // This is needed to guarantee that this test will ALWAYS use dynamic
        // RMI-IIOP.  Currently the default is dynamic when renamed to "ee",
        // but static in the default "se" packaging, and this test will
        // fail without dynamic RMI-IIOP.
        System.setProperty( ORBConstants.USE_DYNAMIC_STUB_PROPERTY, "true" ) ;
    }

    private static final long CLIENT_RUN_LENGTH = 1000 * 30; // 30 seconds

    public static void main(String[] av)
    {
        try {
            Properties props = new Properties();

            props.setProperty(
                "org.omg.PortableInterceptor.ORBInitializerClass."
                + RetryClientRequestInterceptor.class.getName(),
                "dummy");

            ORB orb = ORB.init((String[])null, props);
            Hashtable env = new Hashtable();
            env.put("java.naming.corba.orb", orb);
            InitialContext initialContext = new InitialContext(env);

            Test ref  = (Test)
                lookupAndNarrow(Common.ReferenceName, Test.class, 
                                initialContext);

            long startTime = System.currentTimeMillis();
            int i = 0;
            while (System.currentTimeMillis() - startTime < CLIENT_RUN_LENGTH){
                int result = ref.echo(i);
                if (result != i) {
                    throw new Exception("incorrect echo");
                }
                i++;
                System.out.println("correct echo response: " + result);
            }
            System.out.println("Loop completed.");
            System.out.println();
            System.out.println("Sleeping for 1 minute before timeout test");
            Thread.sleep(1000 * 60);

            try {
                System.out.println("This echo should timeout");
                RetryClientRequestInterceptor
                    .setTransientRetryTimeout(1000 * 5);
                ref.echo(-1);
                throw new Exception("timeout echo should not succeed");
            } catch (Exception e) {
                // See note in README.txt for the type of this exception.
                System.out.println("******: " + e);
            }

            System.out.println("Sleeping for 30 seconds before shutdown test");
            Thread.sleep(1000 * 30);

            try {
                System.out.println("This echo should fail since server done.");
                ref.echo(-1);
                throw new Exception("shutdown echo should not succeed");
            } catch (java.rmi.MarshalException e) {
                if (! e.detail.getClass().isInstance(new COMM_FAILURE())) {
                    throw new Exception("!!! Did not receive correct failure");
                }
            }

            System.out.println("--------------------------------------------");
            System.out.println("Client SUCCESS");
            System.out.println("--------------------------------------------");
            System.exit(0);

        } catch (Exception e) {
            e.printStackTrace(System.out);
            System.out.println("--------------------------------------------");
            System.out.println("Client FAILURE");
            System.out.println("--------------------------------------------");
            System.exit(1);
        }
    }

    public static Object lookupAndNarrow(String name, 
                                         Class clazz,
                                         InitialContext initialContext)
        throws
            NamingException
    {
        return PortableRemoteObject.narrow(initialContext.lookup(name), clazz);
    }
}

// End of file.

