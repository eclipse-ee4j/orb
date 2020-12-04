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

package com.sun.corba.ee.spi.presentation.rmi ;

import java.lang.reflect.Method ;

/** Translates between methods on an interface and RMI-IIOP encodings
 * of those methods as names.
 */
public interface IDLNameTranslator 
{
    /** Get the interfaces that this IDLNameTranslator describes.
     * 
     * @return interfaces described
     */
    Class[] getInterfaces() ;

    /** Get all methods for this remote interface.
     * The methods are returned in a canonical order, that is,
     * they are always in the same order for a particular interface.
     * 
     * @return methods for interface
     */
    Method[] getMethods() ;

    /** Get the method from this IDLNameTranslator's interfaces that 
     * corresponds to the mangled name idlName.  Returns null
     * if there is no matching method.
     * 
     * @param idlName name of method
     * @return method with the specified name
     */
    Method getMethod( String idlName )  ;

    /** Get the mangled name that corresponds to the given method 
     * on this IDLNameTranslator's interface.  Returns null
     * if there is no matching name.
     * 
     * @param method method to get name of
     * @return the corresponding name
     */
    String getIDLName( Method method )  ;
}

