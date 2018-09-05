/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package corba.unarrow;

import test.Test;
import corba.framework.*;
import java.util.*;

/**
 * This is a POA version of the mthello test.  The
 * client creates multiple threads that invoke a simple sayHello
 * method on the remote servant.
 */
public class UncheckedNarrowTest extends CORBATest
{
    protected void doTest() throws Throwable
    {
        Controller orbd = createORBD();
        Controller server = createServer("corba.unarrow.Server");
        Controller client = createClient("corba.unarrow.Client");

        orbd.start();
        server.start();
        client.start();

        client.waitFor(120000);

        client.stop();
        server.stop();
        orbd.stop();
    }
}

