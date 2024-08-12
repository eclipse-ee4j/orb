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

package com.sun.corba.ee.impl.protocol.giopmsgheaders;

/**
 * com/sun/corba/ee/impl/protocol/giopmsgheaders/AddressingDispositionHelper.java Generated by the IDL-to-Java compiler
 * (portable), version "3.0" from ../../../../../../../src/share/classes/com/sun/corba/ee/GiopIDL/g.idl Sunday, June 4,
 * 2000 5:18:54 PM PDT
 */

abstract public class AddressingDispositionHelper {
    private static String _id = "IDL:messages/AddressingDisposition:1.0";

    public static void insert(org.omg.CORBA.Any a, short that) {
        org.omg.CORBA.portable.OutputStream out = a.create_output_stream();
        a.type(type());
        write(out, that);
        a.read_value(out.create_input_stream(), type());
    }

    public static short extract(org.omg.CORBA.Any a) {
        return read(a.create_input_stream());
    }

    private static org.omg.CORBA.TypeCode __typeCode = null;

    synchronized public static org.omg.CORBA.TypeCode type() {
        if (__typeCode == null) {
            __typeCode = org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_short);
            __typeCode = org.omg.CORBA.ORB.init().create_alias_tc(
                    com.sun.corba.ee.impl.protocol.giopmsgheaders.AddressingDispositionHelper.id(), "AddressingDisposition", __typeCode);
        }
        return __typeCode;
    }

    public static String id() {
        return _id;
    }

    public static short read(org.omg.CORBA.portable.InputStream istream) {
        short value = (short) 0;
        value = istream.read_short();
        return value;
    }

    public static void write(org.omg.CORBA.portable.OutputStream ostream, short value) {
        ostream.write_short(value);
    }

}
