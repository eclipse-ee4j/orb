/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package corba.rmipoacounter;

import test.Test;
import corba.framework.*;
import java.util.*;
import com.sun.corba.ee.spi.misc.ORBConstants;

public class RMIPOACounterTest extends CORBATest
{
    protected Controller newClientController()
    {
        return new InternalExec();
    }

    protected void doTest() throws Throwable
    {
        // try this one. the report dir was already set to gen/corba/rmipoacounter
        Options.setOutputDirectory((String)getArgs().get(test.Test.OUTPUT_DIRECTORY));
        Options.addServerArg("-debug");

        Controller orbd = createORBD();

        Properties serverProps = Options.getServerProperties();

        serverProps.setProperty(ORBConstants.PERSISTENT_SERVER_PORT_PROPERTY,
                                Options.getUnusedPort().toString());

        Controller server = createServer("corba.rmipoacounter.counterServer");

        orbd.start();
        server.start();

        // In this test, the client will kill and restart the server, so it
        // needs the reference.  Thus, the client can only be an InternalProcess
        // or a ThreadProcess.
        Hashtable clientExtra = Options.getClientExtra();
        clientExtra.put("server", server);

        /*
          This is basically a test of persistent servers.  The server 
          maintains a counter in a file, and can be restarted without
          losing it.  Plus, the reference in ORBD stays the same.
        */

        Controller client = createClient("corba.rmipoacounter.counterClient");

        client.start();

        client.waitFor();

        client.stop();

        server.stop();

        orbd.stop();
    }
}

