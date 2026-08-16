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

package com.sun.corba.ee.impl.util;

/**
 * PackagePrefixChecker provides static utility methods for getting package prefixes.
 *
 * @author M. Mortazavi
 */

public final class PackagePrefixChecker {
    private static final String PACKAGE_PREFIX = "org.omg.stub.";
    private static final int PACKAGE_PREFIX_LENGTH = PACKAGE_PREFIX.length();

    public static String packagePrefix() {
        return PACKAGE_PREFIX;
    }

    public static String correctPackageName(String p) {
        if (isOffendingPackage(p)) {
            return PACKAGE_PREFIX + p;
        } else {
            return p;
        }
    }

    public static boolean isOffendingPackage(String p) {
        return p != null && (p.equals("java") || p.equals("javax"));
    }

    public static boolean hasOffendingPrefix(String p) {
        return p.startsWith("java.") || p.startsWith("javax.");
    }

    public static boolean hasBeenPrefixed(String p) {
        return p.startsWith(PACKAGE_PREFIX);
    }

    public static String withoutPackagePrefix(String p) {
        if (hasBeenPrefixed(p)) {
            return p.substring(PACKAGE_PREFIX_LENGTH);
        } else {
            return p;
        }
    }
}
