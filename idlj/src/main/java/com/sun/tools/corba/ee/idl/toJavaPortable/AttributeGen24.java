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
// -D62023   <klr> New file to implement CORBA 2.4 RTF
//      REVISIT: These changes should be folded into AttributeGen.

import com.sun.tools.corba.ee.idl.AttributeEntry;
import com.sun.tools.corba.ee.idl.MethodEntry;

import java.io.PrintWriter;
import java.util.Hashtable;

/**
 *
 **/
public class AttributeGen24 extends MethodGenClone24 {
    /**
     * Public zero-argument constructor.
     **/
    public AttributeGen24() {
    } // ctor

    /**
     * (d62023-klr) Added for 2.4 RTF
     **/
    protected void abstractMethod(Hashtable symbolTable, MethodEntry m, PrintWriter stream) {
        AttributeEntry a = (AttributeEntry) m;

        // Generate for the get method
        super.abstractMethod(symbolTable, a, stream);

        // Generate for the set method if the attribute is not readonly
        if (!a.readOnly()) {
            setupForSetMethod();
            super.abstractMethod(symbolTable, a, stream);
            clear();
        }
    } // abstractMethod

    /**
     * (d62023-klr) Added for 2.4 RTF
     **/
    protected void interfaceMethod(Hashtable symbolTable, MethodEntry m, PrintWriter stream) {
        AttributeEntry a = (AttributeEntry) m;

        // Generate for the get method
        super.interfaceMethod(symbolTable, a, stream);

        // Generate for the set method if the attribute is not readonly
        if (!a.readOnly()) {
            setupForSetMethod();
            super.interfaceMethod(symbolTable, a, stream);
            clear();
        }
    } // interfaceMethod

} // class AttributeGen24
