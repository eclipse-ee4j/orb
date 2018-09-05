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

import com.sun.tools.corba.ee.idl.GenFileStream;
import com.sun.tools.corba.ee.idl.SymtabEntry;
import com.sun.tools.corba.ee.idl.ValueBoxEntry;
import com.sun.tools.corba.ee.idl.InterfaceState;
import com.sun.tools.corba.ee.idl.TypedefEntry;
import com.sun.tools.corba.ee.idl.StringEntry;
import com.sun.tools.corba.ee.idl.PrimitiveEntry;

/**
 *
 **/
public class Holder implements AuxGen
{
  /**
   * Public zero-argument constructor.
   **/
  public Holder ()
  {
  } // ctor

  /**
   * Generate the holder class. Provides general algorithm for
   * auxiliary binding generation:
   * 1.) Initialize symbol table and symbol table entry members,
   *     common to all generators.
   * 2.) Initialize members unique to this generator.
   * 3.) Open print stream
   * 4.) Write class heading (package, prologue, source comment, class
   *     statement, open curly
   * 5.) Write class body (member data and methods)
   * 6.) Write class closing (close curly)
   * 7.) Close the print stream
   **/
  public void generate (java.util.Hashtable symbolTable, SymtabEntry entry)
  {
    this.symbolTable = symbolTable;
    this.entry       = entry;
    init ();
    
    openStream ();
    if (stream == null)
      return;
    writeHeading ();
    writeBody ();
    writeClosing ();
    closeStream ();
  } // generate

  /**
   * Initialize variables unique to this generator.
   **/
  protected void init ()
  {
    holderClass = entry.name () + "Holder";
    helperClass = com.sun.tools.corba.ee.idl.toJavaPortable.Util.helperName(entry, true); // <d61056>
    if (entry instanceof ValueBoxEntry)
    {
      ValueBoxEntry v = (ValueBoxEntry) entry;
      TypedefEntry member = ((InterfaceState) v.state ().elementAt (0)).entry;
      SymtabEntry mType =  member.type ();
      holderType = com.sun.tools.corba.ee.idl.toJavaPortable.Util.javaName(mType);
    }
    else
      holderType = com.sun.tools.corba.ee.idl.toJavaPortable.Util.javaName(entry);
  } // init

  /**
   * Open the print stream for subsequent output.
   **/
  protected void openStream ()
  {
    stream = com.sun.tools.corba.ee.idl.toJavaPortable.Util.stream(entry, "Holder.java");
  } // openStream

  /**
   * Generate the heading, including the package, imports,
   * source comment, class statement, and left curly.
   **/
  protected void writeHeading ()
  {
    com.sun.tools.corba.ee.idl.toJavaPortable.Util.writePackage (stream, entry, com.sun.tools.corba.ee.idl.toJavaPortable.Util.HolderFile);
    com.sun.tools.corba.ee.idl.toJavaPortable.Util.writeProlog(stream, stream.name());
    if (entry.comment () != null)
      entry.comment ().generate ("", stream);
    stream.println ("public final class " + holderClass + " implements org.omg.CORBA.portable.Streamable");
    stream.println ('{');
  } // writeHeading

  /**
   * Generate members of this class.
   **/
  protected void writeBody ()
  {
    if (entry instanceof ValueBoxEntry)
      stream.println ("  public " + holderType + " value;");
    else
      com.sun.tools.corba.ee.idl.toJavaPortable.Util.writeInitializer("  public ", "value", "", entry, stream);
    stream.println ();
    writeCtors ();
    writeRead ();
    writeWrite ();
    writeType ();
  } // writeBody

  /**
   * Generate the closing statements.
   **/
  protected void writeClosing ()
  {
    stream.println ('}');
  } // writeClosing

  /**
   * Write the stream to file by closing the print stream.
   **/
  protected void closeStream ()
  {
    stream.close ();
  } // closeStream

  /**
   * Generate the constructors.
   **/
  protected void writeCtors ()
  {
    stream.println ("  public " + holderClass + " ()");
    stream.println ("  {");
    stream.println ("  }");
    stream.println ();
    stream.println ("  public " + holderClass + " (" + holderType + " initialValue)");
    stream.println ("  {");
    stream.println ("    value = initialValue;");
    stream.println ("  }");
    stream.println ();
  } // writeCtors

  /**
   * Generate the _read method.
   **/
  protected void writeRead ()
  {
    stream.println ("  public void _read (org.omg.CORBA.portable.InputStream i)");
    stream.println ("  {");
    if (entry instanceof ValueBoxEntry)
    {
      TypedefEntry member = ((InterfaceState) ((ValueBoxEntry) entry).state ().elementAt (0)).entry;
      SymtabEntry mType = member.type ();
      if (mType instanceof StringEntry)
        stream.println ("    value = i.read_string ();");

      else if (mType instanceof PrimitiveEntry)
        stream.println ("    value = " + helperClass + ".read (i).value;");

      else
        stream.println ("    value = " + helperClass + ".read (i);");
    }
    else
      stream.println ("    value = " + helperClass + ".read (i);");
    stream.println ("  }");
    stream.println ();
  } // writeRead

  /**
   * Generate the _write method.
   **/
  protected void writeWrite ()
  {
    stream.println ("  public void _write (org.omg.CORBA.portable.OutputStream o)");
    stream.println ("  {");
    if (entry instanceof ValueBoxEntry)
    {
      TypedefEntry member = ((InterfaceState) ((ValueBoxEntry) entry).state ().elementAt (0)).entry;
      SymtabEntry mType = member.type ();
      if (mType instanceof StringEntry)
        stream.println ("    o.write_string (value);");

      else if (mType instanceof PrimitiveEntry)
      {
        String name = entry.name ();
        stream.println ("    " + name + " vb = new " + name + " (value);");
        stream.println ("    " + helperClass + ".write (o, vb);");
      }

      else
        stream.println ("    " + helperClass + ".write (o, value);");
    }
    else
      stream.println ("    " + helperClass + ".write (o, value);");
    stream.println ("  }");
    stream.println ();
  } // writeWrite

  /**
   * Generate the _type method.
   **/
  protected void writeType ()
  {
    stream.println ("  public org.omg.CORBA.TypeCode _type ()");
    stream.println ("  {");
    stream.println ("    return " + helperClass + ".type ();");
    stream.println ("  }");
    stream.println ();
  } // writeType

  protected java.util.Hashtable     symbolTable;
  protected SymtabEntry entry;
  protected GenFileStream           stream;

  // Unique to this generator
  protected String holderClass;
  protected String helperClass;
  protected String holderType;
} // class Holder
