/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package corba.codeset;

import test.Test;
import corba.framework.*;
import java.util.*;
import com.sun.corba.ee.spi.misc.ORBConstants;
import org.omg.CORBA.*;

/**
 * Simple tests in GIOP 1.1 and 1.2 of chars and wstrings.
 */
public class CodeSetTest extends CORBATest
{
    protected void doTest() throws Throwable
    {
        // Now GIOP 1.1.
        Controller orbd = createORBD();

        // Specify the GIOP version property
        Properties clientProps = Options.getClientProperties();
        Properties serverProps = Options.getServerProperties();

        clientProps.setProperty(ORBConstants.GIOP_VERSION, "1.1");
        serverProps.setProperty(ORBConstants.GIOP_VERSION, "1.2");
        clientProps.setProperty(ORBConstants.GIOP_FRAGMENT_SIZE, "32");
        serverProps.setProperty(ORBConstants.GIOP_FRAGMENT_SIZE, "64");

        Controller server = createServer("corba.codeset.Server",
                                         "server");
        Controller client = createClient("corba.codeset.Client",
                                         "client1_1");

        orbd.start();
        server.start();
        client.start();

        // Wait for the client to finish for up to 2 minutes, then
        // throw an exception.
        client.waitFor(120000);
        client.stop();

        // Now try GIOP 1.2
        clientProps.setProperty(ORBConstants.GIOP_FRAGMENT_SIZE, "256");
        clientProps.setProperty(ORBConstants.GIOP_VERSION, "1.2");
        client = createClient("corba.codeset.Client",
                              "client1_2");

        client.start();
        client.waitFor(120000);
        client.stop();

        server.stop();
        orbd.stop();
    }
}
    
