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

package com.sun.corba.ee.spi.orb;

public interface ClassCodeBaseHandler {
    /**
     * Returns some sort of codebase for the given class, or null. It is expected that, if str is the result of getCodeBase(
     * cls ), then loadClass( str, cls.getClassName() ) will return cls.
     * 
     * @param cls Class for which we need a codebase
     * @return A codebase to use with this handler, or null if this handler does not apply to this class.
     */
    String getCodeBase(Class<?> cls);

    /**
     * load a class given the classname and a codebase. The className will always satisfy cls.getClassName().equals(
     * className ) if the call succeeds and returns a Class.
     * 
     * @param codebase A string that somehow describes which ClassLoader to use. For example, the string could be an
     * ordinary URL that a URL ClassLoader can use, or something more specialized, such as a description of an OSGi bundles
     * and version.
     * @param className The name of the class to load
     * @return The loaded class, or null if the class could not be loaded.
     */
    Class<?> loadClass(String codebase, String className);
}
