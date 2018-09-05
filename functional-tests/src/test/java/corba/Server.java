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
// Created       : 2003 Dec 11 (Thu) 11:04:04 by Harold Carr.
// Last Modified : 2003 Dec 17 (Wed) 21:29:35 by Harold Carr.
//

package corba.legacybootstrapserver;

import java.util.Properties;
import org.omg.CORBA.ORB;
import corba.framework.Controller;
import corba.framework.Options;

public class Server 
{
    public static final String baseMsg = Server.class.getName();
    public static final String main = baseMsg + ".main";

    public static void main(String[] av)
    {
        try {
            System.out.println(main + " starting");
            System.out.println(main + " " + getBootstrapFilePathAndName());

            // Initialize the file.
            Properties props = new Properties();
            ORB orb = ORB.init((String[])null, (Properties) null);
            org.omg.CORBA.Object o = new IServantConnect();
            orb.connect(o);
            props.put(Client.initialEntryName, orb.object_to_string(o));
            Client.writeProperties(props, getBootstrapFilePathAndName());

            // Set up args.
            String[] args = { "-InitialServicesFile", 
                              getBootstrapFilePathAndName(),
                              "-ORBInitialPort",
                              Client.getORBInitialPort() };

            ServerThread serverThread = new ServerThread(args);
            serverThread.start();

            // Wait 5 seconds before sending handshake.
            Thread.sleep(5000);

            System.out.println(Options.defServerHandshake);

            Object wait = new Object();
            synchronized (wait) {
                wait.wait();
            }
        } catch (Exception e) {
            System.out.println(main + ": unexpected exception: " + e);
            e.printStackTrace(System.out);
            System.exit(1);
        }
        System.exit(Controller.SUCCESS);
    }

    public static String getBootstrapFilePathAndName()
    {
        return
            //Options.getOutputDirectory()
            System.getProperty("output.dir")
            + System.getProperty("file.separator")
            + Client.bootstrapFilename;
    }
}

class ServerThread extends Thread
{
    String[] av;
    ServerThread (String[] av)
    {
        this.av = av;
    }

    public void run ()
    {
        try {
            // Start server.
            com.sun.corba.ee.internal.CosNaming.BootstrapServer.main(av);
        } catch (Throwable t) {
            System.out.println("BootstrapServer.main Throwable:");
            t.printStackTrace(System.out);
            System.exit(1);
        }
    }
}

class IServantConnect
    extends
        _IImplBase
{
    public void dummy(){}
}

// End of file.

