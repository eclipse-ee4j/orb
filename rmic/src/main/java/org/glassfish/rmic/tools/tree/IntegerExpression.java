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

/**
 * WARNING: The contents of this source file are not part of any supported API. Code that depends on them does so at its
 * own risk: they are subject to change or removal without notice.
 */
public class IntegerExpression extends ConstantExpression {
    int value;

    /**
     * Constructor
     */
    IntegerExpression(int op, long where, Type type, int value) {
        super(op, where, type);
        this.value = value;
    }

    /**
     * See if this number fits in the given type.
     */
    public boolean fitsType(Environment env, Context ctx, Type t) {
        if (this.type.isType(TC_CHAR)) {
            // A char constant is not really an int constant,
            // so do not report that 'a' fits in a byte or short,
            // even if its value is in fact 7-bit ascii. See JLS 5.2.
            return super.fitsType(env, ctx, t);
        }
        switch (t.getTypeCode()) {
        case TC_BYTE:
            return value == (byte) value;
        case TC_SHORT:
            return value == (short) value;
        case TC_CHAR:
            return value == (char) value;
        }
        return super.fitsType(env, ctx, t);
    }

    /**
     * Get the value
     */
    public Object getValue() {
        return value;
    }

    /**
     * Check if the expression is equal to a value
     */
    public boolean equals(int i) {
        return value == i;
    }

    /**
     * Check if the expression is equal to its default static value
     */
    public boolean equalsDefault() {
        return value == 0;
    }

    /**
     * Code
     */
    public void codeValue(Environment env, Context ctx, Assembler asm) {
        asm.add(where, opc_ldc, value);
    }
}
