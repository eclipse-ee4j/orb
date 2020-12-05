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

/*
 * File: ./org/omg/CORBA/UnionMember.java
 * From: ./ir.idl
 * Date: Fri Aug 28 16:03:31 1998
 *   By: idltojava Java IDL 1.2 Aug 11 1998 02:00:18
 */

package org.omg.CORBA;

/**
 * A description in the Interface Repository of a member of an IDL union.
 */
// @SuppressWarnings({"serial"})
public final class UnionMember implements org.omg.CORBA.portable.IDLEntity {
    //  instance variables

    /**
     * The name of the union member described by this
     * <code>UnionMember</code> object.
     * @serial
     */
    public String name;

    /**
     * The label of the union member described by this
     * <code>UnionMember</code> object.
     * @serial
     */
    public org.omg.CORBA.Any label;

    /**
     * The type of the union member described by this
     * <code>UnionMember</code> object.
     * @serial
     */
    public org.omg.CORBA.TypeCode type;

    /**
     * The typedef that represents the IDL type of the union member described by this
     * <code>UnionMember</code> object.
     * @serial
     */
    public org.omg.CORBA.IDLType type_def;

    //  constructors

    /**
     * Constructs a new <code>UnionMember</code> object with its fields initialized
     * to null.
     */
    public UnionMember() { }

    /**
     * Constructs a new <code>UnionMember</code> object with its fields initialized
     * to the given values.
     *
     * @param __name a <code>String</code> object with the name of this 
     *        <code>UnionMember</code> object
     * @param __label an <code>Any</code> object with the label of this 
     *        <code>UnionMember</code> object
     * @param __type a <code>TypeCode</code> object describing the type of this 
     *        <code>UnionMember</code> object
     * @param __type_def an <code>IDLType</code> object that represents the
     *        IDL type of this <code>UnionMember</code> object
     */
    public UnionMember(String __name, org.omg.CORBA.Any __label, org.omg.CORBA.TypeCode __type, org.omg.CORBA.IDLType __type_def) {
        name = __name;
        label = __label;
        type = __type;
        type_def = __type_def;
    }
}
