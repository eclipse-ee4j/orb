/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 1997-1999 IBM Corp. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.tools.corba.ee.idl.toJavaPortable;

// NOTES:
// -D61056   <klr> Use Util.helperName

import java.io.PrintWriter;
import java.util.Hashtable;

import com.sun.tools.corba.ee.idl.StringEntry;
import com.sun.tools.corba.ee.idl.SymtabEntry;

/**
 * Handles generation of CORBA strings as well as wstrings.  Be careful
 * not to forget the wstrings.
 **/
public class StringGen implements com.sun.tools.corba.ee.idl.StringGen, JavaGenerator
{
  /**
   * Public zero-argument constructor.
   **/
  public StringGen ()
  {
  } // ctor

  /**
   * This should never be called.  This class exists for the
   * JavaGenerator interface.
   **/
  public void generate (Hashtable symbolTable, StringEntry e, PrintWriter stream)
  {
  } // generate

  ///////////////
  // From JavaGenerator

  public int helperType (int index, String indent, com.sun.tools.corba.ee.idl.toJavaPortable.TCOffsets tcoffsets, String name, SymtabEntry entry, PrintWriter stream)
  {
    return type(index, indent, tcoffsets, name, entry, stream);
  } // helperType

  public int type (int index, String indent, com.sun.tools.corba.ee.idl.toJavaPortable.TCOffsets tcoffsets, String name, SymtabEntry entry, PrintWriter stream) {
    tcoffsets.set (entry);
    StringEntry stringEntry = (StringEntry)entry;
    String bound;
    if (stringEntry.maxSize () == null)
      bound = "0";
    else
      bound = com.sun.tools.corba.ee.idl.toJavaPortable.Util.parseExpression(stringEntry.maxSize());

    // entry.name() is necessary to determine whether it is a
    // string or wstring

    stream.println (indent 
                    + name 
                    + " = org.omg.CORBA.ORB.init ().create_"
                    + entry.name()
                    + "_tc ("
                    + bound + ");");
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
    StringEntry string = (StringEntry)entry;
    String entryName = entry.name ();
    if (entryName.equals ("string"))
      stream.println (indent + name + " = istream.read_string ();");
    else if (entryName.equals ("wstring"))
      stream.println (indent + name + " = istream.read_wstring ();");
    if (string.maxSize () != null)
    {
      stream.println (indent + "if (" + name + ".length () > (" + com.sun.tools.corba.ee.idl.toJavaPortable.Util.parseExpression(string.maxSize()) + "))");
      stream.println (indent + "  throw new org.omg.CORBA.MARSHAL (0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);");
    }
    return index;
  } // read

  public int write (int index, String indent, String name, SymtabEntry entry, PrintWriter stream)
  {
    StringEntry string = (StringEntry)entry;
    if (string.maxSize () != null)
    {
      stream.print (indent + "if (" + name + ".length () > (" + com.sun.tools.corba.ee.idl.toJavaPortable.Util.parseExpression(string.maxSize()) + "))");
      stream.println (indent + "  throw new org.omg.CORBA.MARSHAL (0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);");
    }
    String entryName = entry.name ();
    if (entryName.equals ("string"))
      stream.println (indent + "ostream.write_string (" + name + ");");
    else if (entryName.equals ("wstring"))
      stream.println (indent + "ostream.write_wstring (" + name + ");");
    return index;
  } // write

  // From JavaGenerator
  ///////////////
} // class StringGen
