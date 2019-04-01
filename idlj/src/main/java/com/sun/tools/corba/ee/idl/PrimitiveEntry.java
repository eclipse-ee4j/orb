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

/**
 * This is the symbol table entry for primitive types: octet, char, short, long, long long (and unsigned versions),
 * float, double, string.
 **/
public class PrimitiveEntry extends com.sun.tools.corba.ee.idl.SymtabEntry {
    protected PrimitiveEntry() {
        super();
        repositoryID(Util.emptyID);
    } // ctor

    protected PrimitiveEntry(String name) {
        name(name);
        module("");
        repositoryID(Util.emptyID);
    } // ctor

    protected PrimitiveEntry(PrimitiveEntry that) {
        super(that);
    } // ctor

    public Object clone() {
        return new PrimitiveEntry(this);
    } // clone

    /**
     * Invoke the primitive type generator.
     *
     * @param symbolTable the symbol table is a hash table whose key is a fully qualified type name and whose value is a
     * SymtabEntry or a subclass of SymtabEntry.
     * @param stream the stream to which the generator should sent its output.
     * @see com.sun.tools.corba.ee.idl.SymtabEntry
     */
    public void generate(Hashtable symbolTable, PrintWriter stream) {
        primitiveGen.generate(symbolTable, this, stream);
    } // generate

    /**
     * Access the primitive type generator.
     *
     * @return an object which implements the PrimitiveGen interface.
     * @see com.sun.tools.corba.ee.idl.PrimitiveGen
     */
    public com.sun.tools.corba.ee.idl.Generator generator() {
        return primitiveGen;
    } // generator

    static com.sun.tools.corba.ee.idl.PrimitiveGen primitiveGen;
} // class PrimitiveEntry
