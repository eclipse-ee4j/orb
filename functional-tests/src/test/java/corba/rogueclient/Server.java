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

package corba.rogueclient;

import java.util.Properties;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.misc.ORBConstants;

import corba.framework.Options;
import corba.hcks.C;
import corba.hcks.U;

import org.omg.PortableServer.POA;

//
// Created      : 2004 May 3, 2004 by Charlie Hunt
// Last Modified: 2004 May 3, 2004 by Charlie Hunt
//

public class Server
{
    private static ORB orb = null;
    private static InitialContext initialContext = null;
    private static boolean dprint = false;

    public static void main(String[] args)
    {
        Properties props = System.getProperties();
        if (dprint)
        {
            props.put(ORBConstants.DEBUG_PROPERTY, "transport,giop");
        }

        try
        {
            orb = (ORB)org.omg.CORBA.ORB.init(args, props);

            Tester testerImpl = new TesterImpl();

            initialContext = C.createInitialContext(orb);
            initialContext.rebind("Tester", testerImpl);

            U.sop(Options.defServerHandshake);
            orb.run();

        } catch (Throwable t) {
            U.sop("Unexpected throwable...");
            t.printStackTrace();
            System.exit(1);
        }
        U.sop("Ending successfully...");
    }
}

