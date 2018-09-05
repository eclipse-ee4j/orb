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
 * Invocation strategy in which two requests are sent, one that is not oneway
 * and one that is oneway, in that order.
 */
public class InvokeOneWay
    extends InvokeStrategy
{
    public void invoke() throws Exception {
        super.invoke();

        // Invoke normal call
        invokeMethod( "sayHello" );

        // Invoke oneway call
        invokeMethod( "sayOneway" );
    }
}
