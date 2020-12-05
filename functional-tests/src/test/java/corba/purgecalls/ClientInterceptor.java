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
// Created       : 2002 Jan 17 (Thu) 14:50:11 by Harold Carr.
// Last Modified : 2003 Feb 11 (Tue) 14:12:55 by Harold Carr.
//

package corba.purgecalls;

import com.sun.corba.ee.spi.legacy.interceptor.RequestInfoExt;
import corba.hcks.U;
import org.omg.CORBA.BAD_PARAM;
import org.omg.CORBA.SystemException;
import org.omg.PortableInterceptor.ClientRequestInterceptor;
import org.omg.PortableInterceptor.ClientRequestInfo;

public class ClientInterceptor
    extends
        org.omg.CORBA.LocalObject
    implements
        ClientRequestInterceptor
{
    public static final String baseMsg = ClientInterceptor.class.getName();

    //
    // Interceptor operations
    //

    public String name() 
    {
        return baseMsg; 
    }

    public void destroy() 
    {
    }

    //
    // ClientRequestInterceptor operations
    //

    public void send_request(ClientRequestInfo ri)
    {
        sopCR(baseMsg, "send_request", ri);
        Client.requestConnection = ((RequestInfoExt)ri).connection();
    }

    public void send_poll(ClientRequestInfo ri)
    {
        sopCR(baseMsg, "send_poll", ri);
    }

    public void receive_reply(ClientRequestInfo ri)
    {
        sopCR(baseMsg, "receive_reply", ri);
        checkServiceContexts(ri);
    }

    public void receive_exception(ClientRequestInfo ri)
    {
        sopCR(baseMsg, "receive_exception", ri);
        checkServiceContexts(ri);
    }

    public void receive_other(ClientRequestInfo ri)
    {
        sopCR(baseMsg, "receive_other", ri);
        checkServiceContexts(ri);
    }

    //
    // Utilities.
    //

    public static void sopCR(String clazz, String point, ClientRequestInfo ri)
    {
        try {
            U.sop(clazz + "." + point + " " + ri.operation());
        } catch (SystemException e) {
            U.sopUnexpectedException(baseMsg + "." + point, e);
        }
    }

    public static void checkServiceContexts(ClientRequestInfo ri)
    {
        // The number does not matter.
        // Just want to ensure no null pointer exception after purge calls.
        try {
            ri.get_reply_service_context(100);
        } catch (BAD_PARAM e) {
            U.sop(baseMsg + " Expected: " + e);
        }
    }
}

// End of file.

