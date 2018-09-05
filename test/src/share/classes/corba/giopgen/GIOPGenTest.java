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
// Created       : 2005 Oct 05 (Wed) 17:33:44 by Harold Carr.
// Last Modified : 2005 Oct 05 (Wed) 20:01:57 by Harold Carr.
//

package corba.giopgen;

import java.util.Properties;

import corba.framework.CORBATest;
import corba.framework.Controller;
import corba.framework.Options;

import com.sun.corba.ee.spi.misc.ORBConstants;

/**
 * @author Harold Carr
 */
public class GIOPGenTest extends CORBATest {
    protected void doTest() throws Exception {
        String thisPackage = GIOPGenTest.class.getPackage().getName();

        Controller orbd = createORBD();
        Controller server;
        Controller client;

        orbd.start();

        server = createServer(thisPackage+"."+"Server", "Server");
        client = createClient(thisPackage+"."+"Client", "Client");

        server.start();
        client.start();

        client.waitFor(1000 * 60 * 5);

        client.stop();
        server.stop();
        orbd.stop();
    }
}

// End of file.
