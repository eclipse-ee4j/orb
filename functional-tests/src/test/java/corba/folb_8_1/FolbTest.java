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
// Created       : 2002 Jul 19 (Fri) 14:49:22 by Harold Carr.
// Last Modified : 2005 Jun 01 (Wed) 11:06:20 by Harold Carr.
//

package corba.folb_8_1;

import corba.framework.Controller;
import corba.framework.CORBATest;

/**
 * @author Harold Carr
 */
public class FolbTest
    extends
        CORBATest
{
    public static final String thisPackage =
        FolbTest.class.getPackage().getName();

    protected void doTest()
        throws
            Throwable
    {
        Controller orbd;
        Controller server;
        Controller client;
        Controller colocated ;
        ////////////////////////////////////////////////////

        orbd   = createORBD();
        orbd.start();

        ////////////////////////////////////////////////////
        server = createServer(thisPackage + "." + "Server", "Server");
        client = createClient(thisPackage + "." + "Client", "Client");
        server.start();
        client.start();
        client.waitFor();
        client.stop();
        server.stop();

        ////////////////////////////////////////////////////

        server = createServer(thisPackage + "." + "Server",
                              "ServerForSticky");
        client = createClient(thisPackage + "." + "ClientWithSticky",
                              "ClientWithSticky");
        server.start();
        client.start();
        client.waitFor();
        client.stop();
        server.stop();

        ////////////////////////////////////////////////////

        colocated = createClient(thisPackage + "." + "ColocatedCS",
                                            "ColocatedCS");
        colocated.start();
        colocated.waitFor();
        colocated.stop();

        ////////////////////////////////////////////////////

        colocated = createClient(thisPackage + "." + "ColocatedCSWithSticky",
                                 "ColocatedCSWithSticky");
        colocated.start();
        colocated.waitFor();
        colocated.stop();

        ////////////////////////////////////////////////////

        server = createServer(thisPackage + "." + "Server",
                              "ServerForSticky");
        client = createClient(thisPackage + "." + "ClientTwoRefs",
                              "ClientTwoRefs");
        server.start();
        client.start();
        client.waitFor();
        client.stop();
        server.stop();

        ////////////////////////////////////////////////////

        colocated = createClient(thisPackage + "." + "ColocatedClientTwoRefs",
                                 "ColocatedClientTwoRefs");
        colocated.start();
        colocated.waitFor();
        colocated.stop();

        ////////////////////////////////////////////////////
        /** TODO: Issue # GLASSFISH_CORBA-7. Fix and Uncomment following failing tests.
         * 
        server = createServer(thisPackage + "." + "Server",
                              "ServerForTiming1");
        server.start();
        client = createClient(thisPackage + "." + "ClientForTiming_NoFs_NoF_NoC",
                              "ClientForTiming_NoFs_NoF_NoC");
        client.start();
        client.waitFor();
        client.stop();
        server.stop();

        //-------------------------

        server = createServer(thisPackage + "." + "Server",
                              "ServerForTiming2");
        server.start();
        client = createClient(thisPackage + "." + "ClientForTiming_Fs_NoF_NoC",
                              "ClientForTiming_Fs_NoF_NoC");
        client.start();
        client.waitFor();
        client.stop();
        server.stop();

        //-------------------------

        server = createServer(thisPackage + "." + "Server",
                              "ServerForTiming3");
        server.start();
        client = createClient(thisPackage + "." + "ClientForTiming_Fs_NoF_C",
                              "ClientForTiming_Fs_NoF_C");
        client.start();
        client.waitFor();
        client.stop();
        server.stop();

        //-------------------------


        server = createServer(thisPackage + "." + "Server",
                              "ServerForTiming4");
        server.start();
        client = createClient(thisPackage + "." + "ClientForTiming_Fs_F_NoC",
                              "ClientForTiming_Fs_F_NoC");
        client.start();
        client.waitFor();
        client.stop();
        server.stop();

        //-------------------------
        server = createServer(thisPackage + "." + "Server",
                              "ServerForTiming");
        server.start();
        client = createClient(thisPackage + "." + "ClientForTiming_Fs_F_C",
                              "ClientForTiming_Fs_F_C");
        client.start();
        client.waitFor();
        client.stop();
        server.stop();
        *** End Issue # GLASSFISH_CORBA-7.
        ***/
        ////////////////////////////////////////////////////

        orbd.stop();
    }
}

// End of file.

