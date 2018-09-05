/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package corba.stubserialization;

import test.Test;
import corba.framework.*;
import java.util.Properties;

public class StubSerializationTest extends CORBATest {
    protected void doTest() throws Throwable
    {
        Options.addServerArg("-debug");
        Controller orbd = createORBD();
 
        Properties serverProps = Options.getServerProperties();
 
        Controller server = createServer(
            "corba.stubserialization.Server");
 
        orbd.start();
 
        server.start();
 
        Controller client = createClient(
            "corba.stubserialization.Client");
 
        client.start();
 
        client.waitFor(120000);
 
        client.stop();
 
        server.stop();

        orbd.stop();
    }
}
