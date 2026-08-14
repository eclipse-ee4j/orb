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
// Created       : 2003 Sep 26 (Fri) 17:14:01 by Harold Carr.
// Last Modified : 2003 Nov 21 (Fri) 13:37:34 by Harold Carr.
//

package corba.connections;

import java.util.Properties;
import javax.naming.InitialContext;

import com.sun.corba.ee.spi.orb.ORB;

import com.sun.corba.ee.spi.misc.ORBConstants;

import corba.framework.Options;
import corba.hcks.C;
import corba.hcks.U;

public class Server
{
    public static String server1   = "server1";
    public static String server2   = "server2";
    public static String service11 = "service11";
    public static String service12 = "service12";
    public static String service21 = "service21";
    public static String service22 = "service22";

    public static ORB orb;
    public static InitialContext initialContext;
    public static String serverName;
    public static String name1;
    public static String name2;

    public static boolean setWaterMarks = true;
    public static boolean dprint        = false;

    public static void main(String[] av)
    {
        serverName = av[0];
        name1 = av[1];
        name2 = av[2];

        try {
            Properties props = new Properties();

            if (setWaterMarks) {
                props.put(ORBConstants.HIGH_WATER_MARK_PROPERTY, "25");
                props.put(ORBConstants.LOW_WATER_MARK_PROPERTY, "5");
                props.put(ORBConstants.NUMBER_TO_RECLAIM_PROPERTY, "10");
            }
            if (dprint) {
                props.put(ORBConstants.DEBUG_PROPERTY, "transport");
            }
            orb = (ORB) org.omg.CORBA.ORB.init((String[])null, props);
            ConnectionStatistics stats = new ConnectionStatistics(orb);

            /* Cannot do these here because there is no "Connections" root
            stats.inbound(serverName + ": after ORB.init", orb);
            stats.outbound(serverName + ": after ORB.init", orb);
            */

            initialContext = C.createInitialContext(orb);
            stats.outbound(serverName + ": after InitialContext", orb);
            stats.inbound(serverName + ": after InitialContext", orb);

            U.sop(serverName + " binding: " + name1 + " " + name2);

            initialContext.rebind(name1, new RemoteService(orb, serverName));
            initialContext.rebind(name2, new RemoteService(orb, serverName));

            stats.outbound(serverName + ": after binding", orb);
            stats.inbound(serverName + ": after binding", orb);

            U.sop(Options.defServerHandshake);
            orb.run();

        } catch (Exception e) {
            U.sop(serverName + " exception");
            e.printStackTrace(System.out);
            System.exit(1);
        }
        U.sop(serverName + " ending successfully");
    }
}

// End of file.

