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

package com.sun.corba.ee.spi.misc ;

/**
 * Based on feedback from bug report 4452016, all class loading
 * in the ORB is isolated here.  It is acceptable to use
 * Class.forName only when one is certain that the desired class
 * should come from the core JDK.
 * <p>
 * Note that this class must not depend on generated log wrappers! 
 */
public class ORBClassLoader
{
    public static Class loadClass(String className) 
        throws ClassNotFoundException
    {
        return getClassLoader().loadClass(className);
    }

    public static ClassLoader getClassLoader() 
    {
        ClassLoader ccl = Thread.currentThread().getContextClassLoader() ;
        if (ccl != null)
            return ccl; 
        else
            return ClassLoader.getSystemClassLoader();
    }
}
