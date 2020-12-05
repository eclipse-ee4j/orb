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
