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
// -F46082.51<daz> Remove -stateful feature; javaStatefulName() obsolete.
// -D57067 <klr> suppress default init if an emit init explicitly specified.
// -D59071 <daz> Clone method entries when their content needs modification.
// -D59092 <klr> Valuetype supporting interfaces should implement interface.
// -D59418 <klr> Custom values implement org.omg.CORBA.CustomMarshal
// -D59418 <klr> Invert read/read_Value, write/write_Value for Simon
// -D60929 <klr> Update for RTF2.4 changes
// -D62018 <klr> write_value for value with value field x calls xHelper.write.
// -D62062 <klr> Add _write to value Helper to marshal state.
//   write_value for value subclass calls parent._write
// -D61650<daz> Remove '\n' from generated strings; use println()'s.

import java.io.PrintWriter;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Vector;

import com.sun.tools.corba.ee.idl.GenFileStream;
import com.sun.tools.corba.ee.idl.InterfaceEntry;
import com.sun.tools.corba.ee.idl.SymtabEntry;
import com.sun.tools.corba.ee.idl.TypedefEntry;
import com.sun.tools.corba.ee.idl.ValueEntry;
import com.sun.tools.corba.ee.idl.ValueBoxEntry;
import com.sun.tools.corba.ee.idl.InterfaceState;
import com.sun.tools.corba.ee.idl.MethodEntry;
import com.sun.tools.corba.ee.idl.PrimitiveEntry;
import com.sun.tools.corba.ee.idl.SequenceEntry;
import com.sun.tools.corba.ee.idl.StringEntry;

/**
 *
 **/
public class ValueGen implements com.sun.tools.corba.ee.idl.ValueGen, com.sun.tools.corba.ee.idl.toJavaPortable.JavaGenerator {
    /**
     * Public zero-argument constructor.
     **/
    public ValueGen() {
    } // ctor

    /**
     *
     **/
    public void generate(Hashtable symbolTable, ValueEntry v, PrintWriter str) {
        this.symbolTable = symbolTable;
        this.v = v;
        init();

        openStream();
        if (stream == null)
            return;
        generateTie();
        generateHelper();
        generateHolder();
        writeHeading();
        writeBody();
        writeClosing();
        closeStream();
    } // generate

    /**
     * Initialize members unique to this generator.
     **/
    protected void init() {
        emit = ((Arguments) com.sun.tools.corba.ee.idl.toJavaPortable.Compile.compiler.arguments).emit;
        factories = (com.sun.tools.corba.ee.idl.toJavaPortable.Factories) com.sun.tools.corba.ee.idl.toJavaPortable.Compile.compiler.factories();
    } // init

    /**
     *
     **/
    protected void openStream() {
        stream = com.sun.tools.corba.ee.idl.toJavaPortable.Util.stream(v, ".java");
    } // openStream

    /**
     * Generate a Tie class only when the user specifies the TIE option and the valuetype does support an interface.
     **/
    protected void generateTie() {
        boolean tie = ((Arguments) com.sun.tools.corba.ee.idl.toJavaPortable.Compile.compiler.arguments).TIEServer;
        if (v.supports().size() > 0 && tie) {
            com.sun.tools.corba.ee.idl.toJavaPortable.Factories factories = (com.sun.tools.corba.ee.idl.toJavaPortable.Factories) com.sun.tools.corba.ee.idl.toJavaPortable.Compile.compiler
                    .factories();
            factories.skeleton().generate(symbolTable, v);
        }
    } // generateTie

    /**
     *
     **/
    protected void generateHelper() {
        ((com.sun.tools.corba.ee.idl.toJavaPortable.Factories) com.sun.tools.corba.ee.idl.toJavaPortable.Compile.compiler.factories()).helper()
                .generate(symbolTable, v);
    } // generateHelper

    /**
     *
     **/
    protected void generateHolder() {
        ((com.sun.tools.corba.ee.idl.toJavaPortable.Factories) com.sun.tools.corba.ee.idl.toJavaPortable.Compile.compiler.factories()).holder()
                .generate(symbolTable, v);
    } // generateHolder

