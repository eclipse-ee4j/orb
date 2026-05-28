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

package com.sun.corba.ee.impl.encoding;

import java.io.IOException;

import org.omg.CORBA.Any;
import org.omg.CORBA.TypeCode;
import org.omg.CORBA.portable.InputStream;

public interface MarshalOutputStream {

    InputStream create_input_stream();

    void write_boolean(boolean value);
    void write_char(char value);
    void write_wchar(char value);
    void write_octet(byte value);
    void write_short(short value);
    void write_ushort(short value);
    void write_long(int value);
    void write_ulong(int value);
    void write_longlong(long value);
    void write_ulonglong(long value);
    void write_float(float value);
    void write_double(double value);
    void write_string(String value);
    void write_wstring(String value);

    void write_boolean_array(boolean[] value, int offset, int length);
    void write_char_array(char[] value, int offset, int length);
    void write_wchar_array(char[] value, int offset, int length);
    void write_octet_array(byte[] value, int offset, int length);
    void write_short_array(short[] value, int offset, int length);
    void write_ushort_array(short[] value, int offset, int length);
    void write_long_array(int[] value, int offset, int length);
    void write_ulong_array(int[] value, int offset, int length);
    void write_longlong_array(long[] value, int offset, int length);
    void write_ulonglong_array(long[] value, int offset, int length);
    void write_float_array(float[] value, int offset, int length);
    void write_double_array(double[] value, int offset, int length);

    void write_Object(org.omg.CORBA.Object value);
    void write_TypeCode(TypeCode value);
    void write_any(Any value);
    @SuppressWarnings("deprecation")
    void write_Principal(org.omg.CORBA.Principal value);


    /*
     * The methods necessary to support RMI
     */
    void write_value(java.io.Serializable value);
    void start_block();
    void end_block();

    /*
     * Additional Methods
     */

    void   putEndian();
    void   writeTo(java.io.OutputStream s)
        throws IOException;

    byte[] toByteArray();
}
