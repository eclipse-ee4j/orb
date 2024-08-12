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

package com.sun.org.omg.CORBA;

/**
 * com/sun/org/omg/CORBA/VersionSpecHelper.java Generated by the IDL-to-Java compiler (portable), version "3.0" from
 * ir.idl Thursday, May 6, 1999 1:51:43 AM PDT
 */

public final class VersionSpecHelper {
    private static String _id = "IDL:omg.org/CORBA/VersionSpec:1.0";

    public VersionSpecHelper() {
    }

    public static void insert(org.omg.CORBA.Any a, String that) {
        org.omg.CORBA.portable.OutputStream out = a.create_output_stream();
        a.type(type());
        write(out, that);
        a.read_value(out.create_input_stream(), type());
    }

    public static String extract(org.omg.CORBA.Any a) {
        return read(a.create_input_stream());
    }

    private static org.omg.CORBA.TypeCode __typeCode = null;

    synchronized public static org.omg.CORBA.TypeCode type() {
        if (__typeCode == null) {
            __typeCode = org.omg.CORBA.ORB.init().create_string_tc(0);
            __typeCode = org.omg.CORBA.ORB.init().create_alias_tc(com.sun.org.omg.CORBA.VersionSpecHelper.id(), "VersionSpec", __typeCode);
        }
        return __typeCode;
    }

    public static String id() {
        return _id;
    }

    public static String read(org.omg.CORBA.portable.InputStream istream) {
        String value = null;
        value = istream.read_string();
        return value;
    }

    public static void write(org.omg.CORBA.portable.OutputStream ostream, String value) {
        ostream.write_string(value);
    }

}
