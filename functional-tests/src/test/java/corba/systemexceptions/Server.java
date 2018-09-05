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
// Last Modified : 2004 Jan 31 (Sat) 11:12:48 by Harold Carr.
//

package corba.systemexceptions;

import javax.naming.InitialContext;

import org.omg.PortableServer.POA;
import org.omg.PortableServer.Servant;

import corba.framework.Controller;
import corba.framework.Options;
import corba.hcks.C;
import corba.hcks.U;

import com.sun.corba.ee.spi.orb.ORB;

import java.rmi.Remote; 
import java.rmi.RemoteException; 
import javax.rmi.PortableRemoteObject;

import org.omg.CORBA.*;

interface rmiiI extends Remote {
    void invoke(int excType) throws RemoteException;
}

class rmiiIServantPOA extends PortableRemoteObject implements rmiiI {

    rmiiIServantPOA() throws RemoteException {
        // DO NOT CALL SUPER - that would connect the object.
    }

    public void invoke(int excType) {
        Server.invoke(excType);
    }
}

class idlIServantPOA extends idlIPOA {

    public void invoke(int excType) {
        Server.invoke(excType);
    }
}

public class Server extends org.omg.CORBA.LocalObject {

    public static final String baseMsg = Server.class.getName();
    public static final String main = baseMsg + ".main";
    public static final String thisPackage = 
        Server.class.getPackage().getName();

    public static final String rmiiIServantPOA_Tie = 
        thisPackage + "._rmiiIServantPOA_Tie";

    public static final String rmiiIPOA = "rmiiIPOA";
    public static final String idlIPOA = "idlIPOA";

    public static ORB orb;
    public static InitialContext initialContext;

    static void invoke(int excType) {

        switch (excType) {

        case 0: 
            U.sop("ACTIVITY_COMPLETED");
            throw new ACTIVITY_COMPLETED("ACTIVITY_COMPLETED", 
                                         100, CompletionStatus.COMPLETED_YES);
        case 1: 
            U.sop("ACTIVITY_REQUIRED");
            throw new ACTIVITY_REQUIRED("ACTIVITY_REQUIRED", 
                                        101, CompletionStatus.COMPLETED_YES);
        case 2: 
            U.sop("BAD_QOS");
            throw new BAD_QOS("BAD_QOS", 102, CompletionStatus.COMPLETED_YES);
        case 3: 
            U.sop("CODESET_INCOMPATIBLE");
            throw new CODESET_INCOMPATIBLE("CODESET_INCOMPATIBLE",
                                           103,
                                           CompletionStatus.COMPLETED_YES);
        case 4:
            U.sop("INVALID_ACTIVITY");
            throw new INVALID_ACTIVITY("INVALID_ACTIVITY", 
                                       104, CompletionStatus.COMPLETED_YES);
        case 5:
            U.sop("REBIND");
            throw new REBIND("REBIND", 105, CompletionStatus.COMPLETED_YES);
        case 6:
            U.sop("TIMEOUT");
            throw new TIMEOUT("TIMEOUT", 106, CompletionStatus.COMPLETED_YES);
        case 7:
            U.sop("TRANSACTION_MODE");
            throw new TRANSACTION_MODE("TRANSACTION_MODE", 
                                       107, CompletionStatus.COMPLETED_YES);
        case 8:
            U.sop("TRANSACTION_UNAVAILABLE");
            throw new TRANSACTION_UNAVAILABLE("TRANSACTION_UNAVAILABLE",
                                              108,
                                              CompletionStatus.COMPLETED_YES);
        default:
            U.sop("UNKNOWN");
            throw new UNKNOWN("UNKNOWN", 109, CompletionStatus.COMPLETED_YES);
        }
    }

    public static void main(String[] av) {

        try {
            U.sop(main + " starting");

            if (! ColocatedClientServer.isColocated) {
                U.sop(main + " : creating ORB.");
                orb = (ORB) ORB.init(av, null);
                U.sop(main + " : creating InitialContext.");
                initialContext = C.createInitialContext(orb);
            }

            POA rootPOA = U.getRootPOA(orb);
            rootPOA.the_POAManager().activate();

            // RMI-IIOP references.
            U.sop("Creating/binding RMI-IIOP references.");
            Servant servant = (Servant)
                javax.rmi.CORBA.Util.getTie(new rmiiIServantPOA());
            U.createWithServantAndBind(rmiiIPOA, servant, rootPOA, orb);

            // IDL references.
            U.sop("Creating/binding IDL references.");
            U.createWithServantAndBind(idlIPOA,
                                       new idlIServantPOA(), rootPOA, orb);

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


