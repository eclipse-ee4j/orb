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


