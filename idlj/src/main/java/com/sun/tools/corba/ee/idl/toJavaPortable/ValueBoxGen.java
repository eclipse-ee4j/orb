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
// -D60929   <klr> Update for RTF2.4 changes
// -D61056   <klr> Use Util.helperName
// -D62023   <klr> Fix generation botch in helper.read for boxed bounded strings

import java.io.PrintWriter;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Vector;

import com.sun.tools.corba.ee.idl.GenFileStream;
import com.sun.tools.corba.ee.idl.SymtabEntry;
import com.sun.tools.corba.ee.idl.TypedefEntry;
import com.sun.tools.corba.ee.idl.ValueEntry;
import com.sun.tools.corba.ee.idl.ValueBoxEntry;
import com.sun.tools.corba.ee.idl.InterfaceState;
import com.sun.tools.corba.ee.idl.PrimitiveEntry;
import com.sun.tools.corba.ee.idl.SequenceEntry;
import com.sun.tools.corba.ee.idl.StringEntry;

/**
 *
 **/
public class ValueBoxGen implements com.sun.tools.corba.ee.idl.ValueBoxGen, com.sun.tools.corba.ee.idl.toJavaPortable.JavaGenerator {
    /**
     * Public zero-argument constructor.
     **/
    public ValueBoxGen() {
    } // ctor

    /**
     *
     **/
    public void generate(Hashtable symbolTable, ValueBoxEntry v, PrintWriter str) {
        this.symbolTable = symbolTable;
        this.v = v;

        TypedefEntry member = ((InterfaceState) v.state().elementAt(0)).entry;
        SymtabEntry mType = member.type();
        // if it's primitive type, generate a java class
        if (mType instanceof PrimitiveEntry) {
            openStream();
            if (stream == null)
                return;
            writeHeading();
            writeBody();
            writeClosing();
            closeStream();
        } else {
            // If a constructed type is nested in the value box,
            // value v struct s {...};
            // the bindings for the nested type must be handled here
            Enumeration e = v.contained().elements();
            while (e.hasMoreElements()) {
                SymtabEntry contained = (SymtabEntry) e.nextElement();

                // in case of value box w/ nested enum, ex: value v enum e {e0, e1,...};
                // the SymtabEntry for the enum and labels are contained in the vector.
                // Must check the type to ignore the SymtabEntry for labels.
                if (contained.type() != null)
                    contained.type().generate(symbolTable, stream);
            }
        }
        generateHelper();
        generateHolder();
    } // generate

    /**
     *
     **/
    protected void openStream() {
        stream = com.sun.tools.corba.ee.idl.toJavaPortable.Util.stream(v, ".java");
    } // openStream

    /**
     *
     **/
    protected void generateHelper() {
        ((Factories) com.sun.tools.corba.ee.idl.toJavaPortable.Compile.compiler.factories()).helper().generate(symbolTable, v);
    } // generateHelper

    /**
     *
     **/
    protected void generateHolder() {
        ((Factories) com.sun.tools.corba.ee.idl.toJavaPortable.Compile.compiler.factories()).holder().generate(symbolTable, v);
    } // generateHolder

    /**
     *
     **/
    protected void writeHeading() {
        com.sun.tools.corba.ee.idl.toJavaPortable.Util.writePackage(stream, v);
        com.sun.tools.corba.ee.idl.toJavaPortable.Util.writeProlog(stream, ((GenFileStream) stream).name());
        if (v.comment() != null)
            v.comment().generate("", stream);
//  stream.println ("public class " + v.name () + " implements org.omg.CORBA.portable.ValueBase, org.omg.CORBA.portable.Streamable");
        stream.println("public class " + v.name() + " implements org.omg.CORBA.portable.ValueBase"); // <d60929>
        stream.println("{");
    } // writeHeading

    /**
     *
     **/
    protected void writeBody() {
        InterfaceState member = (InterfaceState) v.state().elementAt(0);
        SymtabEntry entry = (SymtabEntry) member.entry;
        com.sun.tools.corba.ee.idl.toJavaPortable.Util.fillInfo(entry);
        if (entry.comment() != null)
            entry.comment().generate(" ", stream);
        stream.println("  public " + com.sun.tools.corba.ee.idl.toJavaPortable.Util.javaName(entry) + " value;");
        stream.println("  public " + v.name() + " (" + com.sun.tools.corba.ee.idl.toJavaPortable.Util.javaName(entry) + " initial)");
        stream.println("  {");
        stream.println("    value = initial;");
        stream.println("  }");
        stream.println();
        writeTruncatable(); // <d60929>
//  writeStreamableMethods ();
    } // writeBody

