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

import com.sun.tools.corba.ee.idl.AttributeEntry;
import com.sun.tools.corba.ee.idl.GenFileStream;
import com.sun.tools.corba.ee.idl.InterfaceEntry;
import com.sun.tools.corba.ee.idl.MethodEntry;
import com.sun.tools.corba.ee.idl.SymtabEntry;

import java.io.File;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 *
 **/
public class Stub implements AuxGen
{
  /**
   * Public zero-argument constructor.
   **/
  public Stub ()
  {
  } // ctor

  /**
   *
   **/
  public void generate (Hashtable symbolTable, SymtabEntry entry)
  {
    this.symbolTable = symbolTable;
    this.i           = (InterfaceEntry)entry;
    this.localStub   = i.isLocalServant();
    this.isAbstract  = i.isAbstract( );
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
   * Initialize unique members of this generator.
   **/
  protected void init ()
  {
    classSuffix = "Stub";
  } // init

  /**
   *
   **/
  protected void openStream ()
  {
    String name = '_' + i.name () + classSuffix;
    String pkg = com.sun.tools.corba.ee.idl.toJavaPortable.Util.containerFullName(i.container());
    if (pkg != null && !pkg.equals (""))
    {
      com.sun.tools.corba.ee.idl.toJavaPortable.Util.mkdir(pkg);
      name = pkg + '/' + name;
    }
    stream = com.sun.tools.corba.ee.idl.toJavaPortable.Util.getStream(name.replace('/', File.separatorChar) + ".java", i);
  } // openStream

  /**
   *
   **/
  protected void writeHeading ()
  {
    com.sun.tools.corba.ee.idl.toJavaPortable.Util.writePackage (stream, i, com.sun.tools.corba.ee.idl.toJavaPortable.Util.StubFile);
    com.sun.tools.corba.ee.idl.toJavaPortable.Util.writeProlog(stream, ((GenFileStream) stream).name());

    // Transfer interface comment to target <31jul1997>.
    if (i.comment () != null)
      i.comment ().generate ("", stream);

    writeClassDeclaration ();
    stream.println ('{');
  } // writeHeading

  /**
   *
   **/
  protected void writeClassDeclaration ()
  {
    stream.print ("public class _" + i.name () + classSuffix + " extends org.omg.CORBA.portable.ObjectImpl");
    stream.println (" implements " + com.sun.tools.corba.ee.idl.toJavaPortable.Util.javaName(i));
  } // writeClassDeclaration

  /**
   * Steps done within writeBody include:
   * 1.)  makeCtors ();
   * 2.)  buildMethodList ();
   * 3.)  makeMethods ();
   * 4.)  makeCORBAObjectMethods ()
   **/
  protected void writeBody ()
  {
    writeCtors ();
    buildMethodList ();
    writeMethods ();
    writeCORBAObjectMethods ();
    writeSerializationMethods ();
  } // writeBody

  /**
   *
   **/
  protected void writeClosing ()
  {
    stream.println ("} // class _" + i.name () + classSuffix);
  } // writeClosing

  /**
   *
   **/
  protected void closeStream ()
  {
    stream.close ();
  } // closeStream

  /**
   *
   **/
  protected void writeCtors ()
  {
    String name = i.name ();

    /***  the constructors are not generated as per ptc/00-01-08.pdf
     *    since these are non-standard APIs, and same can be accomplished
     *    programatically, we need to comment this out, in order to
     *    be able to generate standard stubs
     */

    /*************
    stream.println ("  // Constructors");
    stream.println ("  // NOTE:  If the default constructor is used, the");
    stream.println ("  //        object is useless until _set_delegate (...)");
    stream.println ("  //        is called.");
    stream.println ("  public _" + name + classSuffix + " ()");
    stream.println ("  {");
    stream.println ("    super ();");
    stream.println ("  }");
    stream.println ();
    stream.println ("  public _" + name + classSuffix + " (org.omg.CORBA.portable.Delegate delegate)");
    stream.println ("  {");
    stream.println ("    super ();");
    stream.println ("    _set_delegate (delegate);");
    stream.println ("  }");
    ***************/
    // This is confusing since we have localOptimization flag as well.
    // We have left this code because JCK team filed a P1 bug for changing
    // _opsClass to $opsClass. Will clean it up in Tiger
    // _REVISIT_ (Hemanth 03/05/2002)
    if (localStub) {
        stream.println ("  final public static java.lang.Class _opsClass = " + 
            name + "Operations.class;");
        stream.println ();
    }
    stream.println ();
  } // writeCtors

  /**
   * Build a list of all of the methods, keeping out duplicates.
   **/
  protected void buildMethodList ()
  {
    // Start from scratch
    methodList = new Vector ();

    buildMethodList (i);
  } // buildMethodList

  /**
   *
   **/
  private void buildMethodList (InterfaceEntry entry)
  {
    // Add the local methods
    Enumeration locals = entry.methods ().elements ();
    while (locals.hasMoreElements ())
      addMethod ((MethodEntry)locals.nextElement ());

    // Add the inherited methods
    Enumeration parents = entry.derivedFrom ().elements ();
    while (parents.hasMoreElements ())
    {
      InterfaceEntry parent = (InterfaceEntry)parents.nextElement ();
      if (!parent.name ().equals ("Object"))
        buildMethodList (parent);
    }
  } // buildMethodList

  /**
   *
   **/
  private void addMethod (MethodEntry method)
  {
    if (!methodList.contains (method))
      methodList.addElement (method);
  } // addMethod

  /**
   *
   **/
  protected void writeMethods ()
  {
    // Count the methods, attributes which are not readonly are
    // counted as 2 methods.
    int count = methodList.size ();
    Enumeration e = methodList.elements ();
    while (e.hasMoreElements ())
    {
      Object method = e.nextElement ();
      if (method instanceof AttributeEntry && !((AttributeEntry)method).readOnly ())
        ++count;
    }

    if( (((com.sun.tools.corba.ee.idl.toJavaPortable.Arguments) com.sun.tools.corba.ee.idl.toJavaPortable.Compile.compiler.arguments).LocalOptimization )
      && !isAbstract ) 
    {
        stream.println( "    final public static java.lang.Class _opsClass =" );
        stream.println( "        " + this.i.name() + "Operations.class;" );
    }

    // Write the methods
    int realI = 0;
    for (int i = 0; i < methodList.size (); ++i)
    {
      MethodEntry method = (MethodEntry)methodList.elementAt (i);
      if (!localStub) {
      ((com.sun.tools.corba.ee.idl.toJavaPortable.MethodGen)method.generator ()).stub (this.i.name(), isAbstract, symbolTable, method, stream, realI);
      } else {
      ((com.sun.tools.corba.ee.idl.toJavaPortable.MethodGen)method.generator ()).localstub (symbolTable, method, stream, realI, this.i);
      }
      if (method instanceof AttributeEntry && !((AttributeEntry)method).readOnly ())
        realI += 2;
      else
        ++realI;
    }
  } // writeMethods

  /**
   *
   **/
  private void buildIDList (InterfaceEntry entry, Vector list)
  {
    if (!entry.fullName ().equals ("org/omg/CORBA/Object"))
    {
      String id = com.sun.tools.corba.ee.idl.toJavaPortable.Util.stripLeadingUnderscoresFromID(entry.repositoryID().ID());
      if (!list.contains (id))
        list.addElement (id);
      Enumeration e = entry.derivedFrom ().elements ();
      while (e.hasMoreElements ())
        buildIDList ((InterfaceEntry)e.nextElement (), list);
    }
  } // buildIDList

  /**
   *
   **/
  private void writeIDs ()
  {
    Vector list = new Vector ();
    buildIDList (i, list);
    Enumeration e = list.elements ();
    boolean first = true;
    while (e.hasMoreElements ())
    {
      if (first)
        first = false;
      else
        stream.println (", ");
      stream.print ("    \"" + (String)e.nextElement () + '"');
    }
  } // writeIDs

  /**
   *
   **/
  protected void writeCORBAObjectMethods ()
  {
    stream.println ("  // Type-specific CORBA::Object operations");
    stream.println ("  private static String[] __ids = {");
    writeIDs ();
    stream.println ("};");
    stream.println ();
    stream.println ("  public String[] _ids ()");
    stream.println ("  {");
    stream.println ("    return (String[])__ids.clone ();");
    stream.println ("  }");
    stream.println ();
  } // writeCORBAObjectMethods

  /**
   *
   **/
  protected void writeSerializationMethods ()
  {
    stream.println ("  private void readObject (java.io.ObjectInputStream s) throws java.io.IOException");
    stream.println ("  {");
    stream.println ("     String str = s.readUTF ();");
    stream.println ("     String[] args = null;");
    stream.println ("     java.util.Properties props = null;");
    stream.println ("     org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init (args, props);");
    stream.println ("   try {");
    stream.println ("     org.omg.CORBA.Object obj = orb.string_to_object (str);");
    stream.println ("     org.omg.CORBA.portable.Delegate delegate = ((org.omg.CORBA.portable.ObjectImpl) obj)._get_delegate ();");
    stream.println ("     _set_delegate (delegate);");
    stream.println ("   } finally {");
    stream.println ("     orb.destroy() ;");
    stream.println ("   }");
    stream.println ("  }");
    stream.println ();
    stream.println ("  private void writeObject (java.io.ObjectOutputStream s) throws java.io.IOException");
    stream.println ("  {");
    stream.println ("     String[] args = null;");
    stream.println ("     java.util.Properties props = null;");
    stream.println ("     org.omg.CORBA.ORB orb = org.omg.CORBA.ORB.init (args, props);");
    stream.println ("   try {");
    stream.println ("     String str = orb.object_to_string (this);");
    stream.println ("     s.writeUTF (str);");
    stream.println ("   } finally {");
    stream.println ("     orb.destroy() ;");
    stream.println ("   }");
    stream.println ("  }");
  }

  protected Hashtable      symbolTable = null;
  protected InterfaceEntry i           = null;
  protected PrintWriter    stream      = null;

  // Unique to this generator
  protected Vector         methodList  = null;
  protected String         classSuffix = "";
  protected boolean        localStub = false;
  private   boolean        isAbstract = false;
} // class Stub
