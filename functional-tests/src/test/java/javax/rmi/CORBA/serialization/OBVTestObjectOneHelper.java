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

package javax.rmi.CORBA.serialization;

abstract public class OBVTestObjectOneHelper
{
  private static String  _id = "IDL:javax/rmi/CORBA/serialization/OBVTestObjectOne:1.0";

  public static void insert (org.omg.CORBA.Any a, OBVTestObjectOne that)
    {
    org.omg.CORBA.portable.OutputStream out = a.create_output_stream ();
    a.type (type ());
    write (out, that);
    a.read_value (out.create_input_stream (), type ());
    }

  public static OBVTestObjectOne extract (org.omg.CORBA.Any a)
    {
    return read (a.create_input_stream ());
    }

  private static org.omg.CORBA.TypeCode __typeCode = null;
  private static boolean __active = false;
  synchronized public static org.omg.CORBA.TypeCode type ()
    {
    if (__typeCode == null)
    {
      synchronized (org.omg.CORBA.TypeCode.class)
      {
        if (__typeCode == null)
        {
          if (__active)
          {
            return org.omg.CORBA.ORB.init().create_recursive_tc ( _id );
    }
          __active = true;
          org.omg.CORBA.ValueMember[] _members0 = new org.omg.CORBA.ValueMember[5];
          org.omg.CORBA.TypeCode _tcOf_members0 = null;
          // ValueMember instance for fInt
          _tcOf_members0 = org.omg.CORBA.ORB.init ().get_primitive_tc (org.omg.CORBA.TCKind.tk_long);
          _members0[0] = new org.omg.CORBA.ValueMember ("fInt", 
              "", 
              _id, 
              "", 
              _tcOf_members0, 
              null, 
              org.omg.CORBA.PRIVATE_MEMBER.value);
          // ValueMember instance for fLong
          _tcOf_members0 = org.omg.CORBA.ORB.init ().get_primitive_tc (org.omg.CORBA.TCKind.tk_longlong);
          _members0[1] = new org.omg.CORBA.ValueMember ("fLong", 
              "", 
              _id, 
              "", 
              _tcOf_members0, 
              null, 
              org.omg.CORBA.PRIVATE_MEMBER.value);
          // ValueMember instance for fFloat
          _tcOf_members0 = org.omg.CORBA.ORB.init ().get_primitive_tc (org.omg.CORBA.TCKind.tk_float);
          _members0[2] = new org.omg.CORBA.ValueMember ("fFloat", 
              "", 
              _id, 
              "", 
              _tcOf_members0, 
              null, 
              org.omg.CORBA.PRIVATE_MEMBER.value);
          // ValueMember instance for fDouble
          _tcOf_members0 = org.omg.CORBA.ORB.init ().get_primitive_tc (org.omg.CORBA.TCKind.tk_double);
          _members0[3] = new org.omg.CORBA.ValueMember ("fDouble", 
              "", 
              _id, 
              "", 
              _tcOf_members0, 
              null, 
              org.omg.CORBA.PRIVATE_MEMBER.value);
          // ValueMember instance for fString
          _tcOf_members0 = org.omg.CORBA.ORB.init ().create_string_tc (0);
          _members0[4] = new org.omg.CORBA.ValueMember ("fString", 
              "", 
              _id, 
              "", 
              _tcOf_members0, 
              null, 
              org.omg.CORBA.PRIVATE_MEMBER.value);
          __typeCode = org.omg.CORBA.ORB.init ().create_value_tc (_id, "OBVTestObjectOne", org.omg.CORBA.VM_NONE.value, null, _members0);
          __active = false;
        }
      }
    }
    return __typeCode;
  }
    
  public static String id ()
    {
    return _id;
    }

  public static OBVTestObjectOne read (org.omg.CORBA.portable.InputStream istream)
    {
    return (OBVTestObjectOne)((org.omg.CORBA_2_3.portable.InputStream) istream).read_value (id ());
    }

  public static void write (org.omg.CORBA.portable.OutputStream ostream, OBVTestObjectOne value)
    {
    ((org.omg.CORBA_2_3.portable.OutputStream) ostream).write_value (value, id ());
    }

}
