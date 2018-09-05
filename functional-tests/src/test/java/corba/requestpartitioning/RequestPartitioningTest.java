/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

// Test the request partitioning feature.
//
// This test creates a Server and a Client.
// The Server is configured to recieve requests for a given thread pool.
//
// Created       : 2004 May 23 by Charlie Hunt.
// Last Modified : 2004 May 23 by Charlie Hunt.
//

package corba.requestpartitioning;

import com.sun.corba.ee.spi.misc.ORBConstants;
import corba.framework.Controller;
import corba.framework.CORBATest;
import corba.framework.Options;
import java.util.Properties;

public class RequestPartitioningTest
    extends
        CORBATest
{
    public static final String thisPackage =
        RequestPartitioningTest.class.getPackage().getName();

    private final static int CLIENT_TIMEOUT = 90000;

    protected void doTest()
        throws
            Throwable
    {
        // Run test with DirectByteBuffers
        Controller orbd = createORBD();
        orbd.start();

        Properties serverProps = Options.getServerProperties();
        serverProps.setProperty(ORBConstants.ALWAYS_ENTER_BLOCKING_READ_PROPERTY, "true");
//        serverProps.setProperty(ORBConstants.DEBUG_PROPERTY,"transport,giop");
        Controller server = createServer(thisPackage + ".Server","Server1");
        server.start();

        Properties clientProps = Options.getClientProperties();
        clientProps.setProperty(ORBConstants.ALWAYS_ENTER_BLOCKING_READ_PROPERTY, "true");
//        clientProps.setProperty(ORBConstants.DEBUG_PROPERTY,"transport,giop");
        Controller client = createClient(thisPackage + ".Client", "Client1");
        client.start();

        client.waitFor(CLIENT_TIMEOUT);

        client.stop();
        server.stop();

        serverProps.setProperty(ORBConstants.DISABLE_DIRECT_BYTE_BUFFER_USE_PROPERTY, "true");
        serverProps.setProperty(ORBConstants.ALWAYS_ENTER_BLOCKING_READ_PROPERTY, "false");
        server = createServer(thisPackage + ".Server","Server2");
        server.start();

        clientProps.setProperty(ORBConstants.DISABLE_DIRECT_BYTE_BUFFER_USE_PROPERTY, "true");
        clientProps.setProperty(ORBConstants.ALWAYS_ENTER_BLOCKING_READ_PROPERTY, "false");
        client = createClient(thisPackage + ".Client", "Client2");
        client.start();

        client.waitFor(CLIENT_TIMEOUT);

        client.stop();
        server.stop();

        orbd.stop();
    }
}

// End of file.
