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
// Created       : 2003 Apr 09 (Wed) 16:54:21 by Harold Carr.
// Last Modified : 2004 Jan 31 (Sat) 10:06:37 by Harold Carr.
//

package corba.acceptorandcontactinfo;

import javax.naming.InitialContext;
import javax.rmi.CORBA.Util;
import org.omg.CORBA.ORB;

import corba.framework.Controller;
import corba.hcks.C;
import corba.hcks.U;

import com.sun.corba.ee.impl.legacy.connection.LegacyServerSocketManagerImpl;

public class Client 
{
    public static final String baseMsg = Client.class.getName();
    public static final String main = baseMsg + ".main";
    
    public static ORB orb;
    public static InitialContext initialContext;

    public static rmiiI rmiiIPOA;
    public static String rmiiIPOAArg     = Server.rmiiIPOA;

    public static void main(String[] av)
    {
        try {
            U.sop(main + " starting");

            if (! ColocatedClientServer.isColocated) {
                U.sop(main + " : creating ORB.");
                orb = ORB.init(av, null);
                U.sop(main + " : creating InitialContext.");
                initialContext = C.createInitialContext(orb);
            }

            rmiiIPOA = (rmiiI)
                U.lookupAndNarrow(C.rmiiSL, rmiiI.class, initialContext);


            U.sop("CLIENT: " + rmiiIPOA.m(rmiiIPOAArg));

            orb.shutdown(true);

        } catch (Exception e) {
            U.sopUnexpectedException(main + " : ", e);
            System.exit(1);
        }
        U.sop(main + " ending successfully");
        System.exit(Controller.SUCCESS);
    }
}

// End of file.

