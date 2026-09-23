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

package com.sun.corba.ee.impl.corba;

public class Enum1Helper {
    // It is useless to have instances of this class
    private Enum1Helper() {
    }

    public static void write(org.omg.CORBA.portable.OutputStream out, Enum1 that) {
        out.write_long(that.value());
    }

    public static Enum1 read(org.omg.CORBA.portable.InputStream in) {
        return Enum1.from_int(in.read_long());
    }

    public static Enum1 extract(org.omg.CORBA.Any a) {
        org.omg.CORBA.portable.InputStream in = a.create_input_stream();
        return read(in);
    }

    public static void insert(org.omg.CORBA.Any a, Enum1 that) {
        org.omg.CORBA.portable.OutputStream out = a.create_output_stream();
        write(out, that);
        a.read_value(out.create_input_stream(), type());
    }

    private static org.omg.CORBA.TypeCode _tc;
    private static final int _memberCount = 4;
    private static String[] _members = { "zeroth", "first", "second", "third" };

    synchronized public static org.omg.CORBA.TypeCode type() {
        if (_tc == null)
            _tc = org.omg.CORBA.ORB.init().create_enum_tc(id(), "enum1", _members);
        return _tc;
    }

    public static String id() {
        return "IDL:anytest/enum1:1.0";
    }
}
