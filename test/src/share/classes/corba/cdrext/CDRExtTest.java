/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package corba.cdrext;

import test.Test;
import corba.framework.*;
import java.util.*;
import com.sun.corba.ee.spi.misc.ORBConstants;

public class CDRExtTest extends CORBATest {

    protected void doTest() throws Throwable  
    {
        // 2048 since the client assumes 2048 bytes will be enough
        // ahead of any structure to push it totally across a
        // fragment boundary
        Properties clientProps = Options.getClientProperties();
        clientProps.put(ORBConstants.GIOP_FRAGMENT_SIZE, "2048");

        Controller orbd = createORBD();
        Controller server = createServer("corba.cdrext.Server");
        Controller client = createClient("corba.cdrext.Client");

        orbd.start();
        server.start();
        client.start();

        client.waitFor(360000);

        client.stop();
        server.stop();
        orbd.stop();
    }
}

