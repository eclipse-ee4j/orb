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

import org.junit.runners.Parameterized.Parameters;

/**
 * RMI-IIOP suite (test/AllTests.tdesc).
 *
 * The Ant test-rmi-iiop target ran com.sun.corba.ee.impl.util.ORBProperties immediately before this suite; that is
 * deliberately not carried over. ORBProperties writes the ORB class defaults into ${java.home}/lib/orb.properties when
 * that file is absent -- permanently altering the JDK installation, and swallowing every exception on the way -- yet
 * nothing here needs it: every JVM the harness starts is given org.omg.CORBA.ORBClass and ORBSingletonClass explicitly,
 * and system properties take precedence over orb.properties in the ORB's lookup order.
 *
 * Verified rather than assumed, by running the whole suite twice from a pristine machine, with and without the call:
 * 230 Surefire cases green in both, per-entry results identical by name, and without the call
 * ${java.home}/lib/orb.properties is never created.
 */
public class AllTdescSuiteTest extends TdescSuite {

    @Parameters(name = "{0}")
    public static Collection<Object[]> data() throws IOException {
        return entries("/test/AllTests.tdesc");
    }
}
