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
