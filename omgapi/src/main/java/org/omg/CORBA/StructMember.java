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
 * File: ./org/omg/CORBA/StructMember.java
 * From: ./ir.idl
 * Date: Fri Aug 28 16:03:31 1998
 *   By: idltojava Java IDL 1.2 Aug 11 1998 02:00:18
 */

package org.omg.CORBA;

/**
 * Describes a member of an IDL <code>struct</code> in the 
 * Interface Repository, including
 * the  name of the <code>struct</code> member, the type of 
 * the <code>struct</code> member, and
 * the typedef that represents the IDL type of the 
 * <code>struct</code> member
 * described the <code>struct</code> member object.
 */
// @SuppressWarnings({"serial"})
public final class StructMember implements org.omg.CORBA.portable.IDLEntity {

    //  instance variables

    /**
     * The name of the struct member described by
     * this <code>StructMember</code> object.
     * @serial
     */
    public String name;

    /**
     * The type of the struct member described by
     * this <code>StructMember</code> object.
     * @serial
     */
    public org.omg.CORBA.TypeCode type;

    /**
     * The typedef that represents the IDL type of the struct member described by
     * this <code>StructMember</code> object.
     * @serial
     */
    public org.omg.CORBA.IDLType type_def;
    //  constructors

    /**
     * Constructs a default <code>StructMember</code> object.
     */
    public StructMember() { }

    /**
     * Constructs a <code>StructMember</code> object initialized with the
     * given values.
     * @param __name a <code>String</code> object with the name of the struct
     *        member
     * @param __type a <code>TypeCode</code> object describing the type of the struct
     *        member
     * @param __type_def an <code>IDLType</code> object representing the IDL type
     *        of the struct member
     */
    public StructMember(String __name, org.omg.CORBA.TypeCode __type, org.omg.CORBA.IDLType __type_def) {
        name = __name;
        type = __type;
        type_def = __type_def;
    }
}
