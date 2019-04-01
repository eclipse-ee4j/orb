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

import com.sun.tools.corba.ee.idl.constExpr.Expression;

import java.io.PrintWriter;
import java.util.Hashtable;
import java.util.Vector;

/**
 * This is the symbol table entry for sequences.
 **/
public class SequenceEntry extends com.sun.tools.corba.ee.idl.SymtabEntry {
    protected SequenceEntry() {
        super();
        repositoryID(com.sun.tools.corba.ee.idl.Util.emptyID);
    } // ctor

    protected SequenceEntry(SequenceEntry that) {
        super(that);
        _maxSize = that._maxSize;
    } // ctor

    protected SequenceEntry(com.sun.tools.corba.ee.idl.SymtabEntry that, IDLID clone) {
        super(that, clone);
        if (!(that instanceof SequenceEntry))
            // If that is a SequenceEntry, then it is a container of this sequence, but it is not a module of this sequence. It's
            // name doesn't belong in the module name.
            if (module().equals(""))
                module(name());
            else if (!name().equals(""))
                module(module() + "/" + name());
        repositoryID(com.sun.tools.corba.ee.idl.Util.emptyID);
    } // ctor

    public Object clone() {
        return new SequenceEntry(this);
    } // clone

    public boolean isReferencable() {
        // A sequence is referencable if its component
        // type is.
        return type().isReferencable();
    }

    public void isReferencable(boolean value) {
        // NO-OP: this cannot be set for a sequence.
    }

    /**
     * Invoke the sequence generator.
     *
     * @param symbolTable the symbol table is a hash table whose key is a fully qualified type name and whose value is a
     * SymtabEntry or a subclass of SymtabEntry.
     * @param stream the stream to which the generator should sent its output.
     * @see com.sun.tools.corba.ee.idl.SymtabEntry
     */
    public void generate(Hashtable symbolTable, PrintWriter stream) {
        sequenceGen.generate(symbolTable, this, stream);
    } // generate

    /**
     * Access the sequence generator.
     *
     * @return an object which implements the SequenceGen interface.
     * @see com.sun.tools.corba.ee.idl.SequenceGen
     */
    public com.sun.tools.corba.ee.idl.Generator generator() {
        return sequenceGen;
    } // generator

    /**
     * the constant expression defining the maximum size of the sequence. If it is null, then the sequence is unbounded.
     */
    public void maxSize(Expression expr) {
        _maxSize = expr;
    } // maxSize

    /**
     * the constant expression defining the maximum size of the sequence. If it is null, then the sequence is unbounded.
     */
    public Expression maxSize() {
        return _maxSize;
    } // maxSize

    /** Only sequences can be contained within sequences. */
    public void addContained(com.sun.tools.corba.ee.idl.SymtabEntry entry) {
        _contained.addElement(entry);
    } // addContained

    /** Only sequences can be contained within sequences. */
    public Vector contained() {
        return _contained;
    } // contained

    static com.sun.tools.corba.ee.idl.SequenceGen sequenceGen;

    private Expression _maxSize = null;
    private Vector _contained = new Vector();
} // class SequenceEntry
