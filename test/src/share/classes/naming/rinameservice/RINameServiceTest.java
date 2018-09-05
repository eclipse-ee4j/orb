/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package naming.rinameservice;

import test.Test;
import corba.framework.*;
import java.util.*;

/**
 * RINameServiceTest compiles couple of JavaFiles (StandAlone NameServer and
 * Simple Client to test INS functionality ) and starts RINameServer
 * and a simple client to test that StandAlone NameServer works.
 */
public class RINameServiceTest extends CORBATest
{
    public static String[] javaFiles = {"NameServer.java",
                                        "NameServiceClient.java"};

    protected void doTest() throws Throwable
    {
        Options.setJavaFiles(javaFiles);

        compileJavaFiles( );
 
        Controller client = createClient(
            "naming.rinameservice.NameServiceClient" );
        Controller server = createServer(
            "naming.rinameservice.NameServer" );

        server.start();
        Thread.sleep( 10000 );
        client.start();
        Thread.sleep( 10000 );
        
        server.stop();
        client.stop();
    }
}


    

