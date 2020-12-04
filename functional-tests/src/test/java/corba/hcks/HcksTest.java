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

