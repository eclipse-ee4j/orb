/*
 * Copyright (c) 2016, 2020 Oracle and/or its affiliates.
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

package com.sun.corba.ee.impl.io;

/**
 * A repository of "shared secrets", which are a mechanism for
 * calling implementation-private methods in another package without
 * using reflection. A package-private class implements a public
 * interface and provides the ability to call package-private methods
 * within that package; the object implementing that interface is
 * provided through a third package to which access is restricted.
 * This framework avoids the primary disadvantage of using reflection
 * for this purpose, namely the loss of compile-time checking.
 */
public class SharedSecrets {
    private static JavaCorbaAccess javaCorbaAccess;

    public static JavaCorbaAccess getJavaCorbaAccess() {
        if (javaCorbaAccess == null) {
            // Ensure ValueUtility is initialized; we know that that class
            // provides the shared secret
            try {
                Class.forName(ValueUtility.class.getName(), true, ValueUtility.class.getClassLoader());
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        return javaCorbaAccess;
    }

    public static void setJavaCorbaAccess(JavaCorbaAccess access) {
        javaCorbaAccess = access;
    }

}
