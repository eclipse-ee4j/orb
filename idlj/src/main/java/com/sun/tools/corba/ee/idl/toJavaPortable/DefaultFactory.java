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

// NOTES:
// -D62023 klr new class

import java.util.Vector;

import com.sun.tools.corba.ee.idl.GenFileStream;
import com.sun.tools.corba.ee.idl.SymtabEntry;
import com.sun.tools.corba.ee.idl.MethodEntry;
import com.sun.tools.corba.ee.idl.ValueEntry;

/**
 *
 **/
public class DefaultFactory implements AuxGen
{
  /**
   * Public zero-argument constructor.
   **/
  public DefaultFactory ()
  {
  } // ctor

  /**
   * Generate the default value factory class. Provides general algorithm for
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
  @Override
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
    factoryClass = entry.name () + "DefaultFactory";
    factoryInterface = entry.name () + "ValueFactory";
    factoryType = com.sun.tools.corba.ee.idl.toJavaPortable.Util.javaName(entry);
    implType = entry.name () + "Impl"; // default implementation class
  } // init

  /**
   * @return true if entry has any factory methods declared
   **/
    protected boolean hasFactoryMethods() {
        Vector<MethodEntry> init = ((ValueEntry) entry).initializers();
        return init != null && init.size() > 0;
    }

  /**
   * Open the print stream for subsequent output.
   **/
  protected void openStream ()
  {
    stream = com.sun.tools.corba.ee.idl.toJavaPortable.Util.stream(entry, "DefaultFactory.java");
  } // openStream

  /**
   * Generate the heading, including the package, imports,
   * source comment, class statement, and left curly.
   **/
  protected void writeHeading ()
  {
    com.sun.tools.corba.ee.idl.toJavaPortable.Util.writePackage (stream, entry, com.sun.tools.corba.ee.idl.toJavaPortable.Util.TypeFile); // REVISIT - same as interface?
    com.sun.tools.corba.ee.idl.toJavaPortable.Util.writeProlog(stream, stream.name());
    if (entry.comment () != null)
      entry.comment ().generate ("", stream);
    stream.print ("public class " + factoryClass + " implements ");
    if (hasFactoryMethods ())
        stream.print (factoryInterface);
    else
        stream.print ("org.omg.CORBA.portable.ValueFactory");
    stream.println (" {");
  } // writeHeading

  /**
   * Generate the contents of this class
   **/
  protected void writeBody ()
  {
    writeFactoryMethods ();
    stream.println ();
    writeReadValue ();
  } // writeBody

  /**
   * Generate members of this class.
   **/
  protected void writeFactoryMethods ()
  {
    Vector<MethodEntry> init = ((ValueEntry)entry).initializers ();
    if (init != null)
    {
      for (int i = 0; i < init.size (); i++)
      {
        MethodEntry element = init.elementAt(i);
        element.valueMethod (true); //tag value method if not tagged previously
        ((com.sun.tools.corba.ee.idl.toJavaPortable.MethodGen24) element.generator()).defaultFactoryMethod(symbolTable, element, stream);
      }
    }
  } // writeFactoryMethods

  /**
   * Generate default read_value
   **/
  protected void writeReadValue ()
  {
     stream.println ("  public java.io.Serializable read_value (org.omg.CORBA_2_3.portable.InputStream is)");
     stream.println ("  {");
     stream.println ("    return is.read_value(new " + implType + " ());");
     stream.println ("  }");
  } // writeReadValue

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

  protected java.util.Hashtable     symbolTable;
  protected SymtabEntry entry;
  protected GenFileStream           stream;

  // Unique to this generator
  protected String factoryClass;
  protected String factoryInterface;
  protected String factoryType;
  protected String implType;
} // class Holder
