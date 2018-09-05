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
// Created       : 2003 Apr 09 (Wed) 16:31:43 by Harold Carr.
// Last Modified : 2003 Jul 26 (Sat) 18:28:47 by Harold Carr.
//

package corba.exceptiondetailsc;

import corba.framework.Controller;
import corba.framework.CORBATest;

public class ExceptionDetailSCTest
    extends
        CORBATest
{
    public static final String thisPackage =
        ExceptionDetailSCTest.class.getPackage().getName();

    protected void doTest() throws Throwable {
        Controller orbd   = createORBD();
        orbd.start();

        doTestType("Server", "Server",
                   "Client", "Client");

        Controller colocatedClientServer = 
            createClient(thisPackage + ".ColocatedClientServer",
                         "colocatedClientServer");
        colocatedClientServer.start();
        colocatedClientServer.waitFor();
        colocatedClientServer.stop();

        orbd.stop();
    }

    protected void doTestType(
        String serverMainClass, String serverTestName, 
        String clientMainClass, String clientTestName) throws Throwable {

        Controller server = createServer(thisPackage + "." + serverMainClass,
                                         serverTestName);
        server.start();

        Controller client = createClient(thisPackage + "." + clientMainClass,
                                         clientTestName);
        client.start();
        client.waitFor();
        client.stop();

        server.stop();
    }
}

// End of file.

