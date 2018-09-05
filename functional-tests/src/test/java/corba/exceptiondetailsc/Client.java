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
// Last Modified : 2003 Dec 17 (Wed) 10:17:24 by Harold Carr.
//

package corba.exceptiondetailsc;

import java.rmi.RemoteException;
import javax.naming.InitialContext;
import org.omg.CORBA.FREE_MEM;
import org.omg.CORBA.ORB;
import org.omg.CORBA.UNKNOWN;

import corba.framework.Controller;
import corba.hcks.C;
import corba.hcks.U;

public class Client 
{
    public static final String baseMsg = Client.class.getName();
    public static final String main = baseMsg + ".main";

    public static final String IDLConnect     = "IDL/Connect";
    public static final String IDLPOA         = "IDL/POA";
    public static final String RMIIIOPConnect = "RMI-IIOP/Connect";
    public static final String RMIIIOPPOA     = "RMI-IIOP/POA";
    public static final String SYSTEM         = "SYSTEM_EXCEPTION";
    public static final String USER           = "USER_EXCEPTION";
    public static final String RUNTIME        = "RuntimeException";
    
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

    public static boolean isColocated = false;
    public static boolean debug = false;

    public static int numErrors = 0;

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

            U.lf();
            U.sop("+++++++++++++++++++++++++Looking up IDL references.");
            U.lf();

            idlIConnect = idlIHelper.narrow(U.resolve(Server.idlIConnect,orb));
            idlIPOA     = idlIHelper.narrow(U.resolve(Server.idlIPOA,    orb));

            U.lf();
            U.sop("+++++++++++++++++++++++++Looking up RMI references.");
            U.lf();
            rmiiIConnect = (rmiiI)
                U.lookupAndNarrow(Server.rmiiIConnect,
                                  rmiiI.class, initialContext);
            rmiiIPOA = (rmiiI)
                U.lookupAndNarrow(Server.rmiiIPOA, rmiiI.class,initialContext);

            /* REVISIT: investigate hang
            rmiiIPOA = (rmiiI)
                U.lookupAndNarrow(C.rmiiSL, rmiiI.class, initialContext);
            */

            U.lf();
            U.sop("+++++++++++++++++++++++++Making IDL/Connect calls.");
            U.lf();
            try {
                idlIConnect.raise_system_exception(idlIConnectArg);
            } catch (Throwable t) {
                processThrowable(IDLConnect, SYSTEM, t,
                                 new FREE_MEM(),
                                 "idlIServantConnect.raise_system_exception");
            }
            try {
                idlIConnect.raise_user_exception(idlIConnectArg);
            } catch (Throwable t) {
                processThrowable(IDLConnect, USER, t,
                                 new idlException());
            }
            try {
                idlIConnect.raise_runtime_exception(idlIConnectArg);
            } catch (Throwable t) {
                processThrowable(IDLConnect, RUNTIME, t,
                                 new UNKNOWN(),
                                 "idlIServantConnect.raise_runtime_exception");
            }

            U.lf();
            U.sop("+++++++++++++++++++++++++Making IDL/POA calls.");
            U.lf();
            try {
                idlIPOA.raise_system_exception(idlIPOAArg);
            } catch (Throwable t) {
                processThrowable(IDLPOA, SYSTEM, t,
                                 new FREE_MEM(),
                                 "idlIServantPOA.raise_system_exception");
            }
            try {
                idlIPOA.raise_user_exception(idlIPOAArg);
            } catch (Throwable t) {
                processThrowable(IDLPOA, USER, t,
                                 new idlException());
            }

            try {
                idlIPOA.raise_runtime_exception(idlIPOAArg);
            } catch (Throwable t) {
                processThrowable(IDLPOA, RUNTIME, t,
                                 new UNKNOWN(),
                                 "idlIServantPOA.raise_runtime_exception");
            }

