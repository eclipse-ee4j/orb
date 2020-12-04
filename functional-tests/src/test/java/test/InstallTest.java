/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates.
 * Copyright (c) 1998-1999 IBM Corp. All rights reserved.
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
