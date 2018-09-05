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
// Created       : 2001 May 23 (Wed) 20:32:27 by Harold Carr.
// Last Modified : 2001 Sep 24 (Mon) 19:16:26 by Harold Carr.
//

package pi.serviceexample;

import org.omg.CosNaming.*;
import org.omg.CosNaming.NamingContextPackage.*;
import org.omg.PortableInterceptor.Current;
import org.omg.PortableInterceptor.CurrentHelper;
import org.omg.PortableInterceptor.ORBInitInfo;

public class LoggingServiceClientORBInitializer 
    extends org.omg.CORBA.LocalObject
    implements org.omg.PortableInterceptor.ORBInitializer
{
    public void pre_init(ORBInitInfo info)
    {
    }

    public void post_init(ORBInitInfo info)
    {
        try {

            // Get a reference to the LoggingService object.

            NamingContext nameService = 
                NamingContextHelper.narrow(
                    info.resolve_initial_references("NameService"));

            NameComponent path[] =
                { new NameComponent("LoggingService", "") };
            LoggingService loggingService = 
                LoggingServiceHelper.narrow(nameService.resolve(path));

            // Get a reference to TSC PICurrent.

            Current piCurrent =
                CurrentHelper.narrow(
                    info.resolve_initial_references("PICurrent"));

            // Allocate a slot id to use for the interceptor to indicate
            // that it is making an outcall.  This is used to avoid
            // infinite recursion.

            int outCallIndicatorSlotId = info.allocate_slot_id();

            // Create (with the above data) and register the client
            // side interceptor.

            LoggingServiceClientInterceptor interceptor =
                new LoggingServiceClientInterceptor(loggingService, 
                                                    piCurrent,
                                                    outCallIndicatorSlotId);

            info.add_client_request_interceptor(interceptor);
        } catch (Throwable t) {
            System.out.println("Exception handling not shown.");
        }
    }
}
 
// End of file.
