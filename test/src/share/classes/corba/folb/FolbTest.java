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
// Created       : 2005 Jun 10 (Fri) 10:04:09 by Harold Carr.
// Last Modified : 2005 Sep 30 (Fri) 15:38:24 by Harold Carr.
//

package corba.folb;

import java.util.Properties;

import corba.framework.CORBATest;
import corba.framework.Controller;
import corba.framework.Options;

import com.sun.corba.ee.spi.misc.ORBConstants;

/**
 * @author Harold Carr
 */
public class FolbTest
    extends
        CORBATest
{
    protected void doTest()
        throws Exception
    {
        String thisPackage = FolbTest.class.getPackage().getName();

        Controller orbd = createORBD();
        Controller server;
        Controller client;

        orbd.start();

        //
        // Main test
        //

        server = createServer(thisPackage+"."+"Server", "Server");
        client = createClient(thisPackage+"."+"Client", "Client");

        server.start();
        client.start();

        client.waitFor(180000);

        client.stop();
        server.stop();

        /** TODO: Issue # GLASSFISH_CORBA-7. Fix and Uncomment following failing tests.
        //
        // ClientMulti test
        //

        server = createServer(thisPackage+"."+"Server", "ServerMulti");
        client = createClient(thisPackage+"."+"ClientMulti", "ClientMulti");
        server.start();
        client.start();
        
        client.waitFor(300000); // 5 minutes

        client.stop();
        server.stop();        
        
        //
        // ClientCircular test
        //

        server = createServer(thisPackage+"."+"Server", "ServerCircular");
        client = createClient(thisPackage+"."+"ClientCircular", "ClientCircular");
        server.start();
        client.start();
        
        client.waitFor(180000);

        client.stop();
        server.stop();
        
        //
        // ClientWaitTimeout test
        //

        server = createServer(thisPackage+"."+"Server", "ServerWaitTimeout");
        client = createClient(thisPackage+"."+"ClientWaitTimeout", "ClientWaitTimeout");
        server.start();
        client.start();
        
        client.waitFor(120000);
        * 
        * End Issue # GLASSFISH_CORBA-7.
        **/ 
        client.stop();
        server.stop();
        
        //
        // Cleanup
        //

        orbd.stop();
    }
}

// End of file.
