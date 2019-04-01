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

import java.io.PrintWriter;
import java.util.Hashtable;

import com.sun.tools.corba.ee.idl.ExceptionEntry;

/**
 *
 **/
public class ExceptionGen extends StructGen implements com.sun.tools.corba.ee.idl.ExceptionGen {
    /**
     * Public zero-argument constructor.
     **/
    public ExceptionGen() {
        super(true);
    } // ctor

    /**
     *
     **/
    public void generate(Hashtable symbolTable, ExceptionEntry entry, PrintWriter stream) {
        super.generate(symbolTable, entry, stream);
    } // generate
} // class ExceptionGen
