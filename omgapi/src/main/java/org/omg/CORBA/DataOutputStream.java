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

/** Defines the methods used to write primitive data types to output streams
* for marshalling custom value types.  This interface is used by user
* written custom marshalling code for custom value types.
* @see org.omg.CORBA.DataInputStream
* @see org.omg.CORBA.CustomMarshal
* @version 1.15 07/27/07
*/
public interface DataOutputStream extends org.omg.CORBA.portable.ValueBase
{
    /** 
    * Writes the Any value to the output stream.
    * @param value The value to be written.
    */
    void write_any (org.omg.CORBA.Any value);

    /** 
    * Writes the boolean value to the output stream.
    * @param value The value to be written.
    */
    void write_boolean (boolean value);

    /** 
    * Writes the IDL character value to the output stream.
    * @param value The value to be written.
    */
    void write_char (char value);

    /** 
    * Writes the IDL wide character value to the output stream.
    * @param value The value to be written.
    */
    void write_wchar (char value);

    /** 
    * Writes the IDL octet value (represented as a Java byte) to the output stream.
    * @param value The value to be written.
    */
    void write_octet (byte value);

    /** 
    * Writes the IDL short value to the output stream.
    * @param value The value to be written.
    */
    void write_short (short value);

    /** 
    * Writes the IDL unsigned short value (represented as a Java short 
    * value) to the output stream.
    * @param value The value to be written.
    */
    void write_ushort (short value);

    /** 
    * Writes the IDL long value (represented as a Java int) to the output stream.
    * @param value The value to be written.
    */
    void write_long (int value);

    /** 
    * Writes the IDL unsigned long value (represented as a Java int) to the output stream.
    * @param value The value to be written.
    */
    void write_ulong (int value);

    /** 
    * Writes the IDL long long value (represented as a Java long) to the output stream.
    * @param value The value to be written.
    */
    void write_longlong (long value);

    /** 
    * Writes the IDL unsigned long long value (represented as a Java long)
    * to the output stream.
    * @param value The value to be written.
    */
    void write_ulonglong (long value);

    /** 
    * Writes the IDL float value to the output stream.
    * @param value The value to be written.
    */
    void write_float (float value);

    /** 
    * Writes the IDL double value to the output stream.
    * @param value The value to be written.
    */
    void write_double (double value);

    // write_longdouble not supported by IDL/Java mapping

    /** 
    * Writes the IDL string value to the output stream.
    * @param value The value to be written.
    */
    void write_string (String value);

    /** 
    * Writes the IDL wide string value (represented as a Java String) to the output stream.
    * @param value The value to be written.
    */
    void write_wstring (String value);

    /** 
    * Writes the IDL CORBA::Object value to the output stream.
    * @param value The value to be written.
    */
    void write_Object (org.omg.CORBA.Object value);

    /** 
    * Writes the IDL Abstract interface type to the output stream.
    * @param value The value to be written.
    */
    void write_Abstract (java.lang.Object value);

    /** 
    * Writes the IDL value type value to the output stream.
    * @param value The value to be written.
    */
    void write_Value (java.io.Serializable value);

    /** 
    * Writes the typecode to the output stream.
    * @param value The value to be written.
    */
    void write_TypeCode (org.omg.CORBA.TypeCode value);
    
    /** 
    * Writes the array of IDL Anys from offset for length elements to the
    * output stream.  
    * @param seq The array to be written.
    * @param offset The index into seq of the first element to write to the
    * output stream.
    * @param length The number of elements to write to the output stream.
    */
    void write_any_array (org.omg.CORBA.Any[] seq, int offset, int length);

    /** 
    * Writes the array of IDL booleans from offset for length elements to the
    * output stream.  
    * @param seq The array to be written.
    * @param offset The index into seq of the first element to write to the
    * output stream.
    * @param length The number of elements to write to the output stream.
    */
    void write_boolean_array (boolean[] seq, int offset, int length);

    /** 
    * Writes the array of IDL characters from offset for length elements to the
    * output stream.  
    * @param seq The array to be written.
    * @param offset The index into seq of the first element to write to the
    * output stream.
    * @param length The number of elements to write to the output stream.
    */
    void write_char_array (char[] seq, int offset, int length);

    /** 
    * Writes the array of IDL wide characters from offset for length elements to the
    * output stream.  
    * @param seq The array to be written.
    * @param offset The index into seq of the first element to write to the
    * output stream.
    * @param length The number of elements to write to the output stream.
    */
    void write_wchar_array (char[] seq, int offset, int length);

    /** 
    * Writes the array of IDL octets from offset for length elements to the
    * output stream.  
    * @param seq The array to be written.
    * @param offset The index into seq of the first element to write to the
    * output stream.
    * @param length The number of elements to write to the output stream.
    */
    void write_octet_array (byte[] seq, int offset, int length);

    /** 
    * Writes the array of IDL shorts from offset for length elements to the
    * output stream.  
    * @param seq The array to be written.
    * @param offset The index into seq of the first element to write to the
    * output stream.
    * @param length The number of elements to write to the output stream.
    */
    void write_short_array (short[] seq, int offset, int length);

    /** 
    * Writes the array of IDL unsigned shorts (represented as Java shorts)
    * from offset for length elements to the output stream.  
    * @param seq The array to be written.
    * @param offset The index into seq of the first element to write to the
    * output stream.
    * @param length The number of elements to write to the output stream.
    */
    void write_ushort_array (short[] seq, int offset, int length);

    /** 
    * Writes the array of IDL longs from offset for length elements to the
    * output stream.  
    * @param seq The array to be written.
    * @param offset The index into seq of the first element to write to the
    * output stream.
    * @param length The number of elements to write to the output stream.
    */
    void write_long_array (int[] seq, int offset, int length);

    /** 
    * Writes the array of IDL unsigned longs (represented as Java ints) 
    * from offset for length elements to the output stream.  
    * @param seq The array to be written.
    * @param offset The index into seq of the first element to write to the
    * output stream.
    * @param length The number of elements to write to the output stream.
    */
    void write_ulong_array (int[] seq, int offset, int length);

    /** 
    * Writes the array of IDL unsigned long longs (represented as Java longs)
    * from offset for length elements to the output stream.  
    * @param seq The array to be written.
    * @param offset The index into seq of the first element to write to the
    * output stream.
    * @param length The number of elements to write to the output stream.
    */
    void write_ulonglong_array (long[] seq, int offset, int length);

    /** 
    * Writes the array of IDL long longs from offset for length elements to the
    * output stream.  
    * @param seq The array to be written.
    * @param offset The index into seq of the first element to write to the
    * output stream.
    * @param length The number of elements to write to the output stream.
    */
    void write_longlong_array (long[] seq, int offset, int length);

    /** 
    * Writes the array of IDL floats from offset for length elements to the
    * output stream.  
    * @param seq The array to be written.
    * @param offset The index into seq of the first element to write to the
    * output stream.
    * @param length The number of elements to write to the output stream.
    */
    void write_float_array (float[] seq, int offset, int length);

    /** 
    * Writes the array of IDL doubles from offset for length elements to the
    * output stream.  
    * @param seq The array to be written.
    * @param offset The index into seq of the first element to write to the
    * output stream.
    * @param length The number of elements to write to the output stream.
    */
    void write_double_array (double[] seq, int offset, int length);
} // interface DataOutputStream
