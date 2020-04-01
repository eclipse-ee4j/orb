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
// -11aug1997<daz> No modification: comments for type_defs will appear in
//  helper, holder classes as a result of modifications to routines
//  makeHelper(), makeHolder() in class com.sun.tools.corba.ee.idl.toJava.Util.
// -F46082.51<daz> Remove -stateful feature; javaStatefulName() obsolete.
// -D61056   <klr> Use Util.helperName

import org.glassfish.corba.idl.InterfaceEntry;
import org.glassfish.corba.idl.InterfaceState;
import org.glassfish.corba.idl.PrimitiveEntry;
import org.glassfish.corba.idl.SequenceEntry;
import org.glassfish.corba.idl.StringEntry;
import org.glassfish.corba.idl.StructEntry;
import org.glassfish.corba.idl.SymtabEntry;
import org.glassfish.corba.idl.TypedefEntry;
import org.glassfish.corba.idl.UnionEntry;
import org.glassfish.corba.idl.constExpr.Expression;

import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Hashtable;

// Notes:

/**
 *
 **/
public class TypedefGen implements org.glassfish.corba.idl.TypedefGen, org.glassfish.corba.idl.toJavaPortable.JavaGenerator
{
  /**
   * Public zero-argument constructor.
   **/
  public TypedefGen ()
  {
  } // ctor

  /**
   *
   **/
  public void generate (Hashtable symbolTable, TypedefEntry t, PrintWriter stream)
  {
    this.symbolTable = symbolTable;
    this.t           = t;

    if (t.arrayInfo ().size () > 0 || t.type () instanceof SequenceEntry)
      generateHolder ();
    generateHelper ();
  } // generator

  /**
   *
   **/
  protected void generateHolder ()
  {
    ((Factories) org.glassfish.corba.idl.toJavaPortable.Compile.compiler.factories ()).holder ().generate (symbolTable, t);
  }

  /**
   *
   **/
  protected void generateHelper ()
  {
    ((Factories) org.glassfish.corba.idl.toJavaPortable.Compile.compiler.factories ()).helper ().generate (symbolTable, t);
  }

  ///////////////
  // From JavaGenerator

  private boolean inStruct (TypedefEntry entry)
  {
    boolean inStruct = false;
    if (entry.container () instanceof StructEntry || entry.container () instanceof UnionEntry)
      inStruct = true;
    else if (entry.container () instanceof InterfaceEntry)
    {
      InterfaceEntry i = (InterfaceEntry)entry.container ();
      if (i.state () != null)
      {
        Enumeration<InterfaceState> e = i.state().elements();
        while (e.hasMoreElements ())
          if (e.nextElement().entry == entry)
          {
            inStruct = true;
            break;
          }
      }
    }
    return inStruct;
  } // inStruct

  public int helperType (int index, String indent, org.glassfish.corba.idl.toJavaPortable.TCOffsets tcoffsets, String name, SymtabEntry entry, PrintWriter stream)
  {
    TypedefEntry td = (TypedefEntry)entry;
    boolean inStruct = inStruct (td);
    if (inStruct)
      tcoffsets.setMember (entry);
    else
      tcoffsets.set (entry);

    // Print the base types typecode
    index = ((org.glassfish.corba.idl.toJavaPortable.JavaGenerator)td.type ().generator ()).type (index, indent, tcoffsets, name, td.type (), stream);

    if (inStruct && !td.arrayInfo ().isEmpty())
      tcoffsets.bumpCurrentOffset (4); // for array length field

    // Print the array typecodes (if there are any)
    int dimensions = td.arrayInfo ().size ();
    for (int i = 0; i < dimensions; ++i)
    {
      String size = org.glassfish.corba.idl.toJavaPortable.Util.parseExpression((Expression) td.arrayInfo().elementAt(i));
      stream.println (indent + name + " = org.omg.CORBA.ORB.init ().create_array_tc (" + size + ", " + name + " );");
    }

    // If this typedef describes a struct/union member, don't put it
    // in an alias typedef; otherwise that's where it belongs.
    if (!inStruct)
      // <54697>
      //stream.println (indent + name + " = org.omg.CORBA.ORB.init ().create_alias_tc (id (), \"" + Util.stripLeadingUnderscores (td.name ()) + "\", " + name + ");");
      stream.println (indent + name + " = org.omg.CORBA.ORB.init ().create_alias_tc (" + org.glassfish.corba.idl.toJavaPortable.Util.helperName(td, true) + ".id (), \"" + org.glassfish.corba.idl.toJavaPortable.Util.stripLeadingUnderscores(td.name()) + "\", " + name + ");"); // <d61056>

    return index;
  } // helperType

