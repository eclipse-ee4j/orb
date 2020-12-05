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
// Created       : 2000 Sep 22 (Fri) 11:34:53 by Harold Carr.
// Last Modified : 2001 Feb 05 (Mon) 15:01:52 by Harold Carr.
//

package corba.hcks;

import org.omg.CosNaming.*;
import org.omg.CosNaming.NamingContextPackage.*;

import org.omg.PortableInterceptor.ORBInitInfo;
import org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName;
import org.omg.PortableInterceptor.Current;
import org.omg.PortableInterceptor.CurrentHelper;

public class MyORBInitializer 
    extends
        org.omg.CORBA.LocalObject
    implements
        org.omg.PortableInterceptor.ORBInitializer
{
    public static final String baseMsg = MyORBInitializer.class.getName();

    public void pre_init(ORBInitInfo info)
    {
        try {
            MyInterceptor interceptor = new MyInterceptor();
            info.add_client_request_interceptor(interceptor);
            info.add_server_request_interceptor(interceptor);
            U.sop(baseMsg + ".pre_init");
        } catch (Throwable t) {
            U.sopUnexpectedException(baseMsg, t);
        }
    }

    public void post_init(ORBInitInfo info)
    {
        try {
            Current piCurrent = 
                CurrentHelper.narrow(
                    info.resolve_initial_references(U.PICurrent));
            NamingContext nameService =
                NamingContextHelper.narrow(
                   info.resolve_initial_references(U.NameService));;

            SsPicInterceptor.sPic1ASlotId = info.allocate_slot_id();
            SsPicInterceptor.sPic1BSlotId = info.allocate_slot_id();
            int sPic2ASlotId = info.allocate_slot_id();
            int sPic2BSlotId = info.allocate_slot_id();

            SsPicInterceptor sPicAInt = 
                new SsPicInterceptor(SsPicInterceptor.sPic1AServiceContextId,
                                     SsPicInterceptor.sPic2AServiceContextId,
                                     SsPicInterceptor.sPic1ASlotId,
                                     sPic2ASlotId,
                                     piCurrent,
                                     nameService,
                                     "sPicA");
            info.add_client_request_interceptor(sPicAInt);
            info.add_server_request_interceptor(sPicAInt);
            
            
            SsPicInterceptor sPicBInt = 
                new SsPicInterceptor(SsPicInterceptor.sPic1BServiceContextId,
                                     SsPicInterceptor.sPic2BServiceContextId,
                                     SsPicInterceptor.sPic1BSlotId,
                                     sPic2BSlotId,
                                     piCurrent,
                                     nameService,
                                     "sPicB");
            info.add_client_request_interceptor(sPicBInt);
            info.add_server_request_interceptor(sPicBInt);
            U.sop(baseMsg + ".post_init");
        } catch (Throwable t) {
            U.sopUnexpectedException(baseMsg, t);
        }
    }

}
 
// End of file.
