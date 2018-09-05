/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package corba.cdrext;

public class Status
{
    public static boolean writeReplaceCalled() {
        return writeReplaceCalled;
    }

    public static boolean readResolveCalled() {
        return readResolveCalled;
    }

    public static void reset() {
        writeReplaceCalled = false;
        readResolveCalled = false;
    }

    public static void inWriteReplace() {
        writeReplaceCalled = true;
    }

    public static void inReadResolve() {
        readResolveCalled = true;
    }

    private static boolean writeReplaceCalled;
    private static boolean readResolveCalled;
}
