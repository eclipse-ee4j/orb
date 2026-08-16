/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates.
 * Copyright (c) 1998-1999 IBM Corp. All rights reserved.
 * Copyright (c) 2022 Contributors to the Eclipse Foundation. All rights reserved.
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

package rmic;

import org.glassfish.rmic.iiop.Constants;
import org.glassfish.rmic.tools.java.ClassPath;

import corba.framework.TestngRunner;

public class ParseTest extends test.Test implements Constants {
    public static ClassPath createClassPath() {

        String path = System.getProperty("java.class.path");

        // Call BatchEnvironment directly. This used to go through reflection, back
        // when the target was sun.rmi.rmic.BatchEnvironment and could not be named
        // at compile time. It can be now, and reflection only hid the fact that the
        // method had grown a second parameter: the lookup failed at runtime and the
        // resulting Error was reported as something else entirely.
        //
        // Passing null for the system class path leaves BatchEnvironment to work it
        // out for itself, which is what rmic's own Main does when -bootclasspath is
        // not given.

        return org.glassfish.rmic.BatchEnvironment.createClassPath(path, null);
    }

    @Override
    public void run() {
        TestngRunner runner = new TestngRunner();
        runner.registerClass(TestExecutor.class);
        runner.run();
        if (runner.hasFailure()) {
            status = new Error("test failed");
        }
    }
}
