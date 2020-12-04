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

package corba.invocation;

import test.Test;
import corba.framework.*;
import java.util.*;

/**
 * This tests if invocation on non-existent targets results in COMM_FAILURE.
 */
public class InvocationTest extends CORBATest {
    public static String URL_PROPERTY = "naming.instest.urlProperty";

    protected void doTest() throws Throwable {

        Properties clientProps = Options.getClientProperties();
        clientProps.setProperty(URL_PROPERTY,
                                "corbaloc:iiop:1.2@localhost:" + 3050 + '/' +
                                "HelloService");
        Controller client = createClient("corba.invocation.Client");
        client.start();

        client.waitFor(60000);

        // Make sure all the processes are shut down.
        client.stop();
    }
}
