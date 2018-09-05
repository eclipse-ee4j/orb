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
// Created       : 2000 Jun 29 (Thu) 14:16:17 by Harold Carr.
// Last Modified : 2001 Sep 24 (Mon) 21:41:02 by Harold Carr.
//

package pi.serviceexample;

import org.omg.CORBA.ORB;
import java.util.Properties;

public class ColocatedServers
{
    public static ORB orb;

    public static boolean colocatedBootstrapDone = false;

    public static void main (String[] av)
    {
        try {

            //
            // Share an ORB between objects servers.
            //

            Properties props = new Properties();
            props.put("org.omg.PortableInterceptor.ORBInitializerClass."
                      + "pi.serviceexample.AServiceORBInitializer",
                      "");
            props.put("org.omg.PortableInterceptor.ORBInitializerClass."
                      + "pi.serviceexample.LoggingServiceServerORBInitializer",
                      "");
            ORB orb = ORB.init(av, props);
            ArbitraryObjectImpl.orb = orb;
            LoggingServiceImpl.orb = orb;

            //
            // Start both object servers.
            //

            ServerThread ServerThread = new ServerThread(av);
            ServerThread.start();
            ArbitraryObjectImpl.main(av);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
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
        LoggingServiceImpl.main(av);
    }
}

// End of file.