            U.lf();
            U.sop("+++++++++++++++++++++++++Making RMI-IIOP/Connect calls.");
            U.lf();
            try {
                rmiiIConnect.raiseSystemException(rmiiIConnectArg);
            } catch (Throwable t) {
                String message;
                if (isColocated) {
                    message = "FREE_MEM";
                } else {
                    message = "rmiiIServantConnect.raiseSystemException";
                }
                processThrowable(RMIIIOPConnect, SYSTEM, t,
                                 new RemoteException(), // wraps FREE_MEM
                                 message);
            }
            try {
                rmiiIConnect.raiseUserException(rmiiIConnectArg);
            } catch (Throwable t) {
                processThrowable(RMIIIOPConnect, USER, t,
                                 new rmiiException("dummy"),
                                 rmiiIConnectArg);
            }
            try {
                rmiiIConnect.raiseRuntimeException(rmiiIConnectArg);
            } catch (Throwable t) {
                processThrowable(RMIIIOPConnect, RUNTIME, t,
                                 new RuntimeException(),
                                 rmiiIConnectArg);
            }

            U.lf();
            U.sop("+++++++++++++++++++++++++Making RMI-IIOP/POA calls.");
            U.lf();
            try {
                rmiiIPOA.raiseSystemException(rmiiIPOAArg);
            } catch (Throwable t) {
                String message;
                if (isColocated) {
                    message = "FREE_MEM";
                } else {
                    message = "rmiiIServantPOA.raiseSystemException";
                }
                processThrowable(RMIIIOPPOA, SYSTEM, t,
                                 new RemoteException(),
                                 message);
            }
            try {
                rmiiIPOA.raiseUserException(rmiiIPOAArg);
            } catch (Throwable t) {
                processThrowable(RMIIIOPPOA, USER, t,
                                 new rmiiException("dummy"),

                                 rmiiIPOAArg);
            }

            try {
                rmiiIPOA.raiseRuntimeException(rmiiIPOAArg);
            } catch (Throwable t) {
                processThrowable(RMIIIOPPOA, RUNTIME, t,
                                 new RuntimeException(),
                                 rmiiIPOAArg);
            }

            if (numErrors == 0) {
                U.lf();
                U.sop("+++++++++++++++++++++++++PASSED");
                U.lf();
            } else {
                throw new RuntimeException("numError != 0");
            }

            orb.shutdown(true);

        } catch (Exception e) {
            U.lf();
            U.sopUnexpectedException(main + " : ", e);
            U.sop("+++++++++++++++++++++++++FAILED");
            U.lf();
            System.exit(1);
        }
        U.lf();
        U.sop(main + " ending successfully");
        System.exit(Controller.SUCCESS);
    }

    public static void processThrowable(String servantType,
                                        String exceptionCategory,
                                        Throwable got,
                                        Throwable expected)
    {
        processThrowable(servantType, exceptionCategory, got, expected, null);
    }

    public static void processThrowable(String servantType,
                                        String exceptionCategory,
                                        Throwable got,
                                        Throwable expected,
                                        String messageSubstring)
    {
        boolean failType = false;
        boolean failMessage = false;

        U.lf();
        U.sop("--------------------------------------------------");
        U.sop(servantType + " " + exceptionCategory);
        if (got.getClass().isInstance(expected)) {
            U.sop("Exception Type PASSED");
        } else {
            U.sop("Exception Type FAIL");
            numErrors++;
            failType = true;
        }
        if (messageSubstring != null) {
            if (got.getMessage().indexOf(messageSubstring) != -1) {
                U.sop("-----Exception Message PASSED-----");
                U.sop("-----BEGIN passing stack trace:");
                got.printStackTrace(System.out);
                U.sop("-----END passing stack trace");
            } else {
                U.sop("Exception Message FAIL");
                numErrors++;
                failMessage = true;
            }
        }
        if (failType) {
            U.sop("exception type expected: " + expected);
            U.sop("exception type received: " + got);
        }
        if (failMessage) {
            U.sop("exception message (substring) expected: " + messageSubstring);
            U.sop("exception message received: " + got.getMessage());
        }

        if (debug) {
            if (isColocated) {
                U.sop("++++++++++++++++++++++++++++++++++++++++++++++++++");
                got.printStackTrace(System.out);
                U.sop("++++++++++++++++++++++++++++++++++++++++++++++++++");
            }
        }
    }
}

// End of file.

