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

