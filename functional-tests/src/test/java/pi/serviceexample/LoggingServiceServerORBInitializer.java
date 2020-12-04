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
// Created       : 2001 May 23 (Wed) 20:32:27 by Harold Carr.
// Last Modified : 2001 Sep 20 (Thu) 21:02:59 by Harold Carr.
//

package pi.serviceexample;

import org.omg.CosNaming.*;
import org.omg.CosNaming.NamingContextPackage.*;
import org.omg.PortableInterceptor.Current;
import org.omg.PortableInterceptor.CurrentHelper;
import org.omg.PortableInterceptor.ORBInitInfo;

public class LoggingServiceServerORBInitializer 
    extends org.omg.CORBA.LocalObject
    implements org.omg.PortableInterceptor.ORBInitializer
{
    public void pre_init(ORBInitInfo info)
    {
    }

    public void post_init(ORBInitInfo info)
    {
        try {

            // Create and register the logging service interceptor.
            // Give that interceptor references to the NameService and
            // PICurrent to avoid further lookups (i.e., optimization).
            // More importantly, allocate and give the interceptor
            // a slot id which is will use to tell itself not to
            // log calls that the interceptor makes to the logging process.

            NamingContext nameService = 
                NamingContextHelper.narrow(
                    info.resolve_initial_references("NameService"));

            Current piCurrent =
                CurrentHelper.narrow(
                    info.resolve_initial_references("PICurrent"));

            int outCallIndicatorSlotId = info.allocate_slot_id();

            LoggingServiceServerInterceptor interceptor =
                new LoggingServiceServerInterceptor(nameService,
                                                    piCurrent,
                                                    outCallIndicatorSlotId);

            info.add_client_request_interceptor(interceptor);
            info.add_server_request_interceptor(interceptor);
        } catch (Throwable t) {
            System.out.println("Exception handling not shown.");
        }
    }
}
 
// End of file.
