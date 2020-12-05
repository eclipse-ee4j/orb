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
// Created       : 2002 Oct 16 (Wed) 08:32:24 by Harold Carr.
// Last Modified : 2003 Mar 17 (Mon) 20:51:22 by Harold Carr.
//

package mantis.m4764130;

import java.util.Properties;
import org.omg.CORBA.BAD_PARAM;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.*;
import org.omg.IOP.ServiceContext;
import org.omg.PortableInterceptor.ClientRequestInterceptor;
import org.omg.PortableInterceptor.ClientRequestInfo;
import org.omg.PortableInterceptor.ForwardRequest;
import org.omg.PortableInterceptor.ORBInitInfo;
import org.omg.PortableInterceptor.ORBInitializer;
import org.omg.PortableInterceptor.ServerRequestInterceptor;
import org.omg.PortableInterceptor.ServerRequestInfo;
import com.sun.corba.ee.spi.servicecontext.SendingContextServiceContext;
import com.sun.corba.ee.spi.misc.ORBConstants;

public class Interceptor
    extends
        org.omg.CORBA.LocalObject
    implements
        ClientRequestInterceptor,
        ServerRequestInterceptor,
        ORBInitializer
{
    public int numberOfClientHelloInvocations = 0;
    //
    // Interceptor operations
    //

    public String name() 
    {
        return this.getClass().getName();
    }

    public void destroy() 
    {
    }

    //
    // ClientRequestInterceptor operations
    //

    public void send_request(ClientRequestInfo ri)
        throws
            ForwardRequest
    {
        System.out.println(ri.operation());
        if (ri.operation().equals("hello")) {
            numberOfClientHelloInvocations++;
            if (numberOfClientHelloInvocations == 1) {
                throw new ForwardRequest(ri.target());
            }
        }
    }

    public void send_poll(ClientRequestInfo ri)
    {
    }

    public void receive_reply(ClientRequestInfo ri)
    {
    }

    public void receive_exception(ClientRequestInfo ri)
    {
    }

    public void receive_other(ClientRequestInfo ri)
    {
    }

    //
    // ServerRequestInterceptor operations
    //

    public void receive_request_service_contexts(ServerRequestInfo ri)
    {
        System.out.println(ri.operation());
        try {
            ServiceContext serviceContext =
                ri.get_request_service_context(SendingContextServiceContext.SERVICE_CONTEXT_ID);
        } catch (BAD_PARAM e) {
            // Not present.
            System.out.println("SendingContextServiceContext not present");
            System.exit(1);
        }
    }

    public void receive_request(ServerRequestInfo ri)
    {
    }

    public void send_reply(ServerRequestInfo ri)
    {
    }

    public void send_exception(ServerRequestInfo ri)
    {
    }

    public void send_other(ServerRequestInfo ri)
    {
    }

    //
    // Initializer operations.
    //

    public void pre_init(ORBInitInfo info)
    {
        System.out.println(this.getClass().getName() + " .pre_init");
        try {
            // NOTE: The client only needs the client side points.
            // The server only needs the server side points.
            // It just saves me time just to write one interceptor/initializer.
            info.add_client_request_interceptor(new Interceptor());
            info.add_server_request_interceptor(new Interceptor());
        } catch (Throwable t) {
            System.out.println("Cannot register interceptor: " + t);
        }
    }
    
    public void post_init(ORBInitInfo info) {}
}

// End of file.
