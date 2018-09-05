/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 1998-1999 IBM Corp. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package test;

import rmic.HelloTest;
import com.sun.corba.ee.impl.util.Version;

/*
 * @test
 */
public class InstallTest extends HelloTest {

    private static boolean TIME_IT = true;
    long startTime;

    public String getName () {
        if (TIME_IT) startTime = System.currentTimeMillis();
        return Version.asString() +
            "\n    Running on JDK " + System.getProperty("java.version") + "\n" +
            "\n    Verifying installation (requires < 2 minutes to complete)";
    }

    public String getPassed () {
        if (TIME_IT) {
            long duration = System.currentTimeMillis() - startTime;
            return "completed successfully in " + duration/1000 + " seconds.";
        } else {
            return "successful.";
        }
    }

    public String getFailed(Error e) {
        return "failed, caught " + e;
    }
}
