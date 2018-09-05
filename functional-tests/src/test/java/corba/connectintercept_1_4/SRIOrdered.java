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
// Created       : 2000 Oct 31 (Tue) 09:58:47 by Harold Carr.
// Last Modified : 2002 Mar 22 (Fri) 09:33:55 by Harold Carr.
//

package corba.connectintercept_1_4;

import org.omg.CORBA.INTERNAL;
import org.omg.PortableInterceptor.ServerRequestInfo;

public class SRIOrdered
    extends
        org.omg.CORBA.LocalObject
    implements
        org.omg.PortableInterceptor.ServerRequestInterceptor,
        Comparable
{
    public static final String baseMsg = SRIOrdered.class.getName();
    public String name;
    public int order;
    public SRIOrdered(String name, int order)
    {
        this.name = name;
        this.order = order;
    }
    public int compareTo(Object o)
    {
        int otherOrder = ((SRIOrdered)o).order;
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

    public void receive_request_service_contexts(ServerRequestInfo sri)
    {
        Common.up(order);
    }

    public void receive_request(ServerRequestInfo sri)
    {
        // Note: Do NOT put Common.up here because all 3 ordered
        // interceptors run in RRSC so when we get here current will
        // be 3 but the first ordered interceptor will have value 1
        // and fail.
        // Bottom line: only count up in one point.
    }

    public void send_reply(ServerRequestInfo sri)
    {
        Common.down(order);
    }

    public void send_exception(ServerRequestInfo sri)
    {
        Common.down(order);
    }

    public void send_other(ServerRequestInfo sri)
    {
        Common.down(order);
    }
}

// End of file.







