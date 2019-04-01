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
 * This is the symbol table entry for the #include statement.
 **/
public class IncludeEntry extends com.sun.tools.corba.ee.idl.SymtabEntry {
    protected IncludeEntry() {
        super();
        repositoryID(Util.emptyID);
    } // ctor

    protected IncludeEntry(com.sun.tools.corba.ee.idl.SymtabEntry that) {
        super(that, new com.sun.tools.corba.ee.idl.IDLID());
        module(that.name());
        name("");
    } // ctor

    protected IncludeEntry(IncludeEntry that) {
        super(that);
    } // ctor

    public Object clone() {
        return new IncludeEntry(this);
    } // clone

    /**
     * Invoke the Include type generator.
     *
     * @param symbolTable the symbol table is a hash table whose key is a fully qualified type name and whose value is a
     * SymtabEntry or a subclass of SymtabEntry.
     * @param stream the stream to which the generator should sent its output.
     * @see com.sun.tools.corba.ee.idl.SymtabEntry
     */
    public void generate(Hashtable symbolTable, PrintWriter stream) {
        includeGen.generate(symbolTable, this, stream);
    } // generate

    /**
     * Access the Include type generator.
     *
     * @return an object which implements the IncludeGen interface.
     * @see com.sun.tools.corba.ee.idl.IncludeGen
     */
    public com.sun.tools.corba.ee.idl.Generator generator() {
        return includeGen;
    } // generator

    // d44810
    /** Set the fully-qualified file specification of this include file. */
    public void absFilename(String afn) {
        _absFilename = afn;
    }

    // d44810
    /**
     * Access the fully-qualified file specification of this include.
     *
     * @return a string containing the path of the include file.
     */
    public String absFilename() {
        return _absFilename;
    }

    /**
     * Add an IncludeEntry to the list of files which this included file includes.
     */
    public void addInclude(IncludeEntry entry) {
        includeList.addElement(entry);
    } // addInclude

    /** Get the list of files which this file includes. */
    public Vector includes() {
        return includeList;
    } // includes

    static com.sun.tools.corba.ee.idl.IncludeGen includeGen;
    /** List of files this file includes */
    private Vector includeList = new Vector();
    // d44810
    /** Absolute file name for .u file generation. */
    private String _absFilename = null;
} // class IncludeEntry
