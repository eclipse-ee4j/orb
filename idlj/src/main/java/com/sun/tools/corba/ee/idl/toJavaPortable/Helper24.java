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
// -D62023   <klr> New file to implement CORBA 2.4 RTF

import com.sun.tools.corba.ee.idl.MethodEntry;
import com.sun.tools.corba.ee.idl.ValueBoxEntry;
import com.sun.tools.corba.ee.idl.ValueEntry;

import java.util.Vector;

/**
 *
 **/
public class Helper24 extends Helper
{
  /**
   * Public zero-argument constructor.
   **/
  public Helper24 ()
  {
  } // ctor

  /**
   * Generate the heading, including package, imports, class statements,
   * and open curly.
   **/
  protected void writeHeading ()
  {
    com.sun.tools.corba.ee.idl.toJavaPortable.Util.writePackage (stream, entry, com.sun.tools.corba.ee.idl.toJavaPortable.Util.HelperFile);
    com.sun.tools.corba.ee.idl.toJavaPortable.Util.writeProlog(stream, stream.name());

    // Transfer comment to target <30jul1997daz>.
    if (entry.comment () != null)
      entry.comment ().generate ("", stream);

    if (entry instanceof ValueBoxEntry) {
        stream.print   ("public final class " + helperClass);
        stream.println (" implements org.omg.CORBA.portable.BoxedValueHelper");
    }
    else
        stream.println ("abstract public class " + helperClass);
    stream.println ('{');
  }

  /**
   * Generate the instance variables.
   **/
  protected void writeInstVars ()
  {
    stream.println ("  private static String  _id = \"" + com.sun.tools.corba.ee.idl.toJavaPortable.Util.stripLeadingUnderscoresFromID(entry.repositoryID().ID()) + "\";");
    if (entry instanceof ValueEntry)
    {
      stream.println ();
      if (entry instanceof ValueBoxEntry) {
          stream.println ("  private static " + helperClass + " _instance = new " + helperClass + " ();");
          stream.println ();
      }
    }
    stream.println ();
  } // writeInstVars

  protected void writeValueHelperInterface ()
  {
    if (entry instanceof ValueBoxEntry) {
        writeGetID ();
    } else if (entry instanceof ValueEntry) {
        writeHelperFactories ();
    }
  } // writeValueHelperInterface

  /**
   *
   **/
  protected void writeHelperFactories ()
  {
    Vector init = ((ValueEntry)entry).initializers ();
    if (init != null)
    {
      stream.println ();
      for (int i = 0; i < init.size (); i++)
      {
        MethodEntry element = (MethodEntry) init.elementAt (i);
        element.valueMethod (true); //tag value method if not tagged previously
        ((com.sun.tools.corba.ee.idl.toJavaPortable.MethodGen24) element.generator ()). helperFactoryMethod (symbolTable, element, entry, stream);
      }
    }
  } // writeHelperFactories

  protected void writeCtors ()
  {
    if (entry instanceof ValueBoxEntry) {
        stream.println ("  public " + helperClass + "()");
        stream.println ("  {");
        stream.println ("  }");
        stream.println ();
    }
  } // writeCtors
}
