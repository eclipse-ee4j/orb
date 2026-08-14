/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
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

package test.suites;

import java.io.IOException;
import java.util.Collection;

import org.junit.BeforeClass;
import org.junit.runners.Parameterized.Parameters;

/** RMI-IIOP suite (test/AllTests.tdesc). */
public class AllTdescSuiteTest extends TdescSuite {

    /**
     * The Ant test-rmi-iiop target ran com.sun.corba.ee.impl.util.ORBProperties immediately
     * before this suite, so it is preserved here rather than quietly dropped.
     *
     * Be aware of what it does: if ${java.home}/lib/orb.properties is absent it writes the ORB
     * class defaults there -- permanently modifying the JDK installation -- and swallows every
     * exception. It is a no-op whenever that file already exists. Whether it is still needed at
     * all is being investigated separately; it is kept for now purely to preserve behaviour.
     */
    @BeforeClass
    public static void writeOrbPropertiesIfAbsent() {
        com.sun.corba.ee.impl.util.ORBProperties.main(new String[0]);
    }

    @Parameters(name = "{0}")
    public static Collection<Object[]> data() throws IOException {
        return entries("/test/AllTests.tdesc");
    }
}
