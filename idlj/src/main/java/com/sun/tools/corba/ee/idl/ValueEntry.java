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

package com.sun.tools.corba.ee.idl;

// NOTES:

import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
//<daz> import com.sun.tools.corba.ee.idl.som.idlemit.TypeCode;

/**
 * This is the symbol table entry for values.
 **/
public class ValueEntry extends com.sun.tools.corba.ee.idl.InterfaceEntry {
    protected ValueEntry() {
        super();
    } // ctor

    protected ValueEntry(ValueEntry that) {
        super(that);
        _supportsNames = (Vector) that._supportsNames.clone();
        _supports = (Vector) that._supports.clone();
        _initializers = (Vector) that._initializers.clone();
        _custom = that._custom;
        _isSafe = that._isSafe;
    } // ctor

    protected ValueEntry(com.sun.tools.corba.ee.idl.SymtabEntry that, IDLID clone) {
        super(that, clone);
    } // ctor

    public Object clone() {
        return new ValueEntry(this);
    } // clone

    /**
     * Invoke the interface generator.
     *
     * @param symbolTable The symbol table is a hash table whose key is a fully qualified type name and whose value is a
     * SymtabEntry or a subclass of SymtabEntry.
     * @param stream The stream to which the generator should sent its output.
     * @see com.sun.tools.corba.ee.idl.SymtabEntry
     */
    public void generate(Hashtable symbolTable, PrintWriter stream) {
        valueGen.generate(symbolTable, this, stream);
    } // generate

    /**
     * Access the value generator.
     *
     * @return an object which implements the ValueGen interface.
     * @see com.sun.tools.corba.ee.idl.ValueGen
     */
    public com.sun.tools.corba.ee.idl.Generator generator() {
        return valueGen;
    } // generator

    /**
     * Add an InterfaceEntry to the list of interfaces which this value supports. During parsing, the parameter to this
     * method COULD be a ForwardEntry, but when parsing is complete, calling supports will return a vector which only
     * contains InterfaceEntry's.
     */
    public void addSupport(com.sun.tools.corba.ee.idl.SymtabEntry supports) {
        _supports.addElement(supports);
    } // addSupport

    /** This method returns a vector of InterfaceEntry's. */
    public Vector supports() {
        return _supports;
    } // supports

    /** Add to the list of support names. */
    public void addSupportName(String name) {
        _supportsNames.addElement(name);
    } // addSupportName

    /**
     * This method returns a vector of Strings, each of which is a fully qualified name of an interface. This vector
     * corresponds to the supports vector. The first element of this vector is the name of the first element of the supports
     * vector, etc.
     */
    public Vector supportsNames() {
        return _supportsNames;
    } // supportsNames

    /**
     * Add a parent value type to the list of parent types for the value. This method:
     * <UL>
     * <LI>Allows only the first added class to be concrete if the receiver is concrete.
     * <LI>Does not allow any added classes to be concrete if the receiver is abstract.
     * <LI>Does not allow duplicate classes to be added.
     * </UL>
     */
    void derivedFromAddElement(com.sun.tools.corba.ee.idl.SymtabEntry e, boolean isSafe, com.sun.tools.corba.ee.idl.Scanner scanner) {
        if (((com.sun.tools.corba.ee.idl.InterfaceType) e).getInterfaceType() != com.sun.tools.corba.ee.idl.InterfaceType.ABSTRACT) {
            if (isAbstract())
                com.sun.tools.corba.ee.idl.ParseException.nonAbstractParent2(scanner, fullName(), e.fullName());
            else if (derivedFrom().size() > 0)
                com.sun.tools.corba.ee.idl.ParseException.nonAbstractParent3(scanner, fullName(), e.fullName());
        }

        if (derivedFrom().contains(e))
            com.sun.tools.corba.ee.idl.ParseException.alreadyDerived(scanner, e.fullName(), fullName());

        if (isSafe)
            _isSafe = true;

        addDerivedFrom(e);
        addDerivedFromName(e.fullName());
        addParentType(e, scanner);
    } // derivedFromAddElement

    void derivedFromAddElement(com.sun.tools.corba.ee.idl.SymtabEntry e, com.sun.tools.corba.ee.idl.Scanner scanner) {
        // This code must check for duplicate interfaces being supported...
        addSupport(e);
        addSupportName(e.fullName());
        addParentType(e, scanner);
    } // derivedFromAddElement

    public boolean replaceForwardDecl(com.sun.tools.corba.ee.idl.ForwardEntry oldEntry, com.sun.tools.corba.ee.idl.InterfaceEntry newEntry) {
        if (super.replaceForwardDecl(oldEntry, newEntry))
            return true;
        int index = _supports.indexOf(oldEntry);
        if (index >= 0)
            _supports.setElementAt(newEntry, index);
        return (index >= 0);
    }

