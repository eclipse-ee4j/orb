/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates.
 * Copyright (c) 1997-1999 IBM Corp. All rights reserved.
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

package com.sun.tools.corba.ee.idl.toJavaPortable;

import com.sun.tools.corba.ee.idl.PrimitiveEntry;
import com.sun.tools.corba.ee.idl.SymtabEntry;

// NOTES:

import java.io.PrintWriter;
import java.util.Hashtable;

/**
 *
 **/
public class PrimitiveGen implements com.sun.tools.corba.ee.idl.PrimitiveGen, JavaGenerator
{
  /**
   * Public zero-argument constructor.
   **/
  public PrimitiveGen ()
  {
  } // ctor

  /**
   * This method should never be called; this class exists for
   * the JavaGenerator interface.
   **/
  public void generate (Hashtable symbolTable, PrimitiveEntry e, PrintWriter stream)
  {
  } // generate

  ///////////////
  // From JavaGenerator

  public int helperType (int index, String indent, com.sun.tools.corba.ee.idl.toJavaPortable.TCOffsets tcoffsets, String name, SymtabEntry entry, PrintWriter stream)
  {
    return type (index, indent, tcoffsets, name, entry, stream);
  } // helperType

  public int type (int index, String indent, com.sun.tools.corba.ee.idl.toJavaPortable.TCOffsets tcoffsets, String name, SymtabEntry entry, PrintWriter stream) {
    tcoffsets.set (entry);
    String emit = "tk_null";
    if (entry.name ().equals ("null"))
      emit = "tk_null";
    else if (entry.name ().equals ("void"))
      emit = "tk_void";
    else if (entry.name ().equals ("short"))
      emit = "tk_short";
    else if (entry.name ().equals ("long"))
      emit = "tk_long";
    else if (entry.name ().equals ("long long"))
      emit = "tk_longlong";
    else if (entry.name ().equals ("unsigned short"))
      emit = "tk_ushort";
    else if (entry.name ().equals ("unsigned long"))
      emit = "tk_ulong";
    else if (entry.name ().equals ("unsigned long long"))
      emit = "tk_ulonglong";
    else if (entry.name ().equals ("float"))
      emit = "tk_float";
    else if (entry.name ().equals ("double"))
      emit = "tk_double";
    else if (entry.name ().equals ("boolean"))
      emit = "tk_boolean";
    else if (entry.name ().equals ("char"))
      emit = "tk_char";
    else if (entry.name ().equals ("octet"))
      emit = "tk_octet";
    else if (entry.name ().equals ("any"))
      emit = "tk_any";
    else if (entry.name ().equals ("TypeCode"))
      emit = "tk_TypeCode";
    else if (entry.name ().equals ("wchar"))
      emit = "tk_wchar";
    else if (entry.name ().equals ("Principal")) // <d61961>
      emit = "tk_Principal";
    else if (entry.name ().equals ("wchar"))
      emit = "tk_wchar";
    stream.println (indent + name + " = org.omg.CORBA.ORB.init ().get_primitive_tc (org.omg.CORBA.TCKind." + emit + ");");
    return index;
  } // type

  public void helperRead (String entryName, SymtabEntry entry, PrintWriter stream)
  {
  } // helperRead

  public void helperWrite (SymtabEntry entry, PrintWriter stream)
  {
  } // helperWrite

  public int read (int index, String indent, String name, SymtabEntry entry, PrintWriter stream)
  {
    stream.println (indent + name + " = " + "istream.read_" + com.sun.tools.corba.ee.idl.toJavaPortable.Util.collapseName(entry.name()) + " ();");
    return index;
  } // read

  public int write (int index, String indent, String name, SymtabEntry entry, PrintWriter stream)
  {
    stream.println (indent + "ostream.write_" + com.sun.tools.corba.ee.idl.toJavaPortable.Util.collapseName(entry.name()) + " (" + name + ");");
    return index;
  } // write

  // From JavaGenerator
  ///////////////
} // class PrimitiveGen
