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
