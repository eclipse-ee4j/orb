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

