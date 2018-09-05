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
// Created       : 2002 Jul 19 (Fri) 14:49:22 by Harold Carr.
// Last Modified : 2002 Jul 22 (Mon) 11:09:39 by Harold Carr.
//

package corba.iorintsockfact;

import corba.framework.Controller;
import corba.framework.CORBATest;

/**
 * @author Harold Carr
 */
public class IorIntSockFactTest extends CORBATest {
    public static final String thisPackage =
        IorIntSockFactTest.class.getPackage().getName();

    protected void doTest() throws Throwable {
        Controller orbd   = createORBD();
        orbd.start();

        Controller server = createServer(thisPackage + "." + "Server",
                                         "Server");
        Controller client = createClient(thisPackage + "." + "Client",
                                         "Client");
        server.start();
        client.start();
        client.waitFor();
        client.stop();
        server.stop();
        orbd.stop();
    }
}

// End of file.

