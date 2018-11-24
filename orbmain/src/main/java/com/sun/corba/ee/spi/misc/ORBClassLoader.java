/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.spi.misc;

/**
 * Based on feedback from bug report 4452016, all class loading in the ORB is isolated here. It is acceptable to use
 * Class.forName only when one is certain that the desired class should come from the core JDK.
 * <p>
 * Note that this class must not depend on generated log wrappers!
 */
public class ORBClassLoader {
    public static Class loadClass(String className) throws ClassNotFoundException {
        return getClassLoader().loadClass(className);
    }

    public static ClassLoader getClassLoader() {
        ClassLoader ccl = Thread.currentThread().getContextClassLoader();
        if (ccl != null)
            return ccl;
        else
            return ClassLoader.getSystemClassLoader();
    }
}
