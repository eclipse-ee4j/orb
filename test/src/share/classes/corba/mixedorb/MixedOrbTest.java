/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package corba.mixedorb;

import test.Test;
import corba.framework.*;
import java.util.*;

public class MixedOrbTest extends CORBATest
{
    private static final String[] javaFiles = { "Client.java", "Echo.java",
        "EchoImpl.java" }  ;

    private static final String[] rmicClasses = { "corba.mixedorb.EchoImpl" } ;

    protected void doTest() throws Throwable
    {
        Options.setRMICClasses(rmicClasses) ;
        Options.addRMICArgs( "-iiop -keep -g -poa" ) ;
        Options.setJavaFiles( javaFiles ) ;

        compileRMICFiles();
        compileJavaFiles();

        Controller client = createClient( "corba.mixedorb.Client" ) ;

        client.start();

        // Wait for the client to finish for up to 2 minutes, then
        // throw an exception.
        client.waitFor(120000);

        // Make sure all the processes are shut down.
        client.stop();
    }
}

