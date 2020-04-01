/*
 * Copyright (c) 1997, 2020, Oracle and/or its affiliates.
 * Copyright (c) 1997-1999 IBM Corp. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.corba.idl.toJavaPortable;

// NOTES:

import java.io.PrintWriter;
import java.util.Hashtable;

import org.glassfish.corba.idl.GenFileStream;
import org.glassfish.corba.idl.ConstEntry;
import org.glassfish.corba.idl.ModuleEntry;
import org.glassfish.corba.idl.PrimitiveEntry;
import org.glassfish.corba.idl.StringEntry;
import org.glassfish.corba.idl.SymtabEntry;
import org.glassfish.corba.idl.TypedefEntry;

/**
 *
 **/
public class ConstGen implements org.glassfish.corba.idl.ConstGen
{
  /**
   * Public zero-argument constructor.
   **/
  public ConstGen ()
  {
  } // ctor

  /**
   * Generate Java code for an IDL constant.  A constant is written to
   * a new class only when it is not a member of an interface; otherwise
   * it written to the interface class in which it resides.
   **/
  public void generate (Hashtable symbolTable, ConstEntry c, PrintWriter s)
  {
    this.symbolTable = symbolTable;
    this.c           = c;
    this.stream      = s;
    init ();
    
    if (c.container () instanceof ModuleEntry)
      generateConst ();
    else if (stream != null)
      writeConstExpr ();
  } // generate

  /**
   * Initialize members unique to this generator.
   **/
  protected void init ()
  {
  } // init

  /**
   * Generate the class defining the constant.
   **/
  protected void generateConst ()
  {
    openStream ();
    if (stream == null)
      return;
    writeHeading ();
    writeBody ();
    writeClosing ();
    closeStream ();
  } // generateConst

  /**
   * Open a new print stream only if the constant is not a member
   * of an interface.
   **/
  protected void openStream ()
  {
    stream = Util.stream(c, ".java");
  } // openStream

  /**
   * Write the heading for the class defining the constant.
   **/
  protected void writeHeading ()
  {
    Util.writePackage(stream, c);
    Util.writeProlog(stream, ((GenFileStream) stream).name());
    stream.println ("public interface " + c.name ()); 
        // should not be done according to the mapping
        // + " extends org.omg.CORBA.portable.IDLEntity");
    stream.println ("{");
  } // writeHeading

  /**
   * Write the constant expression and any comment, if present.
   **/
  protected void writeBody ()
  {
    writeConstExpr ();
  } // writeBody

  /**
   * Write the entire constant expression and any comment, if present.
   **/
  protected void writeConstExpr ()
  {
    if (c.comment () != null)
      c.comment ().generate ("  ", stream);
    if (c.container () instanceof ModuleEntry) {
        
      stream.print ("  public static final " + Util.javaName(c.type()) + " value = ");
    } else {
      stream.print ("  public static final " + Util.javaName(c.type()) + ' ' + c.name () + " = ");
    }
    writeConstValue (c.type ());
  } // writeConstExpr

  /**
   * Write the constant's value according to its type.
   **/
  private void writeConstValue (SymtabEntry type)
  {
    if (type instanceof PrimitiveEntry)
      stream.println ('(' + Util.javaName(type) + ")(" + Util.parseExpression(c.value()) + ");");
    else if (type instanceof StringEntry)
      stream.println (Util.parseExpression(c.value()) + ';');
    else if (type instanceof TypedefEntry)
    {
      while (type instanceof TypedefEntry)
        type = type.type ();
      writeConstValue (type);
    }
    else
      stream.println (Util.parseExpression(c.value()) + ';');
  } // writeValue

  /**
   * Generate any last words and close the class.
   **/
  protected void writeClosing ()
  {
    stream.println ("}");
  } // writeClosing

  /**
   * Close the print stream, causing the file to be written.
   **/
  protected void closeStream ()
  {
    stream.close ();
  } // closeStream

  protected java.util.Hashtable  symbolTable = null;
  protected ConstEntry           c           = null;
  protected PrintWriter          stream      = null;
} // class ConstGen


/*=======================================================================================
  DATE-AUTHOR   ACTION
  ---------------------------------------------------------------------------------------
  11sep1997daz  Return when print stream is null and container is NOT a module. Fixes
                -keep option, which causes null print stream to be sent to ConstGen.
  31jul1997daz  Write source comment immediately preceding constant declaration.
  =======================================================================================*/

