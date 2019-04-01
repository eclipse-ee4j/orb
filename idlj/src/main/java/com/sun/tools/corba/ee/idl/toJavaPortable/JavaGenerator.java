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

import com.sun.tools.corba.ee.idl.SymtabEntry;

/**
 *
 **/
public interface JavaGenerator {
    // The helper methods print the specific helper method.
    // The helper read/write methods call the read/write methods.

    int helperType(int index, String indent, TCOffsets tcoffsets, String name, SymtabEntry entry, PrintWriter stream);

    void helperRead(String entryName, SymtabEntry entry, PrintWriter stream);

    void helperWrite(SymtabEntry entry, PrintWriter stream);

    // The read/write methods print the streaming of the type.
    // This printed code is found in the helper method but it is only
    // that code that is concerned with streaming itself.

    int read(int index, String indent, String name, SymtabEntry entry, PrintWriter stream);

    int write(int index, String indent, String name, SymtabEntry entry, PrintWriter stream);

    int type(int index, String indent, TCOffsets tcoffsets, String name, SymtabEntry entry, PrintWriter stream);

} // interface JavaGenerator
