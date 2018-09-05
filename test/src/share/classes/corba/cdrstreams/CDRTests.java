/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package corba.cdrstreams;

import test.Test;
import corba.framework.*;
import java.util.*;
import com.sun.corba.ee.spi.orb.ORB;

public class CDRTests extends CORBATest
{
    protected void doTest() throws Throwable {
        
        if (test.Test.useJavaSerialization()) {
            return;
        }

        Controller orbd = createORBD();
        Controller server = createServer("corba.cdrstreams.Server");
        Controller client = createClient("corba.cdrstreams.Client");

        orbd.start();
        server.start();
        client.start();

        client.waitFor(180000);

        client.stop();
        server.stop();
        orbd.stop();
    }
}