    /**
     *
     **/
    protected void writeTruncatable() // <d60929>
    {
        // Per Simon, 4/6/98, emit _truncatable_ids()
        stream.println("  public String[] _truncatable_ids() {");
        stream.println("      return " + com.sun.tools.corba.ee.idl.toJavaPortable.Util.helperName(v, true) + ".get_instance().get_truncatable_base_ids();"); // <d61056>
        stream.println("  }");
        stream.println();
    } // writeTruncatable

    /**
     *
     **/
    protected void writeClosing() {
        stream.println("} // class " + v.name());
    } // writeClosing

    /**
     *
     **/
    protected void closeStream() {
        stream.close();
    } // closeStream

    /**
     *
     **/
    protected void writeStreamableMethods() {
        stream.println("  public void _read (org.omg.CORBA.portable.InputStream istream)");
        stream.println("  {");
        streamableRead("this", v, stream);
        stream.println("  }");
        stream.println();
        stream.println("  public void _write (org.omg.CORBA.portable.OutputStream ostream)");
        stream.println("  {");
        write(0, "    ", "this", v, stream);
        stream.println("  }");
        stream.println();
        stream.println("  public org.omg.CORBA.TypeCode _type ()");
        stream.println("  {");
        stream.println("    return " + com.sun.tools.corba.ee.idl.toJavaPortable.Util.helperName(v, false) + ".type ();"); // <d61056>
        stream.println("  }");
    } // writeStreamableMethods

    ///////////////
    // From JavaGenerator

    public int helperType(int index, String indent, com.sun.tools.corba.ee.idl.toJavaPortable.TCOffsets tcoffsets, String name, SymtabEntry entry,
            PrintWriter stream) {
        ValueEntry vt = (ValueEntry) entry;
        TypedefEntry member = (TypedefEntry) ((InterfaceState) (vt.state()).elementAt(0)).entry;
        SymtabEntry mType = com.sun.tools.corba.ee.idl.toJavaPortable.Util.typeOf(member);
        index = ((com.sun.tools.corba.ee.idl.toJavaPortable.JavaGenerator) mType.generator()).type(index, indent, tcoffsets, name, mType, stream);
        stream.println(indent + name + " = org.omg.CORBA.ORB.init ().create_value_box_tc (" + "_id, " + '"' + entry.name() + "\", " + name + ");");
        return index;
    } // helperType

    public int type(int index, String indent, com.sun.tools.corba.ee.idl.toJavaPortable.TCOffsets tcoffsets, String name, SymtabEntry entry,
            PrintWriter stream) {
        stream.println(indent + name + " = " + com.sun.tools.corba.ee.idl.toJavaPortable.Util.helperName(entry, true) + ".type ();"); // <d61056>
        return index;
    } // type

    public int read(int index, String indent, String name, SymtabEntry entry, PrintWriter stream) {
        return index;
    } // read

    public void helperRead(String entryName, SymtabEntry entry, PrintWriter stream) {
        // <d59418 - KLR> per Simon, make "static" read call istream.read_value.
        // put real marshalling code in read_value.
        stream.println("    return (" + entryName + ") ((org.omg.CORBA_2_3.portable.InputStream) istream).read_value (get_instance());"); // <d60929>
        stream.println("  }");
        stream.println();

        // done with "read", now do "read_value with real marshalling code.

        stream.println("  public java.io.Serializable read_value (org.omg.CORBA.portable.InputStream istream)"); // <d60929>
        stream.println("  {");
        // end of <d59418> changes

        String indent = "    ";
        Vector vMembers = ((ValueBoxEntry) entry).state();
        TypedefEntry member = ((InterfaceState) vMembers.elementAt(0)).entry;
        SymtabEntry mType = member.type();
        if (mType instanceof PrimitiveEntry || mType instanceof SequenceEntry || mType instanceof TypedefEntry || mType instanceof StringEntry
                || !member.arrayInfo().isEmpty()) {
            stream.println(indent + com.sun.tools.corba.ee.idl.toJavaPortable.Util.javaName(mType) + " tmp;"); // <d62023>
            ((com.sun.tools.corba.ee.idl.toJavaPortable.JavaGenerator) member.generator()).read(0, indent, "tmp", member, stream);
        } else if (mType instanceof ValueEntry || mType instanceof ValueBoxEntry)
            stream.println(indent + com.sun.tools.corba.ee.idl.toJavaPortable.Util.javaQualifiedName(mType) + " tmp = ("
                    + com.sun.tools.corba.ee.idl.toJavaPortable.Util.javaQualifiedName(mType)
                    + ") ((org.omg.CORBA_2_3.portable.InputStream)istream).read_value ("
                    + com.sun.tools.corba.ee.idl.toJavaPortable.Util.helperName(mType, true) + ".get_instance ());"); // <d60929> // <d61056>
        else
            stream.println(indent + com.sun.tools.corba.ee.idl.toJavaPortable.Util.javaName(mType) + " tmp = "
                    + com.sun.tools.corba.ee.idl.toJavaPortable.Util.helperName(mType, true) + ".read (istream);"); // <d61056>
        if (mType instanceof PrimitiveEntry)
            stream.println(indent + "return new " + entryName + " (tmp);");
        else
            stream.println(indent + "return tmp;");
    } // helperRead

