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
// Last Modified : 2004 Jan 31 (Sat) 10:06:37 by Harold Carr.
//

package corba.giopheaderpadding;

import javax.naming.InitialContext;
import org.omg.CORBA.ORB;

import corba.framework.Controller;
import corba.hcks.C;
import corba.hcks.U;

import com.sun.corba.ee.impl.protocol.MessageMediatorImpl;

import java.lang.reflect.*;
import org.omg.PortableInterceptor.*;

public class Client extends org.omg.CORBA.LocalObject
    implements ORBInitializer, ClientRequestInterceptor {

    public static final String baseMsg = Client.class.getName();
    public static final String main = baseMsg + ".main";
    
    public static ORB orb;
    public static InitialContext initialContext;

    public static rmiiI rmiiIPOA;

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

            U.sop("CLIENT.fooA: " + rmiiIPOA.fooA((byte)5));
            rmiiIPOA.fooB();
            U.sop("CLIENT.fooB completed");

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
        U.sop("send_request called : " + ri.operation());        
    }

    public void send_poll(ClientRequestInfo ri) 
    {
        U.sop("send_poll called : " + ri.operation());
    }

    public void receive_reply(ClientRequestInfo ri) 
    {    
        String opName = ri.operation();
        U.sop("receive_reply.opName: " + opName);

        if ( ! (opName.equals("fooA") || opName.equals("fooB")) ) {
            return;
        }

        Class riClass = ri.getClass();
        MessageMediatorImpl cri;
        try {
            Field riMember = riClass.getDeclaredField("messageMediator");
            riMember.setAccessible(true);
            cri = (MessageMediatorImpl) riMember.get(ri);
        } catch (Throwable e) { 
            e.printStackTrace(System.out); 
            throw new RuntimeException("impl class instrospection failed", e);
        }

        // fooA.buffer: [header + padding + body (1 byte)]
        // fooA.buffer: [header + body (1 byte)]

        // get header size
        int size = cri.getReplyHeader().getSize();
        U.sop("reply message size: " + size);

        if (opName.equals("fooA")) {
            if (size != 41) {
                throw new RuntimeException("header padding error");
            }
        } else { // opName == fooB
            if (size != 34) {
                throw new RuntimeException("header padding error");
            }
        }
    }

    public void receive_exception(ClientRequestInfo ri) throws ForwardRequest 
    {
        U.sop("receive_exception called : " + ri.operation());
    }

    public void receive_other(ClientRequestInfo ri) throws ForwardRequest 
    {
        U.sop("receive_other called : " + ri.operation());
    }
}

// End of file.

