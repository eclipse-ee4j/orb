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

/**
 * This is the symbol table entry for forward declarations of interfaces.
 **/
public class ForwardEntry extends com.sun.tools.corba.ee.idl.SymtabEntry implements com.sun.tools.corba.ee.idl.InterfaceType {
    protected ForwardEntry() {
        super();
    } // ctor

    protected ForwardEntry(ForwardEntry that) {
        super(that);
    } // ctor

    protected ForwardEntry(com.sun.tools.corba.ee.idl.SymtabEntry that, IDLID clone) {
        super(that, clone);
        if (module().equals(""))
            module(name());
        else if (!name().equals(""))
            module(module() + "/" + name());
    } // ctor

    public Object clone() {
        return new ForwardEntry(this);
    } // clone

    /**
     * Invoke the forward declaration generator.
     *
     * @param symbolTable the symbol table is a hash table whose key is a fully qualified type name and whose value is a
     * SymtabEntry or a subclass of SymtabEntry.
     * @param stream the stream to which the generator should sent its output.
     * @see com.sun.tools.corba.ee.idl.SymtabEntry
     */
    public void generate(Hashtable symbolTable, PrintWriter stream) {
        forwardGen.generate(symbolTable, this, stream);
    } // generate

    /**
     * Access the interface generator.
     *
     * @return an object which implements the InterfaceGen interface.
     * @see com.sun.tools.corba.ee.idl.InterfaceGen
     */
    public com.sun.tools.corba.ee.idl.Generator generator() {
        return forwardGen;
    } // generator

    static boolean replaceForwardDecl(com.sun.tools.corba.ee.idl.InterfaceEntry interfaceEntry) {
        boolean result = true;
        try {
            ForwardEntry forwardEntry = (ForwardEntry) com.sun.tools.corba.ee.idl.Parser.symbolTable.get(interfaceEntry.fullName());
            if (forwardEntry != null) {
                result = (interfaceEntry.getInterfaceType() == forwardEntry.getInterfaceType());
                forwardEntry.type(interfaceEntry);

                // If this interface has been forward declared, there are probably
                // other interfaces which derive from a ForwardEntry. Replace
                // those ForwardEntry's with this InterfaceEntry:
                interfaceEntry.forwardedDerivers = forwardEntry.derivers;
                for (Enumeration derivers = forwardEntry.derivers.elements(); derivers.hasMoreElements();)
                    ((com.sun.tools.corba.ee.idl.InterfaceEntry) derivers.nextElement()).replaceForwardDecl(forwardEntry, interfaceEntry);

                // Replace the entry's whose types are forward declarations:
                for (Enumeration types = forwardEntry.types.elements(); types.hasMoreElements();)
                    ((com.sun.tools.corba.ee.idl.SymtabEntry) types.nextElement()).type(interfaceEntry);
            }
        } catch (Exception exception) {
        }
        return result;
    } // replaceForwardDecl

    ///////////////
    // Implement interface InterfaceType

    public int getInterfaceType() {
        return _type;
    }

    public void setInterfaceType(int type) {
        _type = type;
    }

    static com.sun.tools.corba.ee.idl.ForwardGen forwardGen;
    Vector derivers = new Vector(); // Vector of InterfaceEntry's.
    Vector types = new Vector(); // Vector of the entry's whose type is a forward declaration.
    private int _type = com.sun.tools.corba.ee.idl.InterfaceType.NORMAL; // interface type
} // class ForwardEntry
