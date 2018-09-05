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
// Created       : 2001 Sep 24 (Mon) 20:20:45 by Harold Carr.
// Last Modified : 2001 Sep 24 (Mon) 20:21:06 by Harold Carr.
//

package pi.serviceexample;

import corba.framework.Controller;
import corba.framework.CORBATest;

public class ServiceExampleTest
    extends
        CORBATest
{
    public static final String thisPackage =
        ServiceExampleTest.class.getPackage().getName();

    protected void doTest()
        throws
            Throwable
    {
        Controller orbd   = createORBD();
        orbd.start();

        // Remote.

        Controller loggingServer =
            createServer(thisPackage + ".LoggingServiceImpl",
                         "loggingServer") ;
        loggingServer.start();

        Controller arbitraryObjectServer =
            createServer(thisPackage + ".ArbitraryObjectServiceImpl",
                         "arbitraryObjectServer") ;
        arbitraryObjectServer.start();

        Controller client = createClient(thisPackage + ".Client",
                                         "client");

        client.start();
        client.waitFor();
        client.stop();
        arbitraryObjectServer.stop();
        loggingServer.stop();

        // Colocated.

        Controller colocatedServers = 
            createServer(thisPackage + ".ColocatedServers",
                         "colocatedClientServer");
        colocatedServers.start();
        client.start();
        client.waitFor();
        client.stop();
        colocatedServers.stop();

        orbd.stop();
    }
}

// End of file.

