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

import org.omg.CORBA.Any;
import org.omg.CORBA.TypeCode;

public interface MarshalInputStream {
    boolean      read_boolean();
    char         read_char();
    char         read_wchar();
    byte         read_octet();
    short        read_short();
    short        read_ushort();
    int          read_long();
    int          read_ulong();
    long         read_longlong();
    long         read_ulonglong();
    float        read_float();
    double       read_double();
    String       read_string();
    String       read_wstring();

    void read_boolean_array(boolean[] value, int offset, int length);
    void read_char_array(char[] value, int offset, int length);
    void read_wchar_array(char[] value, int offset, int length);
    void read_octet_array(byte[] value, int offset, int length);
    void read_short_array(short[] value, int offset, int length);
    void read_ushort_array(short[] value, int offset, int length);
    void read_long_array(int[] value, int offset, int length);
    void read_ulong_array(int[] value, int offset, int length);
    void read_longlong_array(long[] value, int offset, int length);
    void read_ulonglong_array(long[] value, int offset, int length);
    void read_float_array(float[] value, int offset, int length);
    void read_double_array(double[] value, int offset, int length);

    org.omg.CORBA.Object read_Object();
    TypeCode     read_TypeCode();
    Any          read_any();
    @SuppressWarnings({"deprecation"}) org.omg.CORBA.Principal      read_Principal();

    /*
     * The methods necessary to support RMI
     */
    org.omg.CORBA.Object read_Object(Class stubClass);
    java.io.Serializable read_value() throws Exception;

    /*
     * Additional Methods
     */
    void consumeEndian();

    // Determines the current byte stream position
    // (also handles fragmented streams)
    int getPosition();

    // mark/reset from java.io.InputStream
    void mark(int readAheadLimit);
    void reset();

    /**
     * This must be called once before unmarshaling valuetypes or anything
     * that uses repository IDs.  The ORB's version should be set
     * to the desired value prior to calling.
     */
    void performORBVersionSpecificInit();

    /**
     * Tells the input stream to null any code set converter
     * references, forcing it to reacquire them if it needs
     * converters again.  This is used when the server
     * input stream needs to switch the connection's char code set
     * converter to something different after reading the
     * code set service context for the first time.  Initially,
     * we use ISO8859-1 to read the operation name (it can't
     * be more than ASCII).
     */
    void resetCodeSetConverters();
}
