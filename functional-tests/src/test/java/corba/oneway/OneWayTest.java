/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package corba.oneway;

import corba.framework.*;
import java.util.*;

public class OneWayTest extends CORBATest
{
    protected void doTest() throws Throwable
    {
        Controller orbd = createORBD();

        orbd.start();

        Controller server = createServer("corba.oneway.helloServer");

        server.start();

        Controller client = createClient("corba.oneway.helloClient");
    
        client.start();

        client.waitFor();

        client.stop();

        server.stop();

        orbd.stop();
    }
}

