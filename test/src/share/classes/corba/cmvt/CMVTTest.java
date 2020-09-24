/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2020 Payara Services Ltd.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package corba.cmvt;

import test.Test;
import corba.framework.*;
import java.util.*;
import com.sun.corba.ee.spi.misc.ORBConstants;

public class CMVTTest extends CORBATest {
    static final int GROW = 0;
    static final int STREAM = 2;
    static String[] GIOP_version = { "1.0", "1.1", "1.2" };
    static String[] GIOP_strategy = { "GROW", "STRM" };

    private void printBeginTest(int clientVersion,
                                int clientStrategy,
                                int serverVersion,
                                int serverStrategy)
    {
        StringBuilder output = new StringBuilder(80);

        // Pleasing aesthetics
        output.append("      ");

        output.append(GIOP_version[clientVersion]);
        output.append(" ");
        output.append(GIOP_strategy[clientStrategy]);
        output.append(" client <> ");
        output.append(GIOP_version[serverVersion]);
        output.append(" ");
        output.append(GIOP_strategy[serverStrategy]);
        output.append(" server: ");

        System.out.print(output.toString());
    }

    private void printEndTest(String result)
    {
        System.out.println(result);
    }

    private void setClient(int version, int strategy){
        Properties clientProps = Options.getClientProperties();

        int fragmentSize = 1024;
        clientProps.put(ORBConstants.GIOP_FRAGMENT_SIZE, "" + fragmentSize);

        clientProps.put(ORBConstants.GIOP_VERSION, GIOP_version[version]);
        clientProps.put(ORBConstants.GIOP_11_BUFFMGR, "" + GIOP_strategy[strategy]);
        clientProps.put(ORBConstants.GIOP_12_BUFFMGR, "" + GIOP_strategy[strategy]);
    }

    private void setServer(int version, int strategy){
        Properties serverProps = Options.getServerProperties();

        serverProps.put(ORBConstants.GIOP_VERSION, GIOP_version[version]);
        serverProps.put(ORBConstants.GIOP_11_BUFFMGR, "" + GIOP_strategy[strategy]);
        serverProps.put(ORBConstants.GIOP_12_BUFFMGR, "" + GIOP_strategy[strategy]);
    }

    private void runTest( String name ) throws Throwable{
        Controller server = createServer("corba.cmvt.Server", name);
        Controller client = createClient("corba.cmvt.Client", name);

        server.start();
        client.start();

        client.waitFor(60000);

        if (client.exitValue() != Controller.SUCCESS) {
            printEndTest("FAILED, Client exit value = " + client.exitValue());
        } else if (server.finished()) {
            printEndTest("FAILED, Server crashed");
        } else {
            printEndTest("PASSED");
        }

        client.stop();
        server.stop();
    }

    protected void doTest() throws Throwable  
    {
        int errors = 0;

        // Pleasing aesthetics
        System.out.println();

        //1.0 + grow
        setClient(0,0);
        setServer(0,0);
        printBeginTest(0,0,0,0);
        runTest( "1_0_grow" );

        //1.2 + grow
        setClient(2,0);
        setServer(2,0);
        printBeginTest(2,0,2,0);
        runTest( "1_2_grow" );

        //1.2 + stream
        setClient(2,1);
        setServer(2,1);
        printBeginTest(2,1,2,1);
        runTest( "1_2_stream" );

        System.out.print("      Test result : " );
        
        if (errors > 0)
            throw new Exception("Errors detected");

    }
}