    void initializersAddElement(com.sun.tools.corba.ee.idl.MethodEntry method, com.sun.tools.corba.ee.idl.Scanner scanner) {
        // Check to see if the parameter signature is a duplicate:
        Vector params = method.parameters();
        int args = params.size();
        for (Enumeration e = _initializers.elements(); e.hasMoreElements();) {
            Vector params2 = ((com.sun.tools.corba.ee.idl.MethodEntry) e.nextElement()).parameters();
            if (args == params2.size()) {
                int i = 0;
                for (; i < args; i++)
                    if (!((com.sun.tools.corba.ee.idl.ParameterEntry) params.elementAt(i)).type()
                            .equals(((com.sun.tools.corba.ee.idl.ParameterEntry) params2.elementAt(i)).type()))
                        break;
                if (i >= args)
                    com.sun.tools.corba.ee.idl.ParseException.duplicateInit(scanner);
            }
        }
        _initializers.addElement(method);
    } // initializersAddElement

    public Vector initializers() {
        return _initializers;
    }

    /**
     * Tag all methods introduced by the value type as 'value methods' so they can be differentiated in the emitters from
     * any interface methods that the value type supports.
     */
    public void tagMethods() {
        for (Enumeration e = methods().elements(); e.hasMoreElements();)
            ((com.sun.tools.corba.ee.idl.MethodEntry) e.nextElement()).valueMethod(true);
    }

    // <46082.03> Revert to "IDL:"-style (i.e., regular) repository ID.

    /**
     * Calculate the 'repository ID' for the value. This method should not be called before the complete value type has been
     * parsed, since it computes the repository ID by computing hashcodes using all information contained in the value type
     * definition, not just the value type's fully qualified name.
     */
    /*
     * public void calcRepId () { ValueRepositoryId repId = new ValueRepositoryId (); repId.addType (this); calcRepId
     * (repId); String scopedName = fullName (); // KLR - following switched to new format 8/26/98 per Simon's request
     * repositoryID (new RepositoryID ( "H:" + repId.getHashcode() + ":" + scopedName)); } // calcRepId
     */

