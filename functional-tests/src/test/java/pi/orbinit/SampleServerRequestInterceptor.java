/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package pi.orbinit;

import org.omg.CORBA.LocalObject;

import org.omg.PortableInterceptor.ForwardRequest;
import org.omg.PortableInterceptor.ServerRequestInfo;
import org.omg.PortableInterceptor.ServerRequestInterceptor;

import ORBInitTest.*;

/**
 * Sample ServerRequestInterceptor for use in testing
 */
public class SampleServerRequestInterceptor 
    extends org.omg.CORBA.LocalObject
    implements ServerRequestInterceptor
{

    private String name;

    static int destroyCount = 0;

    public SampleServerRequestInterceptor( String name ) {
        this.name = name;
    }

    public String name() {
        return name;
    }

    public void destroy() {
        destroyCount++;
    }

    public void receive_request_service_contexts (ServerRequestInfo ri) 
        throws ForwardRequest
    {
    }

    public void receive_request (ServerRequestInfo ri) 
        throws ForwardRequest 
    {
    }

    public void send_reply (ServerRequestInfo ri) {
    }

    public void send_exception (ServerRequestInfo ri) 
        throws ForwardRequest 
    {
    }

    public void send_other (ServerRequestInfo ri) 
        throws ForwardRequest 
    {
    }

}


