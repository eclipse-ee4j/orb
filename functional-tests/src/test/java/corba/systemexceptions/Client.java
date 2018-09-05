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

package corba.systemexceptions;

import com.sun.corba.ee.impl.misc.ORBUtility;
import corba.framework.Controller;
import corba.hcks.C;
import corba.hcks.U;
import org.omg.CORBA.CompletionStatus;
import org.omg.CORBA.ORB;
import org.omg.CORBA.SystemException;
import org.omg.PortableInterceptor.ClientRequestInfo;
import org.omg.PortableInterceptor.ClientRequestInterceptor;
import org.omg.PortableInterceptor.ForwardRequest;
import org.omg.PortableInterceptor.ORBInitInfo;
import org.omg.PortableInterceptor.ORBInitializer;

import javax.naming.InitialContext;

public class Client extends org.omg.CORBA.LocalObject 
    implements ORBInitializer, ClientRequestInterceptor {

    public static final String baseMsg = Client.class.getName();
    public static final String main = baseMsg + ".main";

    public static ORB orb;
    public static InitialContext initialContext;
    
    private static String excs[] = { 
        "org.omg.CORBA.ACTIVITY_COMPLETED", "org.omg.CORBA.ACTIVITY_REQUIRED",
        "org.omg.CORBA.BAD_QOS", "org.omg.CORBA.CODESET_INCOMPATIBLE",
        "org.omg.CORBA.INVALID_ACTIVITY", "org.omg.CORBA.REBIND",
        "org.omg.CORBA.TIMEOUT", "org.omg.CORBA.TRANSACTION_MODE",
        "org.omg.CORBA.TRANSACTION_UNAVAILABLE", "org.omg.CORBA.UNKNOWN" };
        
    static int counter; // counter

    public static void main(String[] av) {

        try {
            U.sop(main + " starting");

            if (! ColocatedClientServer.isColocated) {
                U.sop(main + " : creating ORB.");
                orb = ORB.init(av, null);
                U.sop(main + " : creating InitialContext.");
                initialContext = C.createInitialContext(orb);
            }

            // RMI invocations

            rmiiI rmiiIPOA = (rmiiI) U.lookupAndNarrow(Server.rmiiIPOA,
                                                 rmiiI.class, initialContext);
            U.sop("\nRMI invocations:\n");
            int i = 0;
            for (counter = 0, i = 0; i < 10; i++, counter++) {
                try {
                    rmiiIPOA.invoke(i);
                } catch (java.rmi.RemoteException re) {
                    SystemException se = (SystemException) re.getCause();
                    String name = se.getClass().getName();
                    U.sop("name: " + name + ", minorCode: " + se.minor +
                          ", completed: " + 
                          ((se.completed.value() == 
                           CompletionStatus._COMPLETED_YES) ?
                           "true" : "false") + "\n");
                    if (!(name.equals(excs[i]))) {
                        throw new RuntimeException("Test Failed");
                    }
                }
            }

            // IDL invocations

            idlI idlIPOA = idlIHelper.narrow(U.resolve(Server.idlIPOA, orb));
            U.sop("IDL invocations:\n");
            for (counter = 0, i = 0; i < 10; i++, counter++) {
                try {
                    idlIPOA.invoke(i);
                } catch (org.omg.CORBA.SystemException se) {
                    String name = se.getClass().getName();
                    U.sop("name: " + name + ", minorCode: " + se.minor +
                          ", completed: " + 
                          ((se.completed.value() == 
                           CompletionStatus._COMPLETED_YES) ?
                           "true" : "false") + "\n");
                    if (!(name.equals(excs[i]))) {
                        throw new RuntimeException("Test Failed");
                    }
                }
            }

            orb.shutdown(true);

        } catch (Exception e) {
            U.sopUnexpectedException(main + " : ", e);
            System.exit(1);
        }
        U.sop(main + " ending successfully");
        System.exit(Controller.SUCCESS);
    }

    ////////////////////////////////////////////////////
    //    
    // ORBInitializer interface implementation.
    //

    public void pre_init(ORBInitInfo info) 
    {
    }

    public void post_init(ORBInitInfo info) 
    {
        // register the interceptors.
        try {
            info.add_client_request_interceptor(this);
        } catch (org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName e) {
            throw new org.omg.CORBA.INTERNAL();
        }
        U.sop("ORBInitializer.post_init completed");
    }

    ////////////////////////////////////////////////////
    //
    // implementation of the Interceptor interface.
    //

    public String name() 
    {
        return "ClientInterceptor";
    }

    public void destroy() 
    {
    }

    ////////////////////////////////////////////////////
    //    
    // implementation of the ClientInterceptor interface.
    //

    public void send_request(ClientRequestInfo ri) throws ForwardRequest 
    {
    }

    public void send_poll(ClientRequestInfo ri) 
    {
    }

    public void receive_reply(ClientRequestInfo ri) 
    {    
    }

    public void receive_exception(ClientRequestInfo ri) throws ForwardRequest 
    {
        String repID = ri.received_exception_id();
        String className = ORBUtility.classNameOf(repID);
        U.sop("receive_exception.repID: " + repID);
        U.sop("receive_exception.className: " + className);
        if ( !(className.equals(excs[counter])) ) {
            throw new RuntimeException("Test Failed");
        }
    }

    public void receive_other(ClientRequestInfo ri) throws ForwardRequest 
    {
    }
}

// End of file.

