/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package naming.pcosnaming;

import test.Test;
import corba.framework.*;
import java.util.*;

public class PCOSNamingTest extends CORBATest
{
    public static String[] idlFiles = { "hello.idl" };

    public static String[] javaFiles = { "helloClient.java",
                                         "helloServer.java" };

    protected Controller newServerController()
    {
        return new InternalExec();
    }

    // Unusual test in which killing ORBD is necessary, and this must be
    // controlled by the server (to test persistent references).
    //
    // To do this, the server is executed in the main test thread, not in a
    // separate process.  It is passed the Controller objects for ORBD and
    // the client, so it can start them and stop them at the appropriate
    // times.
    protected void doTest() throws Throwable
    {
        Options.addIDLCompilerArg("-fall");
        Options.addIDLCompilerArg("-oldImplBase");
        Options.setIDLFiles(idlFiles);
        Options.setJavaFiles(javaFiles);

        compileIDLFiles();
        compileJavaFiles();

        Controller orbd = createORBD();
        Controller client 
            = createClient("naming.pcosnaming.helloClient");

        Object serverExtras[] = new Object[3];

        Hashtable serverExtra = Options.getServerExtra();
        serverExtra.put("orbd", orbd);
        serverExtra.put("client", client);

        Controller server
            = createServer("naming.pcosnaming.helloServer");

        orbd.start();

        // This seems to be necessary on NT or else we try to restart
        // too soon, and a ghost process from the first ORBD process
        // is left beind to cause trouble.
        Thread.sleep(10000);

        // Server starts the client and does a waitFor
        server.start();

        // These shouldn't do anything in the current implementation
        server.waitFor();
        client.stop();
        server.stop();

        orbd.stop();
    }
}