    /**
     *
     **/
    protected void writeHeading() {
        com.sun.tools.corba.ee.idl.toJavaPortable.Util.writePackage(stream, v);
        com.sun.tools.corba.ee.idl.toJavaPortable.Util.writeProlog(stream, ((GenFileStream) stream).name());

        if (v.comment() != null)
            v.comment().generate("", stream);

        if (v.isAbstract()) {
            writeAbstract();
            return;
        } else
            stream.print("public class " + v.name());

        // There should always be at least one parent: ValueBase
        SymtabEntry parent = (SymtabEntry) v.derivedFrom().elementAt(0);

        // If parent is ValueBase, it's mapped to java.io.Serializable
        String parentName = com.sun.tools.corba.ee.idl.toJavaPortable.Util.javaName(parent);
        boolean impl = false;

        if (parentName.equals("java.io.Serializable")) {
//    stream.print (" implements org.omg.CORBA.portable.ValueBase, org.omg.CORBA.portable.Streamable");
            stream.print(" implements org.omg.CORBA.portable.ValueBase"); // <d60929>
            impl = true;
        } else if (!((ValueEntry) parent).isAbstract())
            stream.print(" extends " + parentName);

        // if inheriting from abstract values
        for (int i = 0; i < v.derivedFrom().size(); i++) {
            parent = (SymtabEntry) v.derivedFrom().elementAt(i);
            if (((ValueEntry) parent).isAbstract()) {
                if (!impl) {
                    stream.print(" implements ");
                    impl = true;
                } else
                    stream.print(", ");
                stream.print(com.sun.tools.corba.ee.idl.toJavaPortable.Util.javaName(parent));
            }
        }
// <d59092-klr> Valuetype supporting interface implement Operations interface
// for supported IDL interface
        if (((ValueEntry) v).supports().size() > 0) {
            if (!impl) {
                stream.print(" implements ");
                impl = true;
            } else
                stream.print(", ");

            InterfaceEntry s = (InterfaceEntry) ((ValueEntry) v).supports().elementAt(0);
            // abstract supported classes don't have "Operations"
            if (s.isAbstract())
                stream.print(com.sun.tools.corba.ee.idl.toJavaPortable.Util.javaName(s));
            else
                stream.print(com.sun.tools.corba.ee.idl.toJavaPortable.Util.javaName(s) + "Operations");
        }

//  <d59418> Custom valuetypes implement org.omg.CORBA.CustomMarshal.
        if (((ValueEntry) v).isCustom()) {
            if (!impl) {
                stream.print(" implements ");
                impl = true;
            } else
                stream.print(", ");

            stream.print("org.omg.CORBA.CustomMarshal ");
        }

        stream.println();
        stream.println("{");
    } // writeHeading

    /**
     *
     **/
    protected void writeBody() {
        writeMembers();
        writeInitializers();
        writeConstructor(); // <d57067>
        writeTruncatable(); // <d60929>
        writeMethods();
    } // writeBody

