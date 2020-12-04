/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates.
 * Copyright (c) 1998-1999 IBM Corp. All rights reserved.
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

 /**
 * The Helper for <tt>ValueBase</tt>.  For more information on 
 * Helper files, see <a href="doc-files/generatedfiles.html#helper">
 * "Generated Files: Helper Files"</a>.<P>
 */
 
package org.omg.CORBA;

abstract public class ValueBaseHelper
{
    private static String  _id = "IDL:omg.org/CORBA/ValueBase:1.0";

    public static void insert (org.omg.CORBA.Any a, java.io.Serializable that)
    {
        org.omg.CORBA.portable.OutputStream out = a.create_output_stream ();
        a.type (type ());
        write (out, that);
        a.read_value (out.create_input_stream (), type ());
    }

    public static java.io.Serializable extract (org.omg.CORBA.Any a)
    {
        return read (a.create_input_stream ());
    }

    private static org.omg.CORBA.TypeCode __typeCode = null;
    synchronized public static org.omg.CORBA.TypeCode type ()
    {
        if (__typeCode == null)
            {
                __typeCode = org.omg.CORBA.ORB.init ().get_primitive_tc (TCKind.tk_value);
            }
        return __typeCode;
    }

    public static String id ()
    {
        return _id;
    }

    public static java.io.Serializable read (org.omg.CORBA.portable.InputStream istream)
    {
        return ((org.omg.CORBA_2_3.portable.InputStream)istream).read_value ();
    }

    public static void write (org.omg.CORBA.portable.OutputStream ostream, java.io.Serializable value)
    {
        ((org.omg.CORBA_2_3.portable.OutputStream)ostream).write_value (value);
    }


}
