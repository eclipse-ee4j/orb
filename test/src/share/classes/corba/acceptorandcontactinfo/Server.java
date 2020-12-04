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
// Created       : 2003 Apr 09 (Wed) 16:28:12 by Harold Carr.
// Last Modified : 2004 Jan 31 (Sat) 11:12:48 by Harold Carr.
//

package corba.acceptorandcontactinfo;

import javax.naming.InitialContext;

import org.omg.CORBA.Policy;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.ServantRetentionPolicyValue;

import corba.framework.Controller;
import corba.framework.Options;
import corba.hcks.C;
import corba.hcks.U;

import com.sun.corba.ee.spi.transport.TransportManager;
import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.impl.legacy.connection.LegacyServerSocketManagerImpl;

public class Server 
{
    public static final String baseMsg = Server.class.getName();
    public static final String main = baseMsg + ".main";
    public static final String thisPackage = 
        Server.class.getPackage().getName();

    public static final String rmiiIServantPOA_Tie = 
        thisPackage + "._rmiiIServantPOA_Tie";

    public static final String rmiiIPOA     = "rmiiIPOA";
    public static final String SLPOA        = "SLPOA";

    public static ORB orb;
    public static InitialContext initialContext;
    public static TransportManager transportManager;
    public static POA rootPOA;
    public static POA slPOA;

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

            Policy[] policies = U.createUseServantManagerPolicies(
                rootPOA, ServantRetentionPolicyValue.NON_RETAIN);

            slPOA = U.createPOAWithServantManager(
                rootPOA, SLPOA, policies, new ServantLocator());


            U.createRMIPOABind(
                C.rmiiSL, rmiiIServantPOA_Tie, slPOA, orb, initialContext);

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

