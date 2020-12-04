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
 * The representation of a <code>DynAny</code> object that is associated
 *  with an IDL value type.
 * @deprecated Use the new <a href="../DynamicAny/DynValue.html">DynValue</a> instead
 */
public interface DynValue extends org.omg.CORBA.Object, org.omg.CORBA.DynAny {

    /**
     * Returns the name of the current member while traversing a
     * <code>DynAny</code> object that represents a Value object.
     *
     * @return the name of the current member
     */
    String current_member_name();

    /**
     * Returns the <code>TCKind</code> object that describes the current member.
     *
     * @return the <code>TCKind</code> object corresponding to the current
     * member
     */
    TCKind current_member_kind();

    /**
     * Returns an array containing all the members of the value object
     * stored in this <code>DynValue</code>.
     *
     * @return an array of name-value pairs.
         * @see #set_members
     */
    org.omg.CORBA.NameValuePair[] get_members();

    /**
     * Sets the members of the value object this <code>DynValue</code>
     * object represents to the given array of <code>NameValuePair</code>
         * objects.
     *
     * @param value the array of name-value pairs to be set
     * @throws org.omg.CORBA.DynAnyPackage.InvalidSeq
     *         if an inconsistent value is part of the given array
         * @see #get_members
     */
    void set_members(NameValuePair[] value)
        throws org.omg.CORBA.DynAnyPackage.InvalidSeq;
}
