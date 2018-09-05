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

/**
 * Base class for all invocation strategies used in this test.  This allows
 * for dynamic behavior modifications between test cases of which objects
 * are invoked.  Default method implementations do nothing.
 */
abstract public class InvokeStrategy {
    /**
     * Invokes the method with the given name
     */
    protected void invokeMethod( String name ) throws Exception {
        ServerCommon.server.invokeMethod( name );
    }

    public void invoke() throws Exception {
        // Reset the request interceptor to prepare for test:
        SampleServerRequestInterceptor.enabled = true;
        SampleServerRequestInterceptor.receiveRequestServiceContextsEnabled = 
            true;
        SampleServerRequestInterceptor.receiveRequestEnabled = true;
        SampleServerRequestInterceptor.sendReplyEnabled = true;
        SampleServerRequestInterceptor.sendExceptionEnabled = true;
        SampleServerRequestInterceptor.sendOtherEnabled = true;

        // Reset flags
        SampleServerRequestInterceptor.exceptionRedirectToOther = false;
        SampleServerRequestInterceptor.invokeOnForwardedObject = false;
    }
}
