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

