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
// Created       : 2002 Jan 17 (Thu) 15:26:57 by Harold Carr.
// Last Modified : 2002 Jan 17 (Thu) 15:37:24 by Harold Carr.
//

package corba.purgecalls;

import corba.hcks.U;
import org.omg.PortableInterceptor.ORBInitializer;
import org.omg.PortableInterceptor.ORBInitInfo;

public class ClientORBInitializer 
    extends
        org.omg.CORBA.LocalObject
    implements
        org.omg.PortableInterceptor.ORBInitializer
{
    public static final String baseMsg = ClientORBInitializer.class.getName();

    public void pre_init(ORBInitInfo info)
    {
        try {
            ClientInterceptor interceptor = new ClientInterceptor();
            info.add_client_request_interceptor(interceptor);
            U.sop(baseMsg + ".pre_init");
        } catch (Throwable t) {
            U.sopUnexpectedException(baseMsg, t);
        }
    }

    public void post_init(ORBInitInfo info)
    {
    }

}
 
// End of file.
