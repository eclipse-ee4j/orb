/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package corba.poamanager;

import test.Test;
import corba.framework.*;
import java.util.*;

public class POAManagerTest extends CORBATest
{
    protected void doTest() throws Throwable
    {
        Controller orbd = createORBD();
        Controller server = createServer("corba.poamanager.HelloServer");
        Controller client = createClient("corba.poamanager.HelloClient");

        orbd.start();
        server.start();
        client.start();

        client.waitFor(1000 * 60 * 5);

        client.stop();
        server.stop();
        orbd.stop();
    }
}

