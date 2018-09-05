/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
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
