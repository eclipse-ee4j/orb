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
 * The following order is used:
 *    rrsc, rr, sr
 *    rrsc, rr, se
 *    rrsc, rr, so
 */
public class InvokeVisitAll 
    extends InvokeStrategy
{
    // String to prepend to any outgoing call for this invocation strategy.
    private String methodPrefix = "";

    public InvokeVisitAll( String methodPrefix ) {
        this.methodPrefix = methodPrefix;
    }

    public InvokeVisitAll() {
        this( "" );
    }

    public void invoke() throws Exception {
        super.invoke();

        // Invoke rrsc, rr, sr
        invokeMethod( methodPrefix + "sayHello" );

        // Invoke rrsc, rr, se
        try {
            invokeMethod( methodPrefix + "saySystemException" );
        }
        catch( IMP_LIMIT e ) {
            // We expect this, but no other exception.
        }

        // Invoke rrsc, rr, so
        try {
            SampleServerRequestInterceptor.exceptionRedirectToOther = true;
            SampleServerRequestInterceptor.invokeOnForwardedObject = false;
            invokeMethod( methodPrefix + "saySystemException" );
        }
        catch( IMP_LIMIT e ) {
            // We expect this, but no other exception.
        }
    }
}
