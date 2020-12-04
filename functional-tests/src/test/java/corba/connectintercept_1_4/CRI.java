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
// Created       : 2000 Oct 16 (Mon) 14:55:05 by Harold Carr.
// Last Modified : 2003 Feb 11 (Tue) 14:09:38 by Harold Carr.
//


package corba.connectintercept_1_4;

import com.sun.corba.ee.spi.legacy.interceptor.RequestInfoExt;
import org.omg.CORBA.BAD_PARAM;
import org.omg.CORBA.INTERNAL;
import org.omg.IOP.TaggedComponent;
import org.omg.PortableInterceptor.ClientRequestInfo;
import org.omg.PortableInterceptor.ClientRequestInterceptor;

public class CRI
    extends
        org.omg.CORBA.LocalObject
    implements
        ClientRequestInterceptor
{
    public static final String baseMsg = CRI.class.getName();

    public int balance = 0;

    public String name() { return baseMsg; }

    public void destroy() 
    {
        if (balance != 0) {
            throw new RuntimeException(baseMsg + ": Interceptors not balanced.");
        }
    }

    public void send_request(ClientRequestInfo cri)
    {
        balance++;
        System.out.println(baseMsg + ".send_request " + cri.operation());
        System.out.println("    request on connection: " +
                           ((RequestInfoExt)cri).connection());

        try {
            TaggedComponent taggedComponent =
                cri.get_effective_component(Common.ListenPortsComponentID);
            String componentData = 
                new String(taggedComponent.component_data);
            System.out.println("    found ListenPortsComponentID: " +
                               componentData);
        } catch (BAD_PARAM e) {
            // This is ignored because we talk to naming which
            // will not contain the listen component.
        }
    }

    public void send_poll(ClientRequestInfo cri)
    {
        balance++;
        System.out.println(baseMsg + ".send_poll " + cri.operation());
    }

    public void receive_reply(ClientRequestInfo cri)
    {
        balance--;
        System.out.println(baseMsg + ".receive_reply " + cri.operation());
    }

    public void receive_exception(ClientRequestInfo cri)
    {
        balance--;
        System.out.println(baseMsg + ".receive_exception " + cri.operation());
    }

    public void receive_other(ClientRequestInfo cri)
    {
        balance--;
        System.out.println(baseMsg + ".receive_other " + cri.operation());
    }
}

// End of file.

