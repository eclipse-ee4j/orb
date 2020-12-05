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
