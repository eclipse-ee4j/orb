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
import org.omg.PortableInterceptor.ClientRequestInfo;
import org.omg.PortableInterceptor.ClientRequestInterceptor;

import ORBInitTest.*;

/**
 * Sample ClientRequestInterceptor for use in testing
 */
public class SampleClientRequestInterceptor 
    extends org.omg.CORBA.LocalObject 
    implements ClientRequestInterceptor
{

    private String name;

    // Number of times destroy was called for interceptors of this type.
    static int destroyCount = 0;

    public SampleClientRequestInterceptor( String name ) {
        this.name = name;
    }

    public String name() {
        return name;
    }

    public void destroy() {
        destroyCount++;
    }

    public void send_request (ClientRequestInfo ri) 
        throws ForwardRequest 
    {
    }

    public void send_poll (ClientRequestInfo ri) {
    }

    public void receive_reply (ClientRequestInfo ri) {
    }

    public void receive_exception (ClientRequestInfo ri) 
        throws ForwardRequest
    {
    }

    public void receive_other (ClientRequestInfo ri) 
        throws ForwardRequest 
    {
    }

}


