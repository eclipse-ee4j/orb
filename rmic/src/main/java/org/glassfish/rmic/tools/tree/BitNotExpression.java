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
public class BitNotExpression extends UnaryExpression {
    /**
     * Constructor
     */
    public BitNotExpression(long where, Expression right) {
        super(BITNOT, where, right.type, right);
    }

    /**
     * Select the type of the expression
     */
    void selectType(Environment env, Context ctx, int tm) {
        if ((tm & TM_LONG) != 0) {
            type = Type.tLong;
        } else {
            type = Type.tInt;
        }
        right = convert(env, ctx, type, right);
    }

    /**
     * Evaluate
     */
    Expression eval(int a) {
        return new IntExpression(where, ~a);
    }

    Expression eval(long a) {
        return new LongExpression(where, ~a);
    }

    /**
     * Simplify
     */
    Expression simplify() {
        if (right.op == BITNOT) {
            return ((BitNotExpression) right).right;
        }
        return this;
    }

    /**
     * Code
     */
    public void codeValue(Environment env, Context ctx, Assembler asm) {
        right.codeValue(env, ctx, asm);
        if (type.isType(TC_INT)) {
            asm.add(where, opc_ldc, -1);
            asm.add(where, opc_ixor);
        } else {
            asm.add(where, opc_ldc2_w, -1L);
            asm.add(where, opc_lxor);
        }
    }
}
