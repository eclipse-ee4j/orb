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
// Created       : 1999 Mar 01 (Mon) 16:59:34 by Harold Carr.
// Last Modified : 2003 Apr 09 (Wed) 18:21:34 by Harold Carr.
//

package corba.hcks;

import java.util.Properties;

//
// IDL imports (and POA-based RMI-IIOP).
//

import org.omg.CORBA.ORB;
import org.omg.CORBA.Policy;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.RequestProcessingPolicyValue;
import org.omg.PortableServer.ServantRetentionPolicyValue;

import corba.framework.Controller;
import corba.framework.Options;

//
// RMI-IIOP imports.
//

import java.rmi.RMISecurityManager;
import javax.rmi.PortableRemoteObject;

//import java.rmi.Naming; // JRMP
import javax.naming.InitialContext;    // IIOP

public class Server 
{
    public static final String baseMsg = Server.class.getName();
    public static final String main = baseMsg + ".main";

    public static final String rmiiIServantPOA_Tie = 
        Server.class.getPackage().getName() + "._rmiiIServantPOA_Tie";

    public static String giopVersion = C.GIOP_VERSION_1_2;
    public static String buffMgrStategy = C.BUFFMGR_STRATEGY_STREAM;
    public static int fragmentSize = C.DEFAULT_FRAGMENT_SIZE;

    public static final String SAPOA = "SAPOA";
    public static final String SLPOA = "SLPOA";
    public static final String DSPOA = "DSPOA";

    public static ORB orb;
    public static InitialContext initialContext;
    public static boolean isColocated;

    public static POA rRootPOA;
    public static POA rSAPOA;
    public static POA rSLPOA;
    public static POA rDSPOA;

    public static rmiiIServant rrmiiIServant;
    public static idlI ridlStaticPoa;
    public static idlI ridlStatic;
    public static idlI ridlStaticForDisconnect;

    public static idlControllerStaticServant ridlControllerStaticServant;

