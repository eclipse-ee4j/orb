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
// Created       : 2003 Apr 09 (Wed) 16:54:21 by Harold Carr.
// Last Modified : 2003 May 19 (Mon) 16:06:58 by Harold Carr.
//

package corba.islocal;

import javax.naming.InitialContext;
import org.omg.CORBA.ORB;

import corba.framework.Controller;
import corba.hcks.C;
import corba.hcks.U;

import com.sun.corba.ee.spi.presentation.rmi.StubAdapter ;

public class Client 
{
    public static final String baseMsg = Client.class.getName();
    public static final String main = baseMsg + ".main";
    
    public static ORB orb;
    public static InitialContext initialContext;

    public static idlI idlIConnect;
    public static idlI idlIPOA;
    public static rmiiI rmiiIConnect;
    public static rmiiI rmiiIPOA;

    public static String idlIConnectArg  = Server.idlIConnect;
    public static String idlIPOAArg      = Server.idlIPOA;
    public static String rmiiIConnectArg = Server.rmiiIConnect;
    public static String rmiiIPOAArg     = Server.rmiiIPOA;

    public static int errors = 0;
    public static Thread clientThread;

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

            idlIConnect = idlIHelper.narrow(U.resolve(Server.idlIConnect,orb));
            idlIPOA     = idlIHelper.narrow(U.resolve(Server.idlIPOA,    orb));

            rmiiIConnect = (rmiiI)
                U.lookupAndNarrow(Server.rmiiIConnect,
                                  rmiiI.class, initialContext);

            /*
            rmiiIPOA = (rmiiI)
                U.lookupAndNarrow(C.rmiiSL, rmiiI.class, initialContext);
            */


            U.sop("-----------isLocal-------------");

            boolean is_local_result = StubAdapter.isLocal( rmiiIConnect ) ;
            U.sop("is_local: " + is_local_result);
            if (is_local_result != ColocatedClientServer.isColocated) {
                    errors++;
                    U.sop("!!! is_local value incorrect !!!");
            }

            /* REVISIT - you cannot call StubAdapter.isLocal outside of stub.
               It HAS state.
            boolean isLocalResult = 
                StubAdapter.isLocal((javax.rmi.CORBA.Stub)rmiiIConnect);
            U.sop("StubAdapter.isLocal: " + isLocalResult);
            if (isLocalResult != ColocatedClientServer.isColocated) {
                    errors++;
                    U.sop("!!! StubAdapter.isLocal value incorrect !!!");
            }
            */

            U.sop("-----------calls-------------");

            if (ColocatedClientServer.isColocated) {
                clientThread = Thread.currentThread();
            }

            U.sop("CLIENT: " + idlIConnect.o(idlIConnectArg));
            U.sop("CLIENT: " + idlIPOA.o(idlIPOAArg));
            U.sop("CLIENT: " + rmiiIConnect.m(rmiiIConnectArg));
            /*
            U.sop("CLIENT: " + rmiiIPOA.m(rmiiIPOAArg));
            */

            orb.shutdown(true);

            if (errors != 0) {
                U.sop("!!! Errors found !!!");
                System.exit(1);
            }

        } catch (Exception e) {
            U.sopUnexpectedException(main + " : ", e);
            System.exit(1);
        }
        U.sop(main + " ending successfully");
        System.exit(Controller.SUCCESS);
    }
}

// End of file.

