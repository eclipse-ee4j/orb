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
// Created       : 2002 Jan 17 (Thu) 19:10:44 by Harold Carr.
// Last Modified : 2003 Mar 12 (Wed) 09:56:16 by Harold Carr.
//

package corba.purgecalls;

import corba.framework.Controller;
import corba.framework.CORBATest;

public class PurgecallsTest
    extends
        CORBATest
{
    public static final String thisPackage =
        PurgecallsTest.class.getPackage().getName();

    protected void doTest()
        throws
            Throwable
    {
        Controller orbd   = createORBD();
        orbd.start();

        Controller server =
            createServer(thisPackage + "." + "Server", "Server");
        Controller client = 
            createClient(thisPackage + "." + "Client", "Client");

        server.start();
        client.start();
        // When this test fails - it hangs, so do not wait forever.
        client.waitFor(60000);
        client.stop();
        server.stop();
        orbd.stop();
    }

}

// End of file.


