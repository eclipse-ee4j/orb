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
// Created       : 1999 by Harold Carr.
// Last Modified : 2002 Dec 04 (Wed) 21:43:39 by Harold Carr.
//

package corba.hcks;

import com.sun.corba.ee.impl.orb.ORBImpl;
import com.sun.corba.ee.spi.servicecontext.ServiceContexts;

public class MyPOAORB 
    extends 
        ORBImpl 
{
    public static final String baseMsg = MyPOAORB.class.getName();

    public static boolean showProprietaryHooks = false;

    public static boolean throwRuntimeExceptionInSendingRequestServiceContexts
        = false;

    protected void sendingRequestServiceContexts( ServiceContexts scs ) 
    {
        if (showProprietaryHooks) {
            U.sop(baseMsg + ".sendingRequestServiceContexts - " + scs);
        }
        if (throwRuntimeExceptionInSendingRequestServiceContexts) {
            throw new RuntimeException(baseMsg +
                                       ".sendingRequestServiceContexts");
        }
    }

    protected void receivedReplyServiceContexts(ServiceContexts scs)
    {
        if (showProprietaryHooks) {
            U.sop(baseMsg + ".receivedReplyServiceContexts - " + scs);
        }
    }

    protected void receivedRequestServiceContexts(ServiceContexts scs) 
    {
        if (showProprietaryHooks) {
            U.sop(baseMsg + ".receivedRequestServiceContexts - " + scs);
        }
    }

    protected void sendingReplyServiceContexts(ServiceContexts scs)
    {
        if (showProprietaryHooks) {
            U.sop(baseMsg + ".sendingReplyServiceContexts - " + scs);
        }
    }
}
 
// End of file.
