/*
 * Copyright (c) 1997, 2020, Oracle and/or its affiliates.
 * Copyright (c) 1997-1999 IBM Corp. All rights reserved.
 * Copyright (c) 2019 Payara Services Ltd.
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
// -D62794   <klr> Fix problem with no-arg create functions

import org.glassfish.corba.idl.ExceptionEntry;
import org.glassfish.corba.idl.MethodEntry;
import org.glassfish.corba.idl.ParameterEntry;
import org.glassfish.corba.idl.SymtabEntry;

import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 *
 **/
public class MethodGen24 extends MethodGen
{
  /**
   * Public zero-argument constructor.
   **/
  public MethodGen24 ()
  {
  } // ctor

  /**
   * Print the parameter list for the factory method.
   * @param m The method to list parameters for
   * @param listTypes If try, declare the parms, otherwise just list them
   * @param stream The PrintWriter to print on
   */
  protected void writeParmList (MethodEntry m, boolean listTypes, PrintWriter stream) {
    boolean firstTime = true;
    Enumeration<ParameterEntry> e = m.parameters().elements ();
    while (e.hasMoreElements()) {
        if (firstTime) {
            firstTime = false;
        } else {
            stream.print(", ");
        }
        ParameterEntry parm = e.nextElement();
        if (listTypes) {
            writeParmType(parm.type(), parm.passType());
            stream.print(' ');
        }
      // Print parm name
      stream.print (parm.name ());
      // end of parameter list
    }
  }

  void helperFactoryMethod(Hashtable symbolTable, MethodEntry m, SymtabEntry t, PrintWriter stream)
  {
    this.symbolTable = symbolTable;
    this.m = m;
    this.stream = stream;
    String initializerName = m.name ();
    String typeName = org.glassfish.corba.idl.toJavaPortable.Util.javaName(t);
    String factoryName = typeName + "ValueFactory";

    // Step 1. Print factory method decl up to parms.
    stream.print  ("  public static " + typeName + " " + initializerName +
            " (org.omg.CORBA.ORB $orb");
    if (!m.parameters ().isEmpty ())
      stream.print (", "); // <d62794>

    // Step 2. Print the declaration parameter list.
    writeParmList (m, true, stream);

    // Step 3. Print the body of the factory method
    stream.println (")");
    stream.println ("  {");
    stream.println ("    try {");
    stream.println ("      " + factoryName + " $factory = (" + factoryName + ")");
    stream.println ("          ((org.omg.CORBA_2_3.ORB) $orb).lookup_value_factory(id());");
    stream.print   ("      return $factory." + initializerName + " (");
    writeParmList (m, false, stream);
    stream.println (");");
    stream.println ("    } catch (ClassCastException $ex) {");
    stream.println ("      throw new org.omg.CORBA.BAD_PARAM ();");
    stream.println ("    }");
    stream.println ("  }");
    stream.println ();
  } // helperFactoryMethod

  void abstractMethod(Hashtable symbolTable, MethodEntry m, PrintWriter stream)
  {
    this.symbolTable = symbolTable;
    this.m           = m;
    this.stream      = stream;
    if (m.comment () != null)
      m.comment ().generate ("  ", stream);
    stream.print ("  ");
    stream.print ("public abstract ");
    writeMethodSignature ();
    stream.println (";");
    stream.println ();
  } // abstractMethod

  void defaultFactoryMethod(Hashtable symbolTable, MethodEntry m, PrintWriter stream)
  {
    this.symbolTable = symbolTable;
    this.m           = m;
    this.stream      = stream;
    String typeName = m.container().name();
    stream.println ();
    if (m.comment () != null)
      m.comment ().generate ("  ", stream);
    stream.print   ("  public " + typeName + " " + m.name() + " (");
    writeParmList  (m, true, stream);
    stream.println (")");
    stream.println ("  {");
    stream.print   ("    return new " + typeName + "Impl (");
    writeParmList (m, false, stream);
    stream.println (");");
    stream.println ("  }");
  } // defaultFactoryMethod

  protected void writeMethodSignature ()
  {
    // Step 0.  Print the return type and name.
    // A return type of null indicates the "void" return type. If m is a
    // Valuetype factory method, it has a null return type,
    if (m.type () == null)
    {
        // if factory method, result type is container 
        if (isValueInitializer ())
            stream.print (m.container ().name ());
        else
            stream.print ("void");
    }
    else
    {
      stream.print (org.glassfish.corba.idl.toJavaPortable.Util.javaName(m.type()));
    }
    stream.print (' ' + m.name () + " (");

    // Step 1.  Print the parameter list.
    boolean firstTime = true;
    Enumeration<ParameterEntry> params = m.parameters().elements();
    while (params.hasMoreElements()) {
        if (firstTime) {
            firstTime = false;
        } else {
            stream.print(", ");
        }
        ParameterEntry parm = params.nextElement();

      writeParmType (parm.type (), parm.passType ());

      // Print parm name
      stream.print (' ' + parm.name ());
    }

    // Step 2.  Add the context parameter if necessary.
    if (m.contexts().size() > 0)
    {
      if (!firstTime)
        stream.print (", ");
      stream.print ("org.omg.CORBA.Context $context");
    }

    // Step 3.  Print the throws clause (if necessary).
    if (m.exceptions().size() > 0)
    {
      stream.print (") throws ");
      Enumeration<ExceptionEntry> exceptions = m.exceptions().elements();
      firstTime = true;
      while (exceptions.hasMoreElements ())
      {
        if (firstTime)
          firstTime = false;
        else
          stream.print (", ");
        stream.print (org.glassfish.corba.idl.toJavaPortable.Util.javaName((SymtabEntry) exceptions.nextElement()));
      }
    }
    else
      stream.print (')');
  } // writeMethodSignature

  @Override
  protected void interfaceMethod (Hashtable symbolTable, MethodEntry m, PrintWriter stream)
  {
    this.symbolTable = symbolTable;
    this.m           = m;
    this.stream      = stream;
    if (m.comment () != null)
      m.comment ().generate ("  ", stream);
    stream.print ("  ");
    writeMethodSignature ();
    stream.println (";");
  } // interfaceMethod
}
