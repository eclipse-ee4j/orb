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
// Created       : 2001 May 23 (Wed) 19:46:30 by Harold Carr.
// Last Modified : 2001 Sep 24 (Mon) 21:42:34 by Harold Carr.
//

package pi.serviceexample;

import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;
import org.omg.CORBA.TCKind;
import org.omg.IOP.ServiceContext;
import org.omg.PortableInterceptor.ClientRequestInterceptor;
import org.omg.PortableInterceptor.ClientRequestInfo;
import org.omg.PortableInterceptor.Current;
import org.omg.PortableInterceptor.InvalidSlot;

public class LoggingServiceClientInterceptor
    extends org.omg.CORBA.LocalObject
    implements ClientRequestInterceptor
{
    private LoggingService loggingService;
    private Current piCurrent;
    private int outCallIndicatorSlotId;

    public LoggingServiceClientInterceptor(LoggingService loggingService,
                                           Current piCurrent,
                                           int outCallIndicatorSlotId)
    {
        this.loggingService = loggingService;
        this.piCurrent = piCurrent;
        this.outCallIndicatorSlotId = outCallIndicatorSlotId;
    }

    //
    // Interceptor operations
    //

    public String name() 
    {
        return "LoggingServiceClientInterceptor";
    }

    public void destroy() 
    {
    }

    //
    // ClientRequestInterceptor operations
    //

    public void send_request(ClientRequestInfo ri)
    {
        log(ri, "send_request");
    }

    public void send_poll(ClientRequestInfo ri)
    {
        log(ri, "send_poll");
    }

    public void receive_reply(ClientRequestInfo ri)
    {
        log(ri, "receive_reply");
    }

    public void receive_exception(ClientRequestInfo ri)
    {
        log(ri, "receive_exception");
    }

    public void receive_other(ClientRequestInfo ri)
    {
        log(ri, "receive_other");
    }

    //
    // Utilities.
    //

    public void log(ClientRequestInfo ri, String point)
    {
        // IMPORTANT: Always set the TSC out call indicator in case
        // other interceptors make outcalls for this request.
        // Otherwise the outcall will not be set for the other interceptor's
        // outcall resulting in infinite recursion.

        Any indicator = ORB.init().create_any();
        indicator.insert_boolean(true);
        try {
            piCurrent.set_slot(outCallIndicatorSlotId, indicator);
        } catch (InvalidSlot e) { }

        try {
            indicator = ri.get_slot(outCallIndicatorSlotId);

            // If the RSC out call slot is not set then log this invocation.
            // If it is set that indicates the interceptor is servicing the
            // invocation of loggingService itself.  In that case do
            // nothing (to avoid infinite recursion).

            if (indicator.type().kind().equals(TCKind.tk_null)) {
                loggingService.log(ri.operation() + " " + point);
            }
        } catch (InvalidSlot e) {
            System.out.println("Exception handling not shown.");            
        }
    }
}

// End of file.

