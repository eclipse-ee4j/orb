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

