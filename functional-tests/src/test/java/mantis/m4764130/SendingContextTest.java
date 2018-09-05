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
// Created       : 2002 Oct 16 (Wed) 08:35:25 by Harold Carr.
// Last Modified : 2002 Oct 17 (Thu) 09:31:31 by Harold Carr.
//

package mantis.m4764130;

import java.util.Properties;
import corba.framework.Controller;
import corba.framework.CORBATest;
import corba.framework.Options;
import com.sun.corba.ee.spi.misc.ORBConstants;

public class SendingContextTest extends CORBATest {

    protected void doTest() throws Throwable {

        if (test.Test.useJavaSerialization()) {
            return;
        }

        Controller orbd   = createORBD();
        orbd.start();

        Controller server = createServer("mantis.m4764130.Server");
        server.start();

        Controller client = createClient("mantis.m4764130.Client");
        client.start();

        client.waitFor();

        client.stop();
        server.stop();
        orbd.stop();
    }
}

// End of file.
