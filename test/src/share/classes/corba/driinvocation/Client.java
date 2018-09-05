/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package corba.driinvocation;

import corba.framework.Controller;
import corba.framework.InternalProcess;
import corba.rmipoacounter.counterClient;
import corba.rmipoacounter.counterIF;
import java.io.DataInputStream ;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Hashtable;
import java.util.Properties;
import javax.rmi.PortableRemoteObject ;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContext;
import org.omg.CosNaming.NamingContextHelper;

public class Client implements InternalProcess
{
    // Temporary hack to get this test to work and keep the output
    // directory clean
    private static final String outputDirOffset 
        = "/corba/rmipoacounter/".replace('/', File.separatorChar);

    /**
     * These counters are used to check that the values remain
     * correct even when the server is restarted.
     */
    private static long counterValue = 1;

    private void performTest(PrintStream out,
                             PrintStream err,
                             counterIF counterRef1,
                             counterIF counterRef2) throws Exception
    {
        // call the counter server objects and print results
        long value = counterRef1.increment(1);
        out.println("Counter1 value = " + value);
        if (++counterValue != value)
            throw new Exception("Invalid counter1: "
                                + value + " but should be " + counterValue);

        for (int i = 0; i < 2; i++) {
            value = counterRef2.increment(1);
            out.println("Counter2 value = "+value);
            if (++counterValue != value)
                throw new Exception("Invalid counter2: "
                                    + value + " but should be " + counterValue);
        }
        
    }

    public void run(Properties environment,
                    String args[],
                    PrintStream out,
                    PrintStream err,
                    Hashtable extra) throws Exception
    {
        environment.list(out);

        try {
            // create and initialize the ORB
            ORB orb = ORB.init(args, environment);

            // get counter objrefs from NameService
            org.omg.CORBA.Object objRef =
                orb.resolve_initial_references("NameService");
            NamingContext ncRef = NamingContextHelper.narrow(objRef);
            NameComponent nc = new NameComponent("Counter1", "");
            NameComponent[] path = {nc};

            counterIF counterRef1 = 
                (counterIF)PortableRemoteObject.narrow(ncRef.resolve(path),
                                                       counterIF.class);

            // Read IOR from file and destringify it
            InputStream inf = 
                new FileInputStream(environment.getProperty("output.dir")
                                    + outputDirOffset
                                    + "counterior2");
            DataInputStream in = new DataInputStream(inf);
            String ior = in.readLine() ;
            org.omg.CORBA.Object obj = orb.string_to_object(ior) ;
            counterIF counterRef2 
                = (counterIF)PortableRemoteObject.narrow(obj, counterIF.class);

            Controller server = (Controller)extra.get("server");

            for (int i = 0; i < 3; i++) {
                out.println("Testing, pass #" + i);
                performTest(out, err, counterRef1, counterRef2);
                out.println("Restarting server...");
                server.stop();
                server.start();
            }

        } catch (Exception e) {
            e.printStackTrace(err);
            throw e;
        }
    }

    public static void main(String args[])
    {
        try {

            (new counterClient()).run(System.getProperties(),
                                      args,
                                      System.out,
                                      System.err,
                                      null);

        } catch (Exception e) {
            System.err.println("ERROR : " + e) ;
            e.printStackTrace(System.err);
            System.exit(1);
        }
    }
}
