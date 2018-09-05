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

import ClientRequestInfo.*;

/**
 * Invocation strategy in which each interception point is visited, similar
 * to InvokeVisitAll, but sayArguments is called instead of sayHello.
 * 
 * The following order is used:
 *    send_request, receive_reply
 *    send_request, receive_exception
 *    send_request, receive_other
 */
public class InvokeDynamic
    extends InvokeStrategy
{
    public void invoke() throws Exception {
        super.invoke();

        // Invoke send_request then receive_reply
        invokeMethod( "sayArguments" );

        // Invoke send_request then receive_exception:
        try {
            invokeMethod( "sayUserException" );
        }
        catch( ExampleException e ) {
            // We expect this, but no other exception.
        }
        catch( UnknownUserException e ) {
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
