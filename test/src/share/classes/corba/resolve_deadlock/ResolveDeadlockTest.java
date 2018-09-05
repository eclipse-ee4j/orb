/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package corba.resolve_deadlock;

import test.Test;
import corba.framework.*;
import java.util.*;

public class ResolveDeadlockTest extends CORBATest
{
    private static String[] javaFiles = { "ResolveDeadlock.java" };

    // This is the main method defining the test.  All tests
    // should have this.
    protected void doTest() throws Throwable
    {
        Options.setJavaFiles( javaFiles ) ;

        compileJavaFiles();

        Controller client = createClient( "corba.resolve_deadlock.ResolveDeadlock" ) ;

        client.start();

        // Wait for the client to finish for up to 1 minute, then
        // throw an exception.
        client.waitFor(120000);

        // Make sure all the processes are shut down.
        client.stop();
    }
}

