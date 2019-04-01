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
 * This is the symbol table entry for forward declarations of values.
 **/
public class ForwardValueEntry extends ForwardEntry {
    protected ForwardValueEntry() {
        super();
    } // ctor

    protected ForwardValueEntry(ForwardValueEntry that) {
        super(that);
    } // ctor

    protected ForwardValueEntry(com.sun.tools.corba.ee.idl.SymtabEntry that, com.sun.tools.corba.ee.idl.IDLID clone) {
        super(that, clone);
    } // ctor

    public Object clone() {
        return new ForwardValueEntry(this);
    } // clone

    /**
     * Invoke the forward value declaration generator.
     *
     * @param symbolTable the symbol table is a hash table whose key is a fully qualified type name and whose value is a
     * SymtabEntry or a subclass of SymtabEntry.
     * @param stream the stream to which the generator should sent its output.
     * @see com.sun.tools.corba.ee.idl.SymtabEntry
     */
    public void generate(Hashtable symbolTable, PrintWriter stream) {
        forwardValueGen.generate(symbolTable, this, stream);
    } // generate

    /**
     * Access the interface generator.
     *
     * @return an object which implements the ForwardValueGen interface.
     * @see com.sun.tools.corba.ee.idl.ValueGen
     */
    public com.sun.tools.corba.ee.idl.Generator generator() {
        return forwardValueGen;
    } // generator

    static com.sun.tools.corba.ee.idl.ForwardValueGen forwardValueGen;
} // class ForwardValueEntry
