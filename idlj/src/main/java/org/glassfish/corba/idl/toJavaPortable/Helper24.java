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
// -D62023   <klr> New file to implement CORBA 2.4 RTF

import org.glassfish.corba.idl.MethodEntry;
import org.glassfish.corba.idl.ValueBoxEntry;
import org.glassfish.corba.idl.ValueEntry;

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
    org.glassfish.corba.idl.toJavaPortable.Util.writePackage (stream, entry, org.glassfish.corba.idl.toJavaPortable.Util.HelperFile);
    org.glassfish.corba.idl.toJavaPortable.Util.writeProlog(stream, stream.name());

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
    stream.println ("  private static String  _id = \"" + org.glassfish.corba.idl.toJavaPortable.Util.stripLeadingUnderscoresFromID(entry.repositoryID().ID()) + "\";");
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
    Vector<MethodEntry> init = ((ValueEntry)entry).initializers();
    if (init != null)
    {
      stream.println ();
      for (int i = 0; i < init.size (); i++)
      {
        MethodEntry element = init.elementAt(i);
        element.valueMethod (true); //tag value method if not tagged previously
        ((org.glassfish.corba.idl.toJavaPortable.MethodGen24) element.generator ()). helperFactoryMethod (symbolTable, element, entry, stream);
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
