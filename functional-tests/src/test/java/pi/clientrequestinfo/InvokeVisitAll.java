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
 * Invocation strategy in which each interception point is visited.
 * The following order is used:
 *    send_request, receive_reply
 *    send_request, receive_exception
 *    send_request, receive_other
 */
public class InvokeVisitAll 
    extends InvokeStrategy
{
    public void invoke() throws Exception {
        super.invoke();

        // Invoke send_request then receive_reply
        invokeMethod( "sayHello" );

        // Invoke send_request then receive_exception:
        try {
            invokeMethod( "saySystemException" );
        }
        catch( UNKNOWN e ) {
            // We expect this, but no other exception.
        }

        // Invoke send_request then receive_other:
        SampleClientRequestInterceptor.exceptionRedirectToOther = true;
        try {
            invokeMethod( "saySystemException" );
        }
        catch( UNKNOWN e ) {
            // We expect this, but no other exception.
        }
    }
}
