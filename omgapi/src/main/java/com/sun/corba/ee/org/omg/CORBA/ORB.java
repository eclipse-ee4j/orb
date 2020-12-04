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

package com.sun.corba.ee.org.omg.CORBA ;

import org.omg.CORBA.ORBPackage.InvalidName ;

/** This ORB class is defined to provide a home for the
* register_initial_reference operation, which is added by
* portable interceptors and CORBA 2.4.  It is added here
* until there is an official Java language mapping for the
* method.
*/
abstract public class ORB extends org.omg.CORBA_2_3.ORB 
{
    /**
     * If this operation is called with an id, <code>"Y"</code>, and an
     * object, <code>YY</code>, then a subsequent call to
     * <code>ORB.resolve_initial_references( "Y" )</code> will
     * return object <code>YY</code>.
     *   
     * @param id The ID by which the initial reference will be known.
     * @param obj The initial reference itself.
     * @throws InvalidName if this operation is called with an empty string id
     *     or this operation is called with an id that is already registered,
     *     including the default names defined by OMG.
     * @throws org.omg.CORBA.BAD_PARAM if the obj parameter is null.
     */  
    public void register_initial_reference( String id,
                                            org.omg.CORBA.Object obj )
        throws InvalidName
    {
        throw new org.omg.CORBA.NO_IMPLEMENT();
    }
}
