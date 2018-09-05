/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package pi.clientrequestinfo;

import com.sun.corba.ee.impl.interceptors.*;
import org.omg.PortableInterceptor.*;
import org.omg.CORBA.*;

/**
 * Invocation strategy in which a recursive call is made, causing
 * send_request and receive_reply to be invoked twice, as follows:
 *    send_request
 *        send_request
 *        receive_reply
 *    receive_reply
 */
public class InvokeRecursive
    extends InvokeStrategy
{
    public void invoke() throws Exception {
        super.invoke();

        // Invoke send_request, cause send_request to make a request causing
        // send_request then receive_reply, and then finally receive_reply.
        SampleClientRequestInterceptor.recursiveInvoke = true;
        invokeMethod( "sayHello" );
    }
}
