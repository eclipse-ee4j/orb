/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Eclipse Distribution License
 * v. 1.0 which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License v. 2.0 are satisfied: GNU General Public License v2.0
 * w/Classpath exception which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR BSD-3-Clause OR GPL-2.0 WITH
 * Classpath-exception-2.0
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

