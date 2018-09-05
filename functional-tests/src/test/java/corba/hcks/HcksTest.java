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
// Created       : 2000 Nov 26 (Sun) 11:57:37 by Harold Carr.
// Last Modified : 2002 May 15 (Wed) 12:55:27 by Harold Carr.
//

package corba.hcks;

import corba.framework.Controller;
import corba.framework.CORBATest;

public class HcksTest
    extends
        CORBATest
{
    public static final String thisPackage =
        HcksTest.class.getPackage().getName();

    protected void doTest()
        throws
            Throwable
    {
        Controller orbd   = createORBD();
        orbd.start();

        doTestType("Server", "remoteServerStream",
                   "Client", "remoteClientStream");
        doTestType("ServerGrow", "remoteServerGrow",
                   "ClientGrow", "remoteClientGrow");
        doTestType("Server_1_1", "remoteServer_1_1",
                   "Client_1_1", "remoteClient_1_1");

        Controller colocatedClientServer = 
            createClient(thisPackage + ".ColocatedClientServer",
                         "colocatedClientServer");
        colocatedClientServer.start();
        colocatedClientServer.waitFor();
        colocatedClientServer.stop();

        orbd.stop();
    }

    protected void doTestType(String serverMainClass, String serverTestName,
                              String clientMainClass, String clientTestName)
        throws
            Throwable
    {
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

