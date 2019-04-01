/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.util;

import java.io.File;

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
        if (isOffendingPackage(p))
            return PACKAGE_PREFIX + p;
        else
            return p;
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
        if (hasBeenPrefixed(p))
            return p.substring(PACKAGE_PREFIX_LENGTH);
        else
            return p;
    }
}
