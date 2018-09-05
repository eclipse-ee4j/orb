/*
 * Copyright (c) 2016, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
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