    public void helperWrite(SymtabEntry entry, PrintWriter stream) {
        // <d59418 - KLR> per Simon, make "static" write call istream.write_value.
        // put real marshalling code in write_value.
        stream.println("    ((org.omg.CORBA_2_3.portable.OutputStream) ostream).write_value (value, get_instance());"); // <d60929>
        stream.println("  }");
        stream.println();

        // done with "write", now do "write_value with real marshalling code.

        stream.println("  public void write_value (org.omg.CORBA.portable.OutputStream ostream, java.io.Serializable obj)"); // <d60929>
        stream.println("  {");

        String entryName = com.sun.tools.corba.ee.idl.toJavaPortable.Util.javaName(entry);
        stream.println("    " + entryName + " value  = (" + entryName + ") obj;");
        write(0, "    ", "value", entry, stream);
    } // helperWrite

    public int write(int index, String indent, String name, SymtabEntry entry, PrintWriter stream) {
        Vector vMembers = ((ValueEntry) entry).state();
        TypedefEntry member = ((InterfaceState) vMembers.elementAt(0)).entry;
        SymtabEntry mType = member.type();

        if (mType instanceof PrimitiveEntry || !member.arrayInfo().isEmpty())
            index = ((com.sun.tools.corba.ee.idl.toJavaPortable.JavaGenerator) member.generator()).write(index, indent, name + ".value", member, stream);
        else if (mType instanceof SequenceEntry || mType instanceof StringEntry || mType instanceof TypedefEntry || !member.arrayInfo().isEmpty())
            index = ((com.sun.tools.corba.ee.idl.toJavaPortable.JavaGenerator) member.generator()).write(index, indent, name, member, stream);
        else if (mType instanceof ValueEntry || mType instanceof ValueBoxEntry)
            stream.println(indent + "((org.omg.CORBA_2_3.portable.OutputStream)ostream).write_value ((java.io.Serializable) value, " // <d60929>
                    + com.sun.tools.corba.ee.idl.toJavaPortable.Util.helperName(mType, true) // <d61056>
                    + ".get_instance ());"); // <d61056>
        else
            stream.println(indent + com.sun.tools.corba.ee.idl.toJavaPortable.Util.helperName(mType, true) + ".write (ostream, " + name + ");"); // <d61056>
        return index;
    } // write

    protected void writeAbstract() {
    } // writeAbstract

    protected void streamableRead(String entryName, SymtabEntry entry, PrintWriter stream) {
        Vector vMembers = ((ValueBoxEntry) entry).state();
        TypedefEntry member = ((InterfaceState) vMembers.elementAt(0)).entry;
        SymtabEntry mType = member.type();
        if (mType instanceof PrimitiveEntry || mType instanceof SequenceEntry || mType instanceof TypedefEntry || mType instanceof StringEntry
                || !member.arrayInfo().isEmpty()) {
            SymtabEntry mEntry = (SymtabEntry) ((InterfaceState) vMembers.elementAt(0)).entry;
            ((com.sun.tools.corba.ee.idl.toJavaPortable.JavaGenerator) member.generator()).read(0, "    ", entryName + ".value", member, stream);
        } else if (mType instanceof ValueEntry || mType instanceof ValueBoxEntry)
            stream.println("    " + entryName + ".value = (" + com.sun.tools.corba.ee.idl.toJavaPortable.Util.javaQualifiedName(mType)
                    + ") ((org.omg.CORBA_2_3.portable.InputStream)istream).read_value ("
                    + com.sun.tools.corba.ee.idl.toJavaPortable.Util.helperName(mType, true) + ".get_instance ());"); // <d60929> // <d61056>
        else
            stream.println("    " + entryName + ".value = " + com.sun.tools.corba.ee.idl.toJavaPortable.Util.helperName(mType, true) + ".read (istream);"); // <d61056>
    } // streamableRead

    protected Hashtable symbolTable = null;
    protected ValueBoxEntry v = null;
    protected PrintWriter stream = null;
} // class ValueBoxGen
