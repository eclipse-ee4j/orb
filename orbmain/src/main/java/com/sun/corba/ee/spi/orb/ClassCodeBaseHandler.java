/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.spi.orb;

public interface ClassCodeBaseHandler {
    /** Returns some sort of codebase for the given class, or null.
     * It is expected that, if str is the result of getCodeBase( cls ), 
     * then loadClass( str, cls.getClassName() ) will return cls.
     * <return>A codebase to use with this handler, or null if this handler
     * does not apply to this class.
     * @param cls Class for which we need a codebase
     * @return Codebase string
     */
    String getCodeBase( Class<?> cls ) ;

    /** load a class given the classname and a codebase.
     * The className will always satisfy cls.getClassName().equals( className ) 
     * if the call succeeds and returns a Class.
     * <param>codebase A string that somehow describes which ClassLoader to use.
     * For example, the string could be an ordinary URL that a URL ClassLoader can use,
     * or something more specialized, such as a description of an OSGi bundles and version.
     * <param>className The name of the class to load.
     * <return>The loaded class, or null if the class could not be loaded.
     * @param codebase The codebase to use for finding the ClassLoader
     * @param className The name of the class to load
     * @return The loaded class
     */
    Class<?> loadClass( String codebase, String className ) ;
}
