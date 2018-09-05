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
import ServerRequestInfo.*;

/**
 * Invocation strategy in which four calls are made.  
 * 1. No exception raised
 * 2. SystemException raised
 * 3. UserException raised
 * 4. No exception raised, receive_other is called.
 */
public class InvokeExceptions
    extends InvokeStrategy
{
    public void invoke() throws Exception {
        super.invoke();

        // Invoke rrsc, rr, then sr
        invokeMethod( "sayHello" );

        // Invoke rrsc, rr, then se
        try {
            invokeMethod( "saySystemException" );
        }
        catch( IMP_LIMIT e ) {
            // We expect this, but no other exception.
        }

        // Invoke rrsc, rr, then se (user exception)
        try {
            invokeMethod( "sayUserException" );
        }
        catch( ExampleException e ) {
            // We expect this, but no other exception.
        }

        // Invoke rrsc, rr, then so
        SampleServerRequestInterceptor.exceptionRedirectToOther = true;
        SampleServerRequestInterceptor.invokeOnForwardedObject = false;
        try {
            invokeMethod( "saySystemException" );
        }
        catch( IMP_LIMIT e ) {
            // We expect this, but no other exception.
        }
    }
}
