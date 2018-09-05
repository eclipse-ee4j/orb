/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

//
// Created       : 2002 Mar 24 (Sun) 09:04:46 by Harold Carr.
// Last Modified : 2002 Jul 24 (Wed) 11:08:55 by Harold Carr.
//

package corba.connectintercept_1_4;

import corba.framework.Controller;
import corba.framework.CORBATest;

public class ConnectIntercept_1_4Test
    extends
        CORBATest
{
    public static final String thisPackage =
        ConnectIntercept_1_4Test.class.getPackage().getName();

    protected void doTest()
        throws
            Throwable
    {
        Controller orbd   = createORBD();
        orbd.start();

        Controller server = createServer(thisPackage + "." + "ServerTransient",
                                         "ServerTransient");
        Controller client = createClient(thisPackage + "." + "Client",
                                         "ClientTransient");
        server.start();
        client.start();
        client.waitFor(120000);
        client.stop();
        server.stop();
        orbd.stop();

        // REVISIT:
        // Need to do integrate persistent test into test framework.
    }
}

// End of file.

