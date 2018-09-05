/*
 * Copyright (c) 1994, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.rmic.tools.tree;

import org.glassfish.rmic.tools.java.*;
import org.glassfish.rmic.tools.asm.Assembler;
import java.io.PrintStream;

/**
 * WARNING: The contents of this source file are not part of any
 * supported API.  Code that depends on them does so at its own risk:
 * they are subject to change or removal without notice.
 */
public
class NullExpression extends ConstantExpression {
    /**
     * Constructor
     */
    public NullExpression(long where) {
        super(NULL, where, Type.tNull);
    }

    /**
     * Check if the expression is equal to a value
     */
    public boolean equals(int i) {
        return i == 0;
    }

    public boolean isNull() {
        return true;
    }

    /**
     * Code
     */
    public void codeValue(Environment env, Context ctx, Assembler asm) {
        asm.add(where, opc_aconst_null);
    }

    /**
     * Print
     */
    public void print(PrintStream out) {
        out.print("null");
    }
}