  public int type (int index, String indent, org.glassfish.corba.idl.toJavaPortable.TCOffsets tcoffsets, String name, SymtabEntry entry, PrintWriter stream)
  {
    // The type() method is invoked from other emitters instead of when an IDL
    // typedef statement is being processed.  Code generated is identical minus the
    // generation of a create_alias_tc() which is required for IDL typedef's but not
    // needed when typedef is being processed as a member of struct/union/valuetype.

    return helperType( index, indent, tcoffsets, name, entry, stream);
  } // type

  public void helperRead (String entryName, SymtabEntry entry, PrintWriter stream)
  {
    org.glassfish.corba.idl.toJavaPortable.Util.writeInitializer("    ", "value", "", entry, stream);
    read (0, "    ", "value", entry, stream);
    stream.println ("    return value;");
  } // helperRead

  public void helperWrite (SymtabEntry entry, PrintWriter stream)
  {
    write (0, "    ", "value", entry, stream);
  } // helperWrite

  public int read (int index, String indent, String name, SymtabEntry entry, PrintWriter stream)
  {
    TypedefEntry td = (TypedefEntry)entry;
    String modifier = org.glassfish.corba.idl.toJavaPortable.Util.arrayInfo(td.arrayInfo());
    if (!modifier.equals (""))
    {
      // arrayInfo is a vector of Expressions which indicate the
      // number of array dimensions for this typedef.  But what if
      // this is a typedef of a sequence?
      // The `new' statement being generated must know the full
      // number of brackets.  That can be found in td.info.
      // For instance:
      // typedef sequence<short> A[10][10];
      // void proc (out A a);
      // typeModifier = "[10][10]"
      // td.info    = "short[][][]";
      // The first new statement generated is:
      // a.value = new short[10][][];
      // Note that the 3 sets of brackets come from td.info, not
      // arrayInfo;
      // The second new statement generated is:
      // a.value[_i1] = new short[10][];
      // ------------     ---- ------
      //    \           \    \
      //    name      baseName   arrayDcl
      int closingBrackets = 0;
      String loopIndex = "";
      String baseName;
      try
      {
        baseName = (String)td.dynamicVariable (org.glassfish.corba.idl.toJavaPortable.Compile.typedefInfo);
      }
      catch (NoSuchFieldException e)
      {
        baseName = td.name ();
      }
      int startArray = baseName.indexOf ('[');
      String arrayDcl = org.glassfish.corba.idl.toJavaPortable.Util.sansArrayInfo(baseName.substring(startArray)) + "[]"; // Add an extra set because the first gets stripped off in the loop.
      baseName = baseName.substring (0, startArray);

      // For interfaces having state, e.g., valuetypes.
      SymtabEntry baseEntry = (SymtabEntry) org.glassfish.corba.idl.toJavaPortable.Util.symbolTable.get (baseName.replace ('.', '/'));
      if (baseEntry instanceof InterfaceEntry && ((InterfaceEntry)baseEntry).state () != null)
        // <f46082.51> Remove -stateful feature; javaStatefulName() obsolete.
        //baseName = Util.javaStatefulName ((InterfaceEntry)baseEntry);
        baseName = org.glassfish.corba.idl.toJavaPortable.Util.javaName((InterfaceEntry) baseEntry);

      int end1stArray;
      while (!modifier.equals (""))
      {
        int rbracket = modifier.indexOf (']');
        String size = modifier.substring (1, rbracket);
        end1stArray = arrayDcl.indexOf (']');
        arrayDcl = '[' + size + arrayDcl.substring (end1stArray + 2);
        stream.println (indent + name + " = new " + baseName + arrayDcl + ';');
        loopIndex = "_o" + index++;
        stream.println (indent + "for (int " + loopIndex + " = 0;" + loopIndex + " < (" + size + "); ++" + loopIndex + ')');
        stream.println (indent + '{');
        ++closingBrackets;
        modifier = modifier.substring (rbracket + 1);
        indent = indent + "  ";
        name = name + '[' + loopIndex + ']';
      }
      end1stArray = arrayDcl.indexOf (']');
      if (td.type () instanceof SequenceEntry || td.type () instanceof PrimitiveEntry || td.type () instanceof StringEntry)
        index = ((org.glassfish.corba.idl.toJavaPortable.JavaGenerator)td.type ().generator ()).read (index, indent, name, td.type (), stream);
      else if (td.type () instanceof InterfaceEntry && td.type ().fullName ().equals ("org/omg/CORBA/Object"))
        stream.println (indent + name + " = istream.read_Object ();");
      else
        stream.println (indent + name + " = " + org.glassfish.corba.idl.toJavaPortable.Util.helperName(td.type(), true) + ".read (istream);"); // <d61056>
      for (int i = 0; i < closingBrackets; ++i)
      {
        indent = indent.substring (2);
        stream.println (indent + '}');
      }
    }
    else
    {
      SymtabEntry tdtype = org.glassfish.corba.idl.toJavaPortable.Util.typeOf(td.type());
      if (tdtype instanceof SequenceEntry || tdtype instanceof PrimitiveEntry || tdtype instanceof StringEntry)
        index = ((org.glassfish.corba.idl.toJavaPortable.JavaGenerator)tdtype.generator ()).read (index, indent, name, tdtype, stream);
      else if (tdtype instanceof InterfaceEntry && tdtype.fullName ().equals ("org/omg/CORBA/Object"))
        stream.println (indent + name + " = istream.read_Object ();");
      else
        stream.println (indent + name + " = " + org.glassfish.corba.idl.toJavaPortable.Util.helperName(tdtype, true) + ".read (istream);"); // <d61056>
    }
    return index;
  } // read

