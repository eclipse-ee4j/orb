/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

//
// Created       : 2003 Dec 11 (Thu) 10:59:34 by Harold Carr.
// Last Modified : 2003 Dec 15 (Mon) 20:31:54 by Harold Carr.
//

package corba.legacyorbclasses;

import corba.framework.Controller;
import corba.framework.CORBATest;

public class LegacyORBClassesTest
    extends
        CORBATest
{
    public static final String thisPackage =
        LegacyORBClassesTest.class.getPackage().getName();

    protected void doTest() throws Throwable {
        Controller orbd   = createORBD();
        orbd.start();

        Controller client = createClient(thisPackage + ".Client");

        client.start();
        client.waitFor(120000);
        client.stop();

        orbd.stop();
    }
}

// End of file.


