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
// Created       : 2003 Dec 11 (Thu) 10:59:34 by Harold Carr.
// Last Modified : 2003 Dec 17 (Wed) 21:06:49 by Harold Carr.
//

package corba.legacybootstrapserver;

import corba.framework.Controller;
import corba.framework.CORBATest;
import corba.framework.Options;

public class LegacyBootstrapServerTest extends CORBATest {

    protected void doTest() throws Throwable {
        //        Options.setOutputDirectory((String)getArgs().get(test.Test.OUTPUT_DIRECTORY));

        // The ORBD is NOT used.
        // However, when it wasn't here then createServer below complained
        // Caught java.io.FileNotFoundException: gen/corba/legacybootstrapserver/server.out.txt (No such file or directory)
        
        Controller orbd   = createORBD();

        Controller server = createServer(Server.class.getName());
        Controller client = createClient(Client.class.getName());

        orbd.start() ;
        server.start();
        client.start();
        client.waitFor(60000);
        client.stop();
        server.stop();
        orbd.stop();
    }
}

// End of file.
