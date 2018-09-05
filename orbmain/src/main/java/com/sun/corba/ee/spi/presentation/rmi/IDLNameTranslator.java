/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.spi.presentation.rmi ;

import java.lang.reflect.Method ;

/** Translates between methods on an interface and RMI-IIOP encodings
 * of those methods as names.
 */
public interface IDLNameTranslator 
{
    /** Get the interfaces that this IDLNameTranslator describes.
     */
    Class[] getInterfaces() ;

    /** Get all methods for this remote interface.
     * The methods are returned in a canonical order, that is,
     * they are always in the same order for a particular interface.
     */
    Method[] getMethods() ;

    /** Get the method from this IDLNameTranslator's interfaces that 
     * corresponds to the mangled name idlName.  Returns null
     * if there is no matching method.
     */
    Method getMethod( String idlName )  ;

    /** Get the mangled name that corresponds to the given method 
     * on this IDLNameTranslator's interface.  Returns null
     * if there is no matching name.
     */
    String getIDLName( Method method )  ;
}

