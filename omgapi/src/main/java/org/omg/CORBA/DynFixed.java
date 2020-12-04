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

package org.omg.CORBA;

/**
 *  Represents a <code>DynAny</code> object that is associated
 *  with an IDL fixed type.
 * @deprecated Use the new <a href="../DynamicAny/DynFixed.html">DynFixed</a> instead
 */

public interface DynFixed extends org.omg.CORBA.Object, org.omg.CORBA.DynAny
{
    /**
     * Returns the value of the fixed type represented in this
     * <code>DynFixed</code> object.
     *
     * @return the value as a byte array
         * @see #set_value
     */
    public byte[] get_value();

    /**
     * Sets the given fixed type instance as the value for this 
     * <code>DynFixed</code> object.
     *
     * @param val the value of the fixed type as a byte array
         * @throws org.omg.CORBA.DynAnyPackage.InvalidValue if the given
         *         argument is bad
         * @see #get_value
     */
    public void set_value(byte[] val)
        throws org.omg.CORBA.DynAnyPackage.InvalidValue;
}
