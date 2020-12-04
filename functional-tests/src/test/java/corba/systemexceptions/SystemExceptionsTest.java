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

//
// Created       : 2003 Apr 09 (Wed) 16:31:43 by Harold Carr.
// Last Modified : 2004 Jan 31 (Sat) 09:54:44 by Harold Carr.
//

package corba.systemexceptions;

import java.util.Properties;

import corba.framework.Controller;
import corba.framework.CORBATest;

import corba.framework.*;

public class SystemExceptionsTest extends CORBATest {
    public static final String thisPackage =
        SystemExceptionsTest.class.getPackage().getName();
    
    protected void doTest() throws Throwable {
        Controller orbd = createORBD();
        orbd.start();

        Properties clientProps = Options.getClientProperties();
        clientProps.put("org.omg.PortableInterceptor.ORBInitializerClass." +
                        "corba.systemexceptions.Client", "true");

        doTestType("Server", "Server",
                   "Client", "Client");

        Controller colocatedClientServer = 
            createClient(thisPackage + ".ColocatedClientServer",
                         "colocatedClientServer");
        colocatedClientServer.start();
        colocatedClientServer.waitFor();
        colocatedClientServer.stop();

        orbd.stop();
    }

    protected void doTestType(String serverMainClass, String serverTestName,
                              String clientMainClass, String clientTestName)
        throws Throwable {

        Controller server = createServer(thisPackage + "." + serverMainClass,
                                         serverTestName);
        server.start();

        Controller client = createClient(thisPackage + "." + clientMainClass,
                                         clientTestName);
        client.start();
        client.waitFor();
        client.stop();

        server.stop();
    }
}

// End of file.

