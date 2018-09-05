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
// Last Modified : 2003 May 19 (Mon) 13:33:14 by Harold Carr.
//

package corba.islocal;

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
    public static final String rmiiIConnectDifferentLoader =
        "rmiiIConnectDifferentLoader";
    public static final String rmiiIPOA     = "rmiiIPOA";
    public static final String SLPOA        = "SLPOA";

    public static ORB orb;
    public static InitialContext initialContext;
    public static TransportManager transportManager;
    public static POA rootPOA;
    public static POA slPOA;

    public static CustomClassLoader loader;

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
                rootPOA,
                ServantRetentionPolicyValue.NON_RETAIN);
            slPOA = U.createPOAWithServantManager(rootPOA, SLPOA, policies,
                                                  new MyServantLocator(orb));

            //
            // IDL references.
            //

            U.createWithConnectAndBind(idlIConnect, 
                                       new idlIServantConnect(), orb);
            U.createWithServantAndBind(idlIPOA,
                                       new idlIServantPOA(), rootPOA, orb);

            //
            // RMI-IIOP references.
            //

            Object rmiiIServantConnectInstance;
            ClassLoader classLoader;

            System.out.println("getSystemClassLoader: "
                               + ClassLoader.getSystemClassLoader());

            // Create one in standard class loader.

            rmiiIServantConnectInstance = new rmiiIServantConnect();
            classLoader = 
                rmiiIServantConnectInstance.getClass().getClassLoader();
            System.out.println("rmiiIServantConnectInstance: " +
                               rmiiIServantConnectInstance);
            System.out.println("rmiiIServantConnectInstance classLoader: " +
                               classLoader);
            initialContext.rebind(rmiiIConnect, rmiiIServantConnectInstance);

            // Create one is a different class loader.

            U.createRMIPOABind(C.rmiiSL, rmiiIServantPOA_Tie,
                               slPOA, orb, initialContext);

            // Create a POA-based RMI-IIOP Servant

            /* REVISIT
            U.createWithServantAndBind(rmiiIPOA,
                                       new rmiiIServantPOA(), rootPOA, 
                                       (org.omg.CORBA.ORB) orb);
            */

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

    public static String filter(String a, String msg)
    {
        return a + " (echo from " + msg + ")";
    }

    public static void  checkThread(String msg)
    {
        if (ColocatedClientServer.isColocated) {
            if (Client.clientThread == Thread.currentThread()) {
                U.sop("NOTE: " 
                      + msg
                      + ": colocated call correctly running in server on client thread");
            } else {
                Client.errors++;
                U.sop("!!! " + msg + ": incorrect thread !!!");
            }
        }
    }
}

// End of file.