    /*
     * public void calcRepId (ValueRepositoryId repId) { Vector baseClasses = derivedFrom (); if (baseClasses.size () >= 1)
     * ((ValueEntry)baseClasses.elementAt (0)).calcRepId (repId); Vector state = state (); if (state != null) for
     * (Enumeration e = state.elements (); e.hasMoreElements ();) calcTypedefType (((InterfaceState)e.nextElement ()).entry,
     * repId); } // calcRepId
     *
     * private void calcValueType (ValueEntry entry, ValueRepositoryId repId) { if (repId.isNewType (entry)) { //<daz>
     * repId.addValue (TypeCode.tk_value); repId.addValue (org.omg.CORBA.TCKind._tk_value); entry.calcRepId (repId); } } //
     * calcValueType
     *
     * private void calcValueBoxType (ValueBoxEntry entry, ValueRepositoryId repId) { if (repId.isNewType (entry)) { //<daz>
     * repId.addValue (TypeCode.tk_value_box); repId.addValue (org.omg.CORBA.TCKind._tk_value_box); entry.calcRepId (repId);
     * } } // calcValueBoxType
     *
     * private void calcTypedefType (TypedefEntry entry, ValueRepositoryId repId) { if (repId.isNewType (entry)) { Vector
     * arrayInfo = entry.arrayInfo (); if (arrayInfo.size () > 0) { //<daz> repId.addValue (TypeCode.tk_array);
     * repId.addValue (org.omg.CORBA.TCKind._tk_array); for (Enumeration e = arrayInfo.elements (); e.hasMoreElements ();)
     * repId.addValue (((Number)((Expression)e.nextElement ()).value ()).intValue ()); } calcType (entry.type (), repId); }
     * } // calcTypedefType
     *
     * private void calcType (SymtabEntry entry, ValueRepositoryId repId) { if (entry instanceof TypedefEntry)
     * calcTypedefType ((TypedefEntry)entry, repId); else if (entry instanceof PrimitiveEntry) calcPrimitiveType (entry,
     * repId); else if (entry instanceof InterfaceEntry) //<daz> repId.addValue (TypeCode._tk_objref); repId.addValue
     * (org.omg.CORBA.TCKind._tk_objref); else if (entry instanceof EnumEntry) //<daz> repId.addValue (TypeCode._tk_enum);
     * repId.addValue (org.omg.CORBA.TCKind._tk_enum); else if (entry instanceof StringEntry) calcStringType ( (StringEntry)
     * entry, repId); else if (entry instanceof SequenceEntry) calcSequenceType ( (SequenceEntry) entry, repId); else if
     * (entry instanceof StructEntry) calcStructType ( (StructEntry) entry, repId); else if (entry instanceof UnionEntry)
     * calcUnionType ( (UnionEntry) entry, repId); else if (entry instanceof ValueBoxEntry) calcValueBoxType (
     * (ValueBoxEntry) entry, repId); else if (entry instanceof ValueEntry) calcValueType ( (ValueEntry) entry, repId); } //
     * calcType
     *
     * private static Hashtable primTypes;
     *
     * private void calcPrimitiveType (SymtabEntry entry, ValueRepositoryId repId) { if (primTypes == null) { primTypes =
     * new Hashtable (); //<daz> primTypes.put ("short", new Integer (TypeCode.tk_short )); primTypes.put ("short", new
     * Integer (org.omg.CORBA.TCKind._tk_short )); //<daz> primTypes.put ("long", new Integer (TypeCode.tk_long ));
     * primTypes.put ("long", new Integer (org.omg.CORBA.TCKind._tk_long )); //<daz> primTypes.put ("unsigned short", new
     * Integer (TypeCode.tk_ushort )); primTypes.put ("unsigned short", new Integer (org.omg.CORBA.TCKind._tk_ushort ));
     * //<daz> primTypes.put ("unsigned long", new Integer (TypeCode.tk_ulong )); primTypes.put ("unsigned long", new
     * Integer (org.omg.CORBA.TCKind._tk_ulong )); //<daz> primTypes.put ("char", new Integer (TypeCode.tk_char ));
     * primTypes.put ("char", new Integer (org.omg.CORBA.TCKind._tk_char )); //<daz> primTypes.put ("wchar", new Integer
     * (TypeCode.tk_wchar )); primTypes.put ("wchar", new Integer (org.omg.CORBA.TCKind._tk_wchar )); //<daz> primTypes.put
     * ("float", new Integer (TypeCode.tk_float )); primTypes.put ("float", new Integer (org.omg.CORBA.TCKind._tk_float ));
     * //<daz> primTypes.put ("double", new Integer (TypeCode.tk_double )); primTypes.put ("double", new Integer
     * (org.omg.CORBA.TCKind._tk_double )); //<daz> primTypes.put ("boolean", new Integer (TypeCode.tk_boolean));
     * primTypes.put ("boolean", new Integer (org.omg.CORBA.TCKind._tk_boolean)); //<daz> primTypes.put ("octet", new
     * Integer (TypeCode.tk_octet )); primTypes.put ("octet", new Integer (org.omg.CORBA.TCKind._tk_octet )); //<daz>
     * primTypes.put ("any", new Integer (TypeCode.tk_any )); } primTypes.put ("any", new Integer
     * (org.omg.CORBA.TCKind._tk_any )); } repId.addValue (((Integer)primTypes.get (entry.name ())).intValue ()); } //
     * calcPrimitiveType
     *
     * private void calcStringType (StringEntry entry, ValueRepositoryId repId) { repId.addValue (entry.name ().equals
     * (Parser.overrideName ("string")) ? //<daz> TypeCode.tk_string: org.omg.CORBA.TCKind._tk_string : //<daz>
     * TypeCode.tk_wstring); org.omg.CORBA.TCKind._tk_wstring); if (entry.maxSize () != null) try { repId.addValue ( (
     * (Number) (entry.maxSize ()).value ()). intValue ()); } catch (Exception exception) {} } // calcStringType
     *
     * private void calcSequenceType (SequenceEntry entry, ValueRepositoryId repId) { //<daz> repId.addValue
     * (TypeCode.tk_sequence); repId.addValue (org.omg.CORBA.TCKind._tk_sequence); if (entry.maxSize () != null) try {
     * repId.addValue (((Number)(entry.maxSize ()).value ()).intValue ()); } catch (Exception exception) {} } //
     * calcSequenceType
     *
     * private void calcStructType (StructEntry entry, ValueRepositoryId repId) { if (repId.isNewType (entry)) { //<daz>
     * repId.addValue (TypeCode.tk_struct); repId.addValue (org.omg.CORBA.TCKind._tk_struct); for (Enumeration e =
     * entry.members ().elements (); e.hasMoreElements ();) calcTypedefType ( (TypedefEntry) e.nextElement (), repId); } }
     * // calcStructType
     *
     * private void calcUnionType (UnionEntry entry, ValueRepositoryId repId) { if (repId.isNewType (entry)) { //<daz>
     * repId.addValue (TypeCode.tk_union); repId.addValue (org.omg.CORBA.TCKind._tk_union); calcType (entry.type (), repId);
     * for (Enumeration e = entry.branches ().elements (); e.hasMoreElements ();) calcTypedefType ( ( (UnionBranch)
     * e.nextElement ()).typedef, repId); } } // calcUnionType
     */

    /** Get the 'custom' marshaling property. */
    public boolean isCustom() {
        return _custom;
    }

    /** Set the 'custom' marshaling property. */
    public void setCustom(boolean isCustom) {
        _custom = isCustom;
    }

    /**
     * Return whether or not the value type can be "safely" truncated to its concrete parent type.
     */
    public boolean isSafe() {
        return _isSafe;
    }

    private Vector _supportsNames = new Vector();
    private Vector _supports = new Vector();
    private Vector _initializers = new Vector();
    private boolean _custom = false;
    private boolean _isSafe = false;

    static com.sun.tools.corba.ee.idl.ValueGen valueGen;
} // class ValueEntry
