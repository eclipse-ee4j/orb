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

package hopper.h4549085;

import test.Test;
import corba.framework.*;
import java.util.*;
import com.sun.corba.ee.spi.misc.ORBConstants;
import org.omg.CORBA.*;

/**
 * Simple tests in GIOP 1.1 and 1.2 of chars and wstrings.
 */
public class LockedCodeSetTest extends CORBATest
{
    public static final String[] idlFiles = { "Tester.idl" };

    public static final String[] javaFiles = { "Server.java",
                                               "Client.java" };


    protected void doTest() throws Throwable
    {
        Options.addIDLCompilerArgs("-fall");
        Options.setIDLFiles(idlFiles);
        Options.setJavaFiles(javaFiles);
        compileIDLFiles();
        compileJavaFiles();

        Controller orbd = createORBD();

        // Make the server only advertise UTF-8 for char, forcing the
        // client to select it.  The server will still use ISO8859-1 to
        // unmarshal the operation name, but should be able to handle
        // multibyte chars after the service context is unmarshaled.
        Properties serverProps = Options.getServerProperties();

        serverProps.setProperty(ORBConstants.CHAR_CODESETS,
                                "83951617,83951617");

        Controller server = createServer("hopper.h4549085.Server");
        Controller client = createClient("hopper.h4549085.Client");

        orbd.start();
        server.start();
        client.start();

        // Wait for the client to finish for up to 2 minutes, then
        // throw an exception.
        client.waitFor(120000);
        client.stop();
        server.stop();
        orbd.stop();
    }
}
    
