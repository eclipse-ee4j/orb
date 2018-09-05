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
// Created       : 2003 Apr 09 (Wed) 16:28:12 by Harold Carr.
// Last Modified : 2003 Jul 29 (Tue) 16:38:25 by Harold Carr.
//

package corba.exceptiondetailsc;

import javax.naming.InitialContext;

import org.omg.CORBA.ORB;
import org.omg.CORBA.Policy;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.Servant;

import corba.framework.Controller;
import corba.framework.Options;
import corba.hcks.C;
import corba.hcks.U;

public class Server 
{
    public static final String baseMsg = Server.class.getName();
    public static final String main = baseMsg + ".main";
    public static final String thisPackage = 
        Server.class.getPackage().getName();

    public static final String rmiiIServantPOA_Tie = 
        thisPackage + "._rmiiIServantPOA_Tie";

    public static final String idlIConnect  = "idlIConnect";
    public static final String idlIPOA      = "idlIPOA";
    public static final String rmiiIConnect = "rmiiIConnect";
    public static final String rmiiIPOA     = "rmiiIPOA";

    public static ORB orb;
    public static InitialContext initialContext;
    public static POA rootPOA;

    public static void main(String[] av)
    {
        try {
            U.sop(main + " starting");

            if (! ColocatedClientServer.isColocated) {
                U.sop(main + " : creating ORB.");
                orb = (ORB) ORB.init(av, null);
                U.sop(main + " : creating InitialContext.");
                initialContext = C.createInitialContext(orb);
            }

            rootPOA = U.getRootPOA(orb);
            rootPOA.the_POAManager().activate();

            //
            // IDL references.
            //

            U.sop("Creating/binding IDL references.");

            U.createWithConnectAndBind(idlIConnect, 
                                       new idlIServantConnect(), orb);
            U.createWithServantAndBind(idlIPOA,
                                       new idlIServantPOA(), rootPOA, orb);

            //
            // RMI-IIOP references.
            //

            U.sop("Creating/binding RMI-IIOP references.");

            initialContext.rebind(rmiiIConnect, new rmiiIServantConnect());

            Servant servant = (Servant)
                javax.rmi.CORBA.Util.getTie(new rmiiIServantPOA());
            U.createWithServantAndBind(rmiiIPOA, servant, rootPOA, orb);

            U.sop(main + " ready");
            U.sop(Options.defServerHandshake);
            System.out.flush();

            synchronized (ColocatedClientServer.signal) {
                ColocatedClientServer.signal.notifyAll();
            }
            
            orb.run();

        } catch (Exception e) {
            U.sopUnexpectedException(main, e);
            System.exit(1);
        }
        U.sop(main + " ending successfully");
        System.exit(Controller.SUCCESS);
    }
}

// End of file.