  public int write (int index, String indent, String name, SymtabEntry entry, PrintWriter stream)
  {
    TypedefEntry td = (TypedefEntry)entry;
    String modifier = org.glassfish.corba.idl.toJavaPortable.Util.arrayInfo(td.arrayInfo());
    if (!modifier.equals (""))
    {
      int closingBrackets = 0;
      String loopIndex = "";
      while (!modifier.equals (""))
      {
        int rbracket = modifier.indexOf (']');
        String size = modifier.substring (1, rbracket);
        stream.println (indent + "if (" + name + ".length != (" + size + "))");
        stream.println (indent + "  throw new org.omg.CORBA.MARSHAL (0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);");
        loopIndex = "_i" + index++;
        stream.println (indent + "for (int " + loopIndex + " = 0;" + loopIndex + " < (" + size + "); ++" + loopIndex + ')');
        stream.println (indent + '{');
        ++closingBrackets;
        modifier = modifier.substring (rbracket + 1);
        indent = indent + "  ";
        name = name + '[' + loopIndex + ']';
      }
      if (td.type () instanceof SequenceEntry || td.type () instanceof PrimitiveEntry || td.type () instanceof StringEntry)
        index = ((org.glassfish.corba.idl.toJavaPortable.JavaGenerator)td.type ().generator ()).write (index, indent, name, td.type (), stream);
      else if (td.type () instanceof InterfaceEntry && td.type ().fullName ().equals ("org/omg/CORBA/Object"))
        stream.println (indent + "ostream.write_Object (" + name + ");");
      else
        stream.println (indent + org.glassfish.corba.idl.toJavaPortable.Util.helperName(td.type(), true) + ".write (ostream, " + name + ");"); // <d61056>
      for (int i = 0; i < closingBrackets; ++i)
      {
        indent = indent.substring (2);
        stream.println (indent + '}');
      }
    }
    else
    {
      SymtabEntry tdtype = org.glassfish.corba.idl.toJavaPortable.Util.typeOf(td.type());
      if (tdtype instanceof SequenceEntry || tdtype instanceof PrimitiveEntry || tdtype instanceof StringEntry)
        index = ((org.glassfish.corba.idl.toJavaPortable.JavaGenerator)tdtype.generator ()).write (index, indent, name, tdtype, stream);
      else if (tdtype instanceof InterfaceEntry && tdtype.fullName ().equals ("org/omg/CORBA/Object"))
        stream.println (indent + "ostream.write_Object (" + name + ");");
      else
        stream.println (indent + org.glassfish.corba.idl.toJavaPortable.Util.helperName(tdtype, true) + ".write (ostream, " + name + ");"); // <d61056>
    }
    return index;
  } // write

  // From JavaGenerator
  ////////////////

  protected Hashtable     symbolTable = null;
  protected TypedefEntry  t           = null;
} // class TypedefGen

