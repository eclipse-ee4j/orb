/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package corba.serialization.proxy;

import test.Test;
import corba.framework.*;
import java.util.Properties;

public class ProxySerializationTest extends CORBATest {
    protected void doTest() throws Throwable
    {
        //DEBUG: release commnet here
        //test.Test.debug=true;
        Options.addServerArg("-debug");
        Controller orbd = createORBD();
 
        Properties serverProps = Options.getServerProperties();
        orbd.start();
 
        Controller server = createServer("corba.serialization.proxy.Server");
        Controller client = createClient("corba.serialization.proxy.Client");

        server.start();
        client.start();
 
        client.waitFor(120000);

        client.stop();
        server.stop();

        orbd.stop();
    }
}
