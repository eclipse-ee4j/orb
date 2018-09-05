/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package pi.serverrequestinfo;

import com.sun.corba.ee.impl.interceptors.*;
import org.omg.PortableInterceptor.*;
import org.omg.CORBA.*;

/**
 * Invocation strategy in which each interception point is visited.
 * Interceptors are also called for the forwarded object.
 * The following order is used:
 *    rrsc, rr, sr
 *    rrsc, rr, se
 *    rrsc, rr, so
 *    rrsc, rr, sr
 */
public class InvokeVisitAllForward
    extends InvokeStrategy
{
    public InvokeVisitAllForward() {
    }

    public void invoke() throws Exception {
        super.invoke();

        SampleServerRequestInterceptor.invokeOnForwardedObject = true;

        // Invoke send_request then receive_reply
        invokeMethod( "sayHello" );

        // Invoke send_request then receive_exception:
        try {
            invokeMethod( "saySystemException" );
        }
        catch( IMP_LIMIT e ) {
            // We expect this, but no other exception.
        }

        // Invoke send_request then receive_other:
        SampleServerRequestInterceptor.exceptionRedirectToOther = true;
        try {
            invokeMethod( "saySystemException" );
        }
        catch( IMP_LIMIT e ) {
            // We expect this, but no other exception.
        }
    }
}
