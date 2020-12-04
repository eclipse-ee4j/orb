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
// Created       : 2000 Oct 20 (Fri) 11:22:06 by Harold Carr.
// Last Modified : 2002 Mar 22 (Fri) 09:31:59 by Harold Carr.
//

package corba.connectintercept_1_4;

import org.omg.CORBA.INTERNAL;
import org.omg.PortableInterceptor.ClientRequestInfo;

public class CRIOrdered
    extends
        org.omg.CORBA.LocalObject
    implements
        org.omg.PortableInterceptor.ClientRequestInterceptor,
        Comparable
{
    public static final String baseMsg = CRIOrdered.class.getName();
    public String name;
    public int order;
    public CRIOrdered(String name, int order)
    {
        this.name = name;
        this.order = order;
    }
    public int compareTo(Object o)
    {
        int otherOrder = ((CRIOrdered)o).order;
        if (order < otherOrder) {
            return -1;
        } else if (order == otherOrder) {
            return 0;
        }
        return 1;
    }
    public String name() { return name; }

    public void destroy() 
    {
        try {
            Common.up(order);
        } catch (INTERNAL e) {
            // INTERNAL will get swallowed by ORB.
            // Convert it to something else so server will exit incorrectly
            // so error can be detected.
            throw new RuntimeException(baseMsg + ": Wrong order in destroy.");
        }
    }

    public void send_request(ClientRequestInfo cri)
    {
        Common.up(order);
    }
    public void send_poll(ClientRequestInfo cri)
    {
        Common.up(order);
    }
    public void receive_reply(ClientRequestInfo cri)
    {
        Common.down(order);
    }
    public void receive_exception(ClientRequestInfo cri)
    {
        Common.down(order);
    }
    public void receive_other(ClientRequestInfo cri)
    {
        Common.down(order);
    }
}

// End of file.
