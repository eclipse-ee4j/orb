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
// Created       : 2003 Apr 09 (Wed) 16:31:43 by Harold Carr.
// Last Modified : 2004 Jan 31 (Sat) 09:54:44 by Harold Carr.
//

package corba.rmic;

import corba.framework.Controller;
import corba.framework.CORBATest;

import corba.framework.*;

public class RmicTest extends CORBATest {

    public static String[] rmicClasses = { "corba.rmic.CImpl" };

    protected void doTest() throws Throwable {
        Options.addRMICArgs("-g -iiop -poa -always -keep");
        Options.setRMICClasses(rmicClasses);
        compileRMICFiles();
    }
}

// End of file.

