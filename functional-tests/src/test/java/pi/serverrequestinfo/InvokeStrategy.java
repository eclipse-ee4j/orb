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
