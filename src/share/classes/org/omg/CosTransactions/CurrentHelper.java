/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.omg.CosTransactions;

public class CurrentHelper {
    // It is useless to have instances of this class
    private CurrentHelper() { }

    public static void write(org.omg.CORBA.portable.OutputStream out, org.omg.CosTransactions.Current that) {
        throw new org.omg.CORBA.MARSHAL("Current cannot be marshaled");
    }
    public static org.omg.CosTransactions.Current read(org.omg.CORBA.portable.InputStream in) {
        throw new org.omg.CORBA.MARSHAL("Current cannot be unmarshaled");
    }
    public static org.omg.CosTransactions.Current extract(org.omg.CORBA.Any a) {
        org.omg.CORBA.portable.InputStream in = a.create_input_stream();
        return read(in);
    }
    public static void insert(org.omg.CORBA.Any a, org.omg.CosTransactions.Current that) {
        org.omg.CORBA.portable.OutputStream out = a.create_output_stream();
        write(out, that);
        a.read_value(out.create_input_stream(), type());
    }
    private static org.omg.CORBA.TypeCode _tc;
    synchronized public static org.omg.CORBA.TypeCode type() {
        if (_tc == null)
            _tc = org.omg.CORBA.ORB.init().create_interface_tc(id(), "Current");
        return _tc;
    }
    public static String id() {
        return "IDL:omg.org/CosTransactions/Current:1.0";
    }
    public static org.omg.CosTransactions.Current narrow(org.omg.CORBA.Object that)
        throws org.omg.CORBA.BAD_PARAM {
        if (that == null)
            return null;
        if (that instanceof org.omg.CosTransactions.Current)
            return (org.omg.CosTransactions.Current) that;
        else
            throw new org.omg.CORBA.BAD_PARAM();
    }
}
