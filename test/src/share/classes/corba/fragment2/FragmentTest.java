/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package corba.fragment2;

import test.Test;
import corba.framework.*;
import java.util.*;
import com.sun.corba.ee.spi.misc.ORBConstants;

public class FragmentTest extends CORBATest
{
    protected void doTest() throws Throwable
    {
        Properties clientProps = Options.getClientProperties();

        int fragmentSize = 1024;

        // clientProps.add(ORBConstants.GIOP_VERSION, "1.2");
        clientProps.put(ORBConstants.GIOP_FRAGMENT_SIZE, "" + fragmentSize);
        clientProps.put("array.length", "" + fragmentSize);
        clientProps.put(ORBConstants.GIOP_VERSION, "1.2");

        Properties serverProps = Options.getServerProperties();
        serverProps.put(ORBConstants.GIOP_VERSION, "1.2");

        //Controller orbd = createORBD();
        Controller server = createServer("corba.fragment.Server");
        Controller client = createClient("corba.fragment.Client");

        //        orbd.start();
        server.start();
        client.start();

        client.waitFor(2000000);

        client.stop();
        server.stop();
        //orbd.stop();
    }
}

