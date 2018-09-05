/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package corba.preinvokepostinvoke;

import corba.framework.Controller;
import corba.framework.CORBATest;
import corba.framework.Options;

public class PreInvokePostInvokeTest
    extends
        CORBATest
{
    protected void doTest() throws Throwable
    {
        Controller orbd = createORBD();
        Controller server = createServer("corba.preinvokepostinvoke.Server" );

        orbd.start();

        server.start();

        server.stop();

        orbd.stop( );
    }
}
