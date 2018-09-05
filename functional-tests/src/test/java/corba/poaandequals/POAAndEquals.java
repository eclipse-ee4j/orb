/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package corba.poaandequals;

import test.Test;
import corba.framework.*;
import java.util.*;
import org.omg.CORBA.*;

public class POAAndEquals extends CORBATest
{
    protected Controller newServerController()
    {
        return new InternalExec();
    }

    protected Controller newClientController()
    {
        return new ThreadExec();
    }

    private ORB createORB() {
        Properties props = new Properties();
        props.put("org.omg.CORBA.ORBClass",
                  Options.getORBClass());
        String args[] = new String[] { "-ORBInitialPort",
                                       Options.getORBInitialPort() };
 
        return ORB.init(args, props);
    }

    protected void doTest() throws Throwable
    {
        ORB orb = createORB();

        Hashtable clientExtra = Options.getClientExtra();
        clientExtra.put("orb", orb);

        Controller client 
            = createClient("corba.poaandequals.WombatClient");

        Hashtable serverExtra = Options.getServerExtra();
        serverExtra.put("orb", orb);
        serverExtra.put("client", client);

        Controller server
            = createServer("corba.poaandequals.WombatServer");

        server.start();
        server.waitFor();

        client.stop();

        server.stop();

        // orb.shutdown(true);
    }
}