    /**
     *
     **/
    protected void writeClosing() {
        if (v.isAbstract())
            stream.println("} // interface " + v.name());
        else
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
    protected void writeConstructor() {
        // Per Simon, 9/3/98, emit a protected default constructor
        if (!v.isAbstract() && !explicitDefaultInit) { // <d57067 - klr>
            stream.println("  protected " + v.name() + " () {}");
            stream.println();
        }
    } // writeConstructor

    /**
     *
     **/
    protected void writeTruncatable() // <d60929>
    {
        // Per Simon, 4/6/98, emit _truncatable_ids()
        if (!v.isAbstract()) {
            stream.println("  public String[] _truncatable_ids() {");
            stream.println(
                    "      return " + com.sun.tools.corba.ee.idl.toJavaPortable.Util.helperName(v, true) + ".get_instance().get_truncatable_base_ids();"); // <d61056>
            stream.println("  }");
            stream.println();
        }
    } // writeTruncatable

    /**
     *
     **/
    protected void writeMembers() {
        // if the value type contains no data members, a null return is expected
        if (v.state() == null)
            return;

        for (int i = 0; i < v.state().size(); i++) {
            InterfaceState member = (InterfaceState) v.state().elementAt(i);
            SymtabEntry entry = (SymtabEntry) member.entry;
            com.sun.tools.corba.ee.idl.toJavaPortable.Util.fillInfo(entry);

            if (entry.comment() != null)
                entry.comment().generate(" ", stream);

            String modifier = "  ";
            if (member.modifier == InterfaceState.Public)
                modifier = "  public ";
            com.sun.tools.corba.ee.idl.toJavaPortable.Util.writeInitializer(modifier, entry.name(), "", entry, stream);
        }
    } // writeMembers

    /**
     *
     **/
    protected void writeInitializers() {
        Vector init = v.initializers();
        if (init != null) {
            stream.println();
            for (int i = 0; i < init.size(); i++) {
                MethodEntry element = (MethodEntry) init.elementAt(i);
                element.valueMethod(true);
                ((com.sun.tools.corba.ee.idl.toJavaPortable.MethodGen) element.generator()).interfaceMethod(symbolTable, element, stream);
                if (element.parameters().isEmpty()) // <d57067-klr>
                    explicitDefaultInit = true;
            }
        }
    } // writeInitializers

    /**
     *
     **/
    protected void writeMethods() {
        // contained vector contains methods, attributes, const, enums, exceptions,
        // structs, unions, or typedefs that are declared inside the value object.
        // State members of the nested types are also included in this vector.
        // Thus, if the declaration of a constructed type is nested in the decl.
        // of a state member, e.g struct x {boolean b;} memberx;
        // the generation of the nested type must be handled here.
        Enumeration e = v.contained().elements();
        while (e.hasMoreElements()) {
            SymtabEntry contained = (SymtabEntry) e.nextElement();
            if (contained instanceof MethodEntry) {
                MethodEntry element = (MethodEntry) contained;
                ((com.sun.tools.corba.ee.idl.toJavaPortable.MethodGen) element.generator()).interfaceMethod(symbolTable, element, stream);
            } else {
                // Generate the type referenced by the typedef.
                if (contained instanceof TypedefEntry)
                    contained.type().generate(symbolTable, stream);

                // Note that we also need to generate the typedef itself if
                // contained is a typedef.
                contained.generate(symbolTable, stream);
            }
        }

        // Abstract values are mapped to interfaces. There is no need to generate
        // the bindings for inheriting methods in case of inheritance from other
        // abstract values or supporting interface
        if (v.isAbstract())
            return;

        // workaround: if the value type doesnot support any interfaces, a null
        // return is expected instead of an empty vector

        // if supporting an interfaces, generate bindings for inheriting methods
        if (v.supports().size() > 0) {
            InterfaceEntry intf = (InterfaceEntry) v.supports().elementAt(0);
            Enumeration el = intf.allMethods().elements();
            while (el.hasMoreElements()) {
                MethodEntry m = (MethodEntry) el.nextElement();
                // <d59071> Don't alter the symbol table/emit list elements!
                // m.container (v);
                // ((MethodGen)m.generator ()).interfaceMethod (symbolTable, m, stream);
                MethodEntry mClone = (MethodEntry) m.clone();
                mClone.container(v);
                ((com.sun.tools.corba.ee.idl.toJavaPortable.MethodGen) mClone.generator()).interfaceMethod(symbolTable, mClone, stream);
            }
        }

        // if inheriting from abstract values, generating bindings for all
        // inheriting methods
        for (int i = 0; i < v.derivedFrom().size(); i++) {
            ValueEntry parentValue = (ValueEntry) v.derivedFrom().elementAt(i);
            if (parentValue.isAbstract()) {
                Enumeration el = parentValue.allMethods().elements();
                while (el.hasMoreElements()) {
                    MethodEntry m = (MethodEntry) el.nextElement();
                    // <d59071> Don't alter the symbol table/emit list elements!
                    // m.container (v);
                    // ((MethodGen)m.generator ()).interfaceMethod (symbolTable, m, stream);
                    MethodEntry mClone = (MethodEntry) m.clone();
                    mClone.container(v);
                    ((com.sun.tools.corba.ee.idl.toJavaPortable.MethodGen) mClone.generator()).interfaceMethod(symbolTable, mClone, stream);
                }
            }
        }

        // writeStreamableMethods ();
    } // writeMethods

    /**
     *
     **/
    protected void writeStreamableMethods() {
        stream.println("  public void _read (org.omg.CORBA.portable.InputStream istream)");
        stream.println("  {");
        read(0, "    ", "this", v, stream);
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
        Vector state = vt.state();
        int noOfMembers = state == null ? 0 : state.size();
        String members = "_members" + index++;
        String tcOfMembers = "_tcOf" + members;

        stream.println(indent + "org.omg.CORBA.ValueMember[] " + members + " = new org.omg.CORBA.ValueMember[" + noOfMembers + "];");
        stream.println(indent + "org.omg.CORBA.TypeCode " + tcOfMembers + " = null;");
        // stream.println (""); // <d61650>

        String definedInrepId = "_id";
        String repId, version;

        for (int k = 0; k < noOfMembers; k++) {
            InterfaceState valueMember = (InterfaceState) state.elementAt(k);
            TypedefEntry member = (TypedefEntry) valueMember.entry;
            SymtabEntry mType = com.sun.tools.corba.ee.idl.toJavaPortable.Util.typeOf(member);
            if (hasRepId(member)) {
                repId = com.sun.tools.corba.ee.idl.toJavaPortable.Util.helperName(mType, true) + ".id ()"; // <d61056>
                if (mType instanceof ValueEntry || mType instanceof ValueBoxEntry)
                    // OBV spec is silent on defining VersionSpec for valuetype RepIds
                    version = "\"\"";
                else {
                    String id = mType.repositoryID().ID();
                    version = '"' + id.substring(id.lastIndexOf(':') + 1) + '"';
                }
            } else {
                repId = "\"\"";
                version = "\"\"";
            }

            // Get TypeCode for valuetype member and store it var. name given by tcOfMembers
            stream.println(indent + "// ValueMember instance for " + member.name());
            index = ((com.sun.tools.corba.ee.idl.toJavaPortable.JavaGenerator) member.generator()).type(index, indent, tcoffsets, tcOfMembers, member, stream);
            stream.println(indent + members + "[" + k + "] = new org.omg.CORBA.ValueMember (" // <d61650>
                    + '"' + member.name() + "\", "); // name
            stream.println(indent + "    " + repId + ", "); // id
            stream.println(indent + "    " + definedInrepId + ", "); // defined_in
            stream.println(indent + "    " + version + ", "); // version
            stream.println(indent + "    " + tcOfMembers + ", "); // type
            stream.println(indent + "    " + "null, "); // type_def
            stream.println(indent + "    " + "org.omg.CORBA." + (valueMember.modifier == InterfaceState.Public ? "PUBLIC_MEMBER" : "PRIVATE_MEMBER") + ".value"
                    + ");"); // access
        } // end for

        stream.println(indent + name + " = org.omg.CORBA.ORB.init ().create_value_tc (" + "_id, " + '"' + entry.name() + "\", " + getValueModifier(vt) + ", "
                + getConcreteBaseTypeCode(vt) + ", " + members + ");");

        return index;
    } // helperType

    public int type(int index, String indent, com.sun.tools.corba.ee.idl.toJavaPortable.TCOffsets tcoffsets, String name, SymtabEntry entry,
            PrintWriter stream) {
        stream.println(indent + name + " = " + com.sun.tools.corba.ee.idl.toJavaPortable.Util.helperName(entry, true) + ".type ();"); // <d61056>
        return index;
    } // type

    // Check for types which don't have a Repository ID: primitive,
    // string, arrays and sequences.

    private static boolean hasRepId(SymtabEntry member) {
        SymtabEntry mType = com.sun.tools.corba.ee.idl.toJavaPortable.Util.typeOf(member);
        return !(mType instanceof PrimitiveEntry || mType instanceof StringEntry
                || (mType instanceof TypedefEntry && !(((TypedefEntry) mType).arrayInfo().isEmpty()))
                || (mType instanceof TypedefEntry && member.type() instanceof SequenceEntry));
    } // hasRepId

    private static String getValueModifier(ValueEntry vt) {
        String mod = "NONE";
        if (vt.isCustom())
            mod = "CUSTOM";
        else if (vt.isAbstract())
            mod = "ABSTRACT";
        else if (vt.isSafe())
            mod = "TRUNCATABLE";
        return "org.omg.CORBA.VM_" + mod + ".value";
    } // getValueModifier

    private static String getConcreteBaseTypeCode(ValueEntry vt) {
        Vector v = vt.derivedFrom();
        if (!vt.isAbstract()) {
            SymtabEntry base = (SymtabEntry) vt.derivedFrom().elementAt(0);
            if (!"ValueBase".equals(base.name()))
                return com.sun.tools.corba.ee.idl.toJavaPortable.Util.helperName(base, true) + ".type ()"; // <d61056>
        }
        return "null";
    } // getConcreteBaseTypeCode

    public void helperRead(String entryName, SymtabEntry entry, PrintWriter stream) {
        // <d59418 - KLR> per Simon, make "static" read call istream.read_value.
        // put real marshalling code in read_value.

        if (((ValueEntry) entry).isAbstract()) {
            stream.println("    throw new org.omg.CORBA.BAD_OPERATION (\"abstract value cannot be instantiated\");");
        } else {
            stream.println("    return (" + entryName + ") ((org.omg.CORBA_2_3.portable.InputStream) istream).read_value (get_instance());"); // <d60929>
        }
        stream.println("  }");
        stream.println();

        // done with "read", now do "read_value with real marshalling code.

        stream.println("  public java.io.Serializable read_value (org.omg.CORBA.portable.InputStream istream)"); // <d60929>
        stream.println("  {");

        // per Simon, 3/3/99, read_value for custom values throws an exception
        if (((ValueEntry) entry).isAbstract()) {
            stream.println("    throw new org.omg.CORBA.BAD_OPERATION (\"abstract value cannot be instantiated\");");
        } else if (((ValueEntry) entry).isCustom()) {
            stream.println("    throw new org.omg.CORBA.BAD_OPERATION (\"custom values should use unmarshal()\");");
        } else {
            stream.println("    " + entryName + " value = new " + entryName + " ();");
            read(0, "    ", "value", entry, stream);
            stream.println("    return value;");
        }
        stream.println("  }");
        stream.println();
        // End of normal read method

        // Per Simon, 8/26/98 - Value helpers get an additional overloaded
        // read method where the value is passed in instead of "new'd" up. This is
        // used for reading parent value state.

        // Per Simon, 3/3/99 - Don't change this "read" for custom marshalling
        stream.println("  public static void read (org.omg.CORBA.portable.InputStream istream, " + entryName + " value)");
        stream.println("  {");
        read(0, "    ", "value", entry, stream);
    } // helperRead

    public int read(int index, String indent, String name, SymtabEntry entry, PrintWriter stream) {
        // First do the state members from concrete parent hierarchy
        Vector vParents = ((ValueEntry) entry).derivedFrom();
        if (vParents != null && vParents.size() != 0) {
            ValueEntry parent = (ValueEntry) vParents.elementAt(0);
            if (parent == null)
                return index;
            // Per Simon, 4/6/99 - call parent read. <d60929>
            if (!com.sun.tools.corba.ee.idl.toJavaPortable.Util.javaQualifiedName(parent).equals("java.io.Serializable")) // <d60929>
                stream.println(indent + com.sun.tools.corba.ee.idl.toJavaPortable.Util.helperName(parent, true) + ".read (istream, value);"); // <d60929> //
                                                                                                                                              // <d61056>
        }

        Vector vMembers = ((ValueEntry) entry).state();
        int noOfMembers = vMembers == null ? 0 : vMembers.size();

        for (int k = 0; k < noOfMembers; k++) {
            TypedefEntry member = (TypedefEntry) ((InterfaceState) vMembers.elementAt(k)).entry;
            String memberName = member.name();
            SymtabEntry mType = member.type();

            if (mType instanceof PrimitiveEntry || mType instanceof TypedefEntry || mType instanceof SequenceEntry || mType instanceof StringEntry
                    || !member.arrayInfo().isEmpty())
                index = ((com.sun.tools.corba.ee.idl.toJavaPortable.JavaGenerator) member.generator()).read(index, indent, name + '.' + memberName, member,
                        stream);
            else if (mType instanceof ValueEntry) {
                String returnType = com.sun.tools.corba.ee.idl.toJavaPortable.Util.javaQualifiedName(mType);
                if (mType instanceof ValueBoxEntry)
                    // <f46082.51> Remove -stateful.
                    // returnType = Util.javaStatefulName (mType);
                    returnType = com.sun.tools.corba.ee.idl.toJavaPortable.Util.javaName(mType);
                stream.println("    " + name + '.' + memberName + " = (" + returnType + ") ((org.omg.CORBA_2_3.portable.InputStream)istream).read_value ("
                        + com.sun.tools.corba.ee.idl.toJavaPortable.Util.helperName(mType, true) + // <d61056>
                        ".get_instance ());"); // <d61056>
            } else
                stream.println(
                        indent + name + '.' + memberName + " = " + com.sun.tools.corba.ee.idl.toJavaPortable.Util.helperName(mType, true) + ".read (istream);"); // <d61056>
        }

        return index;
    } // read

    public void helperWrite(SymtabEntry entry, PrintWriter stream) {
        // <d59418 - KLR> per Simon, make "static" write call istream.write_value.
        // put real marshalling code in write_value.
        stream.println("    ((org.omg.CORBA_2_3.portable.OutputStream) ostream).write_value (value, get_instance());"); // <d60929>
        stream.println("  }");
        stream.println();

        // <d62062>
        // per Simon, 4/27/99, add static _write that marshals the state of this
        // value for non-custom valuetypes
        if (!((ValueEntry) entry).isCustom()) {
            stream.println("  public static void _write (org.omg.CORBA.portable.OutputStream ostream, "
                    + com.sun.tools.corba.ee.idl.toJavaPortable.Util.javaName(entry) + " value)");
            stream.println("  {");
            write(0, "    ", "value", entry, stream);
            stream.println("  }");
            stream.println();
        }

        // done with "_write", now do "write_value
        stream.println("  public void write_value (org.omg.CORBA.portable.OutputStream ostream, java.io.Serializable obj)"); // <d60929>
        stream.println("  {");

        // per Simon, 3/3/99, write_value for custom values throws an exception
        if (((ValueEntry) entry).isCustom()) {
            stream.println("    throw new org.omg.CORBA.BAD_OPERATION (\"custom values should use marshal()\");");
        } else {
            String entryName = com.sun.tools.corba.ee.idl.toJavaPortable.Util.javaName(entry);
            stream.println("    _write (ostream, (" + entryName + ") obj);"); // <d62062>
//      write (0, "    ", "value", entry, stream); <d62062>
        }
    } // helperWrite

    public int write(int index, String indent, String name, SymtabEntry entry, PrintWriter stream) {
        // First do the state members from concrete parent hierarchy
        Vector vParents = ((ValueEntry) entry).derivedFrom();
        if (vParents != null && vParents.size() != 0) {
            ValueEntry parent = (ValueEntry) vParents.elementAt(0);
            if (parent == null)
                return index;
            // Per Simon, 4/06/99 - call parent write. <d60929>
            // Per Simon, 4/27/99 - call parent _write. <d62062>
            if (!com.sun.tools.corba.ee.idl.toJavaPortable.Util.javaQualifiedName(parent).equals("java.io.Serializable")) // <d60929>
                stream.println(indent + com.sun.tools.corba.ee.idl.toJavaPortable.Util.helperName(parent, true) + "._write (ostream, value);"); // <d60929>
                                                                                                                                                // <d61056>
                                                                                                                                                // <d62062>
        }

        Vector vMembers = ((ValueEntry) entry).state();
        int noOfMembers = vMembers == null ? 0 : vMembers.size();
        for (int k = 0; k < noOfMembers; k++) {
            TypedefEntry member = (TypedefEntry) ((InterfaceState) vMembers.elementAt(k)).entry;
            String memberName = member.name();
            SymtabEntry mType = member.type();

            if (mType instanceof PrimitiveEntry || mType instanceof TypedefEntry || mType instanceof SequenceEntry || mType instanceof StringEntry
                    || !member.arrayInfo().isEmpty())
                index = ((com.sun.tools.corba.ee.idl.toJavaPortable.JavaGenerator) member.generator()).write(index, indent, name + '.' + memberName, member,
                        stream);
            else
                stream.println(indent + com.sun.tools.corba.ee.idl.toJavaPortable.Util.helperName(mType, true) + // <d61056>
                        ".write (ostream, " + name + '.' + memberName + ");");
        }

        return index;
    } // write

    /**
     *
     **/
    protected void writeAbstract() {
        stream.print("public interface " + v.name());

        // workaround: if the abstract value type does not have any parent, a vector
        // containing ValueBase should be returned instead of an empty vector
        if (v.derivedFrom().size() == 0)
            stream.print(" extends org.omg.CORBA.portable.ValueBase"); // <d60929>
        else {
            SymtabEntry parent;
            // list the values the abstract value type inherits
            for (int i = 0; i < v.derivedFrom().size(); i++) {
                if (i == 0)
                    stream.print(" extends ");
                else
                    stream.print(", ");
                parent = (SymtabEntry) v.derivedFrom().elementAt(i);
                stream.print(com.sun.tools.corba.ee.idl.toJavaPortable.Util.javaName(parent));
            }
        }

        // list the interface the abstract value type supports
        if (v.supports().size() > 0) {
            stream.print(", ");
            SymtabEntry intf = (SymtabEntry) v.supports().elementAt(0);
            stream.print(com.sun.tools.corba.ee.idl.toJavaPortable.Util.javaName(intf));
        }
        stream.println();
        stream.println("{");
    }

    protected int emit = 0;
    protected com.sun.tools.corba.ee.idl.toJavaPortable.Factories factories = null;
    protected Hashtable symbolTable = null;
    protected ValueEntry v = null;
    protected PrintWriter stream = null;
    protected boolean explicitDefaultInit = false; // <d57067 - klr>
} // class ValueGen
