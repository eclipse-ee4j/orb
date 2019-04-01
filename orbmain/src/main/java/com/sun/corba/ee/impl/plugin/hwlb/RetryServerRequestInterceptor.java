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
// Created       : 2005 Jul 01 (Fri) 13:36:46 by Harold Carr.
//

package com.sun.corba.ee.impl.plugin.hwlb;

import java.util.Properties;

import org.omg.CORBA.ORB;
import org.omg.CORBA.TRANSIENT;
import org.omg.PortableInterceptor.ORBInitializer;
import org.omg.PortableInterceptor.ORBInitInfo;
import org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName;
import org.omg.PortableInterceptor.ServerRequestInterceptor;
import org.omg.PortableInterceptor.ServerRequestInfo;

/**
 * @author Harold Carr
 */
public class RetryServerRequestInterceptor extends org.omg.CORBA.LocalObject implements ORBInitializer, ServerRequestInterceptor {
    private static final String baseMsg = RetryServerRequestInterceptor.class.getName();

    private static boolean rejectingRequests = false;

    private static boolean debug = true;

    ////////////////////////////////////////////////////
    //
    // Application specific
    //

    public static boolean getRejectingRequests() {
        return rejectingRequests;
    }

    public static void setRejectingRequests(boolean x) {
        rejectingRequests = x;
    }

    ////////////////////////////////////////////////////
    //
    // Interceptor operations
    //

    public String name() {
        return baseMsg;
    }

    public void destroy() {
    }

    ////////////////////////////////////////////////////
    //
    // ServerRequestInterceptor
    //

    public void receive_request_service_contexts(ServerRequestInfo ri) {
        if (rejectingRequests) {
            if (debug) {
                System.out.println(baseMsg + ".receive_request_service_contexts:" + " rejecting request: " + ri.operation());
            }
            throw new TRANSIENT();
        }
        if (debug) {
            System.out.println(baseMsg + ".receive_request_service_contexts:" + " accepting request: " + ri.operation());
        }
    }

    public void receive_request(ServerRequestInfo ri) {
    }

    public void send_reply(ServerRequestInfo ri) {
    }

    public void send_exception(ServerRequestInfo ri) {
    }

    public void send_other(ServerRequestInfo ri) {
    }

    ////////////////////////////////////////////////////
    //
    // ORBInitializer
    //

    public void pre_init(ORBInitInfo info) {
    }

    public void post_init(ORBInitInfo info) {
        try {
            if (debug) {
                System.out.println(".post_init: registering: " + this);
            }
            info.add_server_request_interceptor(this);
        } catch (DuplicateName e) {
            // REVISIT - LOG AND EXIT
            if (debug) {
                System.out.println(".post_init: exception: " + e);
            }
        }
    }
}

// End of file.
