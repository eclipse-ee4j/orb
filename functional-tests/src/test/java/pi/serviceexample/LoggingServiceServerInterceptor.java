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
// Created       : 2001 Jun 05 (Tue) 19:22:46 by Harold Carr.
// Last Modified : 2001 Sep 24 (Mon) 21:34:05 by Harold Carr.
//

package pi.serviceexample;

import org.omg.CosNaming.*;
import org.omg.CosNaming.NamingContextPackage.*;
import org.omg.CORBA.Any;
import org.omg.CORBA.BAD_PARAM;
import org.omg.CORBA.ORB;
import org.omg.CORBA.TCKind;
import org.omg.IOP.ServiceContext;
import org.omg.PortableInterceptor.ClientRequestInterceptor;
import org.omg.PortableInterceptor.ClientRequestInfo;
import org.omg.PortableInterceptor.Current;
import org.omg.PortableInterceptor.InvalidSlot;
import org.omg.PortableInterceptor.ServerRequestInterceptor;
import org.omg.PortableInterceptor.ServerRequestInfo;

public class LoggingServiceServerInterceptor
    extends org.omg.CORBA.LocalObject
    implements ClientRequestInterceptor,
               ServerRequestInterceptor
{
    private NamingContext nameService;
    private LoggingService loggingService;
    private Current piCurrent;
    private int outCallIndicatorSlotId;
    private static final int serviceContextId = 100001;
    private static final byte[] serviceContextData = {1};

    // Returns a reference to the logging process.

    private LoggingService loggingService()
    {
        if (loggingService == null) {
            NameComponent path[] =
                { new NameComponent("LoggingService", "") };
            try {
                loggingService = 
                    LoggingServiceHelper.narrow(nameService.resolve(path));
            } catch (Throwable t) {
                System.out.println("Exception handling not shown.");
            }
        }
        return loggingService;
    }

    public LoggingServiceServerInterceptor(NamingContext nameService,
                                           Current piCurrent,
                                           int outCallIndicatorSlotId)
    {
        this.nameService = nameService;
        this.piCurrent = piCurrent;
        this.outCallIndicatorSlotId = outCallIndicatorSlotId;
    }

    //
    // Interceptor operations
    //

    public String name() 
    {
        return "LoggingServiceServerInterceptor";
    }

    public void destroy() 
    {
    }

    //
    // ClientRequestInterceptor operations
    //

    public void send_request(ClientRequestInfo ri)
    {

        // If the server interceptor sets the recursion slot then
        // put in the service context so the server doesn't make
        // the call again in the case where the server side interceptor
        // is colocated in the same ORB as the object being invoked.

        try {
            Any indicator = ri.get_slot(outCallIndicatorSlotId);
            if (indicator.type().kind().equals(TCKind.tk_boolean)) {
                ServiceContext serviceContext =
                    new ServiceContext(serviceContextId, serviceContextData);
                ri.add_request_service_context(serviceContext, false);
            }
        } catch (InvalidSlot e) {
            System.out.println("Exception handling not shown.");
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
        log(ri, "receive_request_service_contexts");
    }

    public void receive_request(ServerRequestInfo ri)
    {
        log(ri, "receive_request");
    }

    public void send_reply(ServerRequestInfo ri)
    {
        log(ri, "send_reply");
    }

    public void send_exception(ServerRequestInfo ri)
    {
        log(ri, "send_exception");
    }

    public void send_other(ServerRequestInfo ri)
    {
        log(ri, "send_other");
    }

    //
    // Utilities.
    //

    public void log(ServerRequestInfo ri, String point)
    {
        // This is only relevant for the colocated example.
        // Do not attempt to log until the logging service object
        // has been bound in naming.  Otherwise the attempt to call
        // rebind on naming will call log which will fail.
        if (! ColocatedServers.colocatedBootstrapDone) {
            return;
        }

        // IMPORTANT:
        // The conditional logging of the invocation is only necessary
        // if there is a chance that the object being invoked is colocated
        // in the same ORB as this interceptor.  Otherwise the outcall to 
        // the logging service can be made unconditionally.

        // Always set the recursion slot.

        Any indicator = ORB.init().create_any();
        indicator.insert_boolean(true);
        try {
            piCurrent.set_slot(outCallIndicatorSlotId, indicator);
        } catch (InvalidSlot e) {
            System.out.println("Exception handling not shown.");
        }

        // Make the out call if you have not already done so.

        try {

            // Only the presence of the service context counts.
            // The data is ignored.

            ri.get_request_service_context(serviceContextId);
        } catch (BAD_PARAM e) {
            // Recursion indicator not set so make the call.
            loggingService().log(ri.operation() + " " + point);
        }
    }
}

// End of file.

