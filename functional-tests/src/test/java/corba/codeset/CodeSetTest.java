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
    