    public static void main(String[] av)
    {
        // REVISIT - where did this come from?
        if (System.getSecurityManager() == null) { 
            //System.setSecurityManager(new RMISecurityManager()); 
        } 

        try {
            U.sop(main + " starting");

            if (ColocatedClientServer.isColocated) {
                isColocated = true;
            } else {
                isColocated = false;
                orb = C.createORB(av, giopVersion,
                                  buffMgrStategy, fragmentSize);
                // Use the same ORB which has interceptor properties set.
                initialContext = C.createInitialContext(orb);
            }

            rRootPOA = U.getRootPOA(orb);
            rRootPOA.the_POAManager().activate();

            //
            // Create a POA which uses a ServantActivator.
            //

            Policy[] policies;
            
            policies = U.createUseServantManagerPolicies(
                           rRootPOA,
                           ServantRetentionPolicyValue.RETAIN);
            rSAPOA =
                U.createPOAWithServantManager(rRootPOA, SAPOA, policies,
                                              new MyServantActivator(orb));

            //
            // Create a POA which uses a ServantLocator.
            //

            policies = U.createUseServantManagerPolicies(
                           rRootPOA,
                           ServantRetentionPolicyValue.NON_RETAIN);
            rSLPOA =
                U.createPOAWithServantManager(rRootPOA, SLPOA, policies,
                                              new MyServantLocator(orb));

            //
            // Create a POA which retains and uses default servant.
            //

            policies[0] = rRootPOA.create_servant_retention_policy(
                              ServantRetentionPolicyValue.NON_RETAIN);
            policies[1] = rRootPOA.create_request_processing_policy(
                             RequestProcessingPolicyValue.USE_DEFAULT_SERVANT);


            rDSPOA = rRootPOA.create_POA(DSPOA, null, policies);
            rDSPOA.the_POAManager().activate();

            //
            // Standard rmi-iiop references.
            //

            rrmiiIServant = new rmiiIServant();
            //Naming.rebind(rmiiI1, rrmiiIServant);              // JRMP
            initialContext.rebind(C.rmiiI1, rrmiiIServant);      // IIOP
            initialContext.rebind(C.rmiiI2, new rmiiIServant());

            //
            // poa-based rmi-iiop references.
            //

            U.createRMIIPOABindAndCall(C.rmiiSA, rmiiIServantPOA_Tie,
                                       rSAPOA, orb, initialContext);
            U.createRMIIPOABindAndCall(C.rmiiSL, rmiiIServantPOA_Tie,
                                       rSLPOA, orb, initialContext);

            //
            // references managed by RootPOA
            //

            U.createWithServantAndBind(C.idlHEADERI,
                                       new idlHEADERIServant(orb),
                                       rRootPOA, orb);

            ridlStaticPoa = 
                idlIHelper.narrow(U.createWithServantAndBind(
                    C.idlStaticPOA, new idlPOAServant(orb), rRootPOA, orb));


            U.createWithServantAndBind(
                C.idlDynamicPOA, new idlDynamicServant(orb), rRootPOA, orb);

            U.createWithServantAndBind(
                C.sendRecursiveType, new SendRecursiveTypePOAServant(orb),
                rRootPOA, orb);

            //
            // references managed by a POA with a ServantActivator
            //

            U.createWithIdAndBind(C.idlSAI1, idlSAIHelper.id(), rSAPOA, orb);
            U.createWithIdAndBind(C.idlSAI2, idlSAIHelper.id(), rSAPOA, orb);

            U.createWithIdAndBind(C.idlSAIRaiseObjectNotExistInIncarnate,
                                  idlSAIHelper.id(), rSAPOA, orb);

            U.createWithIdAndBind(C.idlSAIRaiseSystemExceptionInIncarnate,
                                  idlSAIHelper.id(), rSAPOA, orb);

            //
            // references managed by a POA with a ServantLocator
            //

            U.createWithIdAndBind(C.idlSLI1, idlSLIHelper.id(), rSLPOA, orb);
            U.createWithIdAndBind(C.idlAlwaysForward, 
                                             idlSLIHelper.id(), rSLPOA, orb);
            U.createWithIdAndBind(C.idlAlwaysForwardedToo,
                                             idlSLIHelper.id(), rSLPOA, orb);

            //
            // references manager by a POA with a default servant
            // which is not set.
            //

            // Note the type, SAI, is not important.
            U.createWithIdAndBind(C.idlNonExistentDefaultServant,
                                  idlSAIHelper.id(), rDSPOA, orb);

            //
            // deprecated APIs - also used by RMI-IIOP
            //

            ridlStatic = (idlI)
                U.createWithConnectAndBind(C.idlStatic,
                                           new idlStaticServant(orb),
                                           orb);
            ridlStaticForDisconnect = (idlI)
                U.createWithConnectAndBind(C.idlStaticForDisconnect,
                                           new idlStaticServant(orb),
                                           orb);

            U.createWithConnectAndBind(C.idlStaticTie,
                                       new idlI_Tie(ridlStatic),
                                       orb);

            U.createWithConnectAndBind(C.idlDynamic, 
                                       new idlDeprecatedDynamicServant(orb),
                                       orb);

            //
            // The controller.
            //

            
            ridlControllerStaticServant = new idlControllerStaticServant();
            U.createWithConnectAndBind(C.idlControllerStatic,
                                       ridlControllerStaticServant,
                                       orb);
            ridlControllerStaticServant.setRidlStaticORB(orb);
            

            // Do an invocation on the reference to see how it works
            // directly in the server.

            rrmiiIServant.sayHello();
            ridlStaticPoa.syncOK("idlStaticPoaFromInsideServer");
            ridlStatic.syncOK("idlStaticFromInsideServer");

            U.sop(main + " ready");
            U.sop(Options.defServerHandshake);
            System.out.flush();

            synchronized (ColocatedClientServer.signal) {
                ColocatedClientServer.signal.notifyAll();
            }

            orb.run(); // The ORB would not exist in standard rmi-iiop.

        } catch (Exception e) {
            U.sopUnexpectedException(main, e);
            System.exit(1);
        }
        System.exit(Controller.SUCCESS);
    }

}

// End of file.

