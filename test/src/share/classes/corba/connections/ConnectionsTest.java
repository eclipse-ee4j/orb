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
// Created       : 2002 Mar 24 (Sun) 09:04:46 by Harold Carr.
// Last Modified : 2003 Sep 27 (Sat) 19:48:45 by Harold Carr.
//

package corba.connections;

import corba.framework.Controller;
import corba.framework.CORBATest;

public class ConnectionsTest
    extends
        CORBATest
{
    public static final String thisPackage =
        ConnectionsTest.class.getPackage().getName();

    protected void doTest()
        throws
            Throwable
    {
        Controller orbd   = createORBD();
        orbd.start();

        Controller server1 = createServer(thisPackage + "." + "Server1",
                                         "Server1");
        Controller server2 = createServer(thisPackage + "." + "Server2",
                                         "Server2");
        Controller client1 = createClient(thisPackage + "." + "Client1",
                                         "Client1");
        Controller client2 = createClient(thisPackage + "." + "Client2",
                                         "Client2");

        server1.start();
        server2.start();

        client1.start();
        client2.start();

        client1.waitFor(300000);
        client2.waitFor(300000);

        client1.stop();
        client2.stop();
        server1.stop();
        server2.stop();
        orbd.stop();
    }
}

// End of file.

