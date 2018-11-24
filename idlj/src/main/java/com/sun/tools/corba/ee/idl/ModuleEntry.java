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
import java.util.Hashtable;
import java.util.Vector;

/**
 * This is the symbol table entry for modules.
 **/
public class ModuleEntry extends com.sun.tools.corba.ee.idl.SymtabEntry {
    protected ModuleEntry() {
        super();
    } // ctor

    protected ModuleEntry(ModuleEntry that) {
        super(that);
        _contained = (Vector) that._contained.clone();
    } // ctor

    protected ModuleEntry(com.sun.tools.corba.ee.idl.SymtabEntry that, IDLID clone) {
        super(that, clone);

        if (module().equals(""))
            module(name());
        else if (!name().equals(""))
            module(module() + "/" + name());
    } // ctor

    public Object clone() {
        return new ModuleEntry(this);
    } // clone

    /**
     * Invoke the module generator.
     *
     * @param symbolTable the symbol table is a hash table whose key is a fully qualified type name and whose value is a
     * SymtabEntry or a subclass of SymtabEntry.
     * @param stream the stream to which the generator should sent its output.
     * @see com.sun.tools.corba.ee.idl.SymtabEntry
     */
    public void generate(Hashtable symbolTable, PrintWriter stream) {
        moduleGen.generate(symbolTable, this, stream);
    } // generate

    /**
     * Access the module generator.
     *
     * @return an object which implements the ModuleGen interface.
     * @see com.sun.tools.corba.ee.idl.ModuleGen
     */
    public com.sun.tools.corba.ee.idl.Generator generator() {
        return moduleGen;
    } // generator

    /**
     * alid entries in this vector are: TypedefEntry, ExceptionEntry, StructEntry, UnionEntry, EnumEntry, ConstEntry,
     * InterfaceEntry, ModuleEntry.
     */
    public void addContained(com.sun.tools.corba.ee.idl.SymtabEntry entry) {
        _contained.addElement(entry);
    } // addContained

    /**
     * This is a vector of SymtabEntry's. Valid entries in this vector are: TypedefEntry, ExceptionEntry, StructEntry,
     * UnionEntry, EnumEntry, ConstEntry, InterfaceEntry, ModuleEntry.
     */
    public Vector contained() {
        return _contained;
    } // contained

    private Vector _contained = new Vector();

    static com.sun.tools.corba.ee.idl.ModuleGen moduleGen;
} // class ModuleEntry
