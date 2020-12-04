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
 * Associates  a name with a value that is an
 * attribute of an IDL struct, and is used in the <tt>DynStruct</tt> APIs.
 */

// @SuppressWarnings({"serial"})
public final class NameValuePair implements org.omg.CORBA.portable.IDLEntity {

    /**
     * The name to be associated with a value by this <code>NameValuePair</code> object.
     */
    public String id;

    /**
     * The value to be associated with a name by this <code>NameValuePair</code> object.
     */
    public org.omg.CORBA.Any value;

    /**
     * Constructs an empty <code>NameValuePair</code> object.
     * To associate a name with a value after using this constructor, the fields
     * of this object have to be accessed individually.
     */
    public NameValuePair() { }
        
    /**
     * Constructs a <code>NameValuePair</code> object that associates
     * the given name with the given <code>org.omg.CORBA.Any</code> object.
     * @param __id the name to be associated with the given <code>Any</code> object
     * @param __value the <code>Any</code> object to be associated with the given name
     */
    public NameValuePair(String __id, org.omg.CORBA.Any __value) {
        id = __id;
        value = __value;
    }
}
