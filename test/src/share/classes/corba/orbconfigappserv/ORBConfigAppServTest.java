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
// Created       : 2003 Apr 15 (Tue) 17:31:08 by Harold Carr.
// Last Modified : 2003 Apr 17 (Thu) 21:13:48 by Harold Carr.
//

package corba.orbconfigappserv;

import test.Test;
import corba.framework.*;

public class ORBConfigAppServTest extends CORBATest
{
    public static final String thisPackage =
        ORBConfigAppServTest.class.getPackage().getName();

    protected void doTest() throws Throwable
    {
        Controller client =
            createClient(thisPackage + ".ORBManager", "ORBManager");
        client.start();
        client.waitFor(120000);
        client.stop();
    }
}

// End of file.


