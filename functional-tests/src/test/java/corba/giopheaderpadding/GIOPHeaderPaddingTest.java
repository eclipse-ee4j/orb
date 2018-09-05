/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

//
// Created       : 2003 Apr 09 (Wed) 16:31:43 by Harold Carr.
// Last Modified : 2004 Jan 31 (Sat) 09:54:44 by Harold Carr.
//

package corba.giopheaderpadding;

import corba.framework.Controller;
import corba.framework.CORBATest;

import java.util.Properties;

import com.sun.corba.ee.spi.misc.ORBConstants;
import com.sun.corba.ee.spi.orb.ORB;

import corba.framework.*;

public class GIOPHeaderPaddingTest extends CORBATest {

    public static final String thisPackage =
        GIOPHeaderPaddingTest.class.getPackage().getName();

    protected void doTest() throws Throwable {
        if (test.Test.useJavaSerialization()) {
            return;
        }

        Controller orbd = createORBD();
        orbd.start();

        Properties clientProps = Options.getClientProperties();
        clientProps.put("org.omg.PortableInterceptor.ORBInitializerClass." +
                        "corba.giopheaderpadding.Client", "true");
        clientProps.put("org.omg.PortableInterceptor.ORBInitializerClass." +
                        "corba.giopheaderpadding.Server", "true");
        clientProps.put(ORBConstants.GIOP_VERSION, "1.2");
        clientProps.put(ORBConstants.GIOP_12_BUFFMGR, "0"); // GROW

        Properties serverProps = Options.getServerProperties();
        serverProps.put("org.omg.PortableInterceptor.ORBInitializerClass." +
                        "corba.giopheaderpadding.Server", "true");
        serverProps.put(ORBConstants.GIOP_VERSION, "1.2");
        serverProps.put(ORBConstants.GIOP_12_BUFFMGR, "0"); // GROW

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

