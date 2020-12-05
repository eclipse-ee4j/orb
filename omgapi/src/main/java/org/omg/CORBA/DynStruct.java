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
 *  with an IDL struct.
 * @deprecated Use the new <a href="../DynamicAny/DynStruct.html">DynStruct</a> instead
 */

// @Deprecated
public interface DynStruct extends org.omg.CORBA.Object, org.omg.CORBA.DynAny
{
    /**
     * During a traversal, returns the name of the current member.
     *
     * @return the string name of the current member
     */
    public String current_member_name();

    /**
     * Returns the <code>TCKind</code> object that describes the kind of
         * the current member.
     *
     * @return the <code>TCKind</code> object that describes the current member
     */
    public org.omg.CORBA.TCKind current_member_kind();

    /**
     * Returns an array containing all the members of the stored struct.
     *
     * @return the array of name-value pairs
         * @see #set_members
     */
    public org.omg.CORBA.NameValuePair[] get_members();

    /**
     * Set the members of the struct.
     *
     * @param value the array of name-value pairs.
         * @throws org.omg.CORBA.DynAnyPackage.InvalidSeq if the given argument
         *         is invalid
         * @see #get_members
     */
    public void set_members(org.omg.CORBA.NameValuePair[] value)
        throws org.omg.CORBA.DynAnyPackage.InvalidSeq;
}
