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
// Created       : 2002 Jan 17 (Thu) 14:09:43 by Harold Carr.
// Last Modified : 2002 Jan 17 (Thu) 15:42:33 by Harold Carr.
//

package corba.purgecalls;

import corba.framework.Controller;
import corba.hcks.C;
import corba.hcks.U;
import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;

public class Server 
{
    public static final String baseMsg = Server.class.getName();
    public static final String main = baseMsg + ".main";

    public static final String ServerSide = "ServerSide";

    public static ORB        orb;
    public static POA        rRootPOA;
    public static ServerSide rServerSide;

    public static void main(String[] av)
    {
        try {

            U.sop(main + " starting");

            orb = C.createORB(av, 1024);
            rRootPOA = U.getRootPOA(orb);
            rRootPOA.the_POAManager().activate();

            U.createWithServantAndBind(ServerSide,
                                       new ServerSideServant(),
                                       rRootPOA, orb);

            U.sop(main + " ready");
            U.sop("Server is ready."); // CORBATest handshake.

            System.out.flush();

            orb.run();

        } catch (Exception e) {
            U.sopUnexpectedException(main, e);
            System.exit(1);
        }
        System.exit(Controller.SUCCESS);
    }
}

// End of file.

