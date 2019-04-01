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
import org.glassfish.rmic.tools.asm.Label;

/**
 * WARNING: The contents of this source file are not part of any supported API. Code that depends on them does so at its
 * own risk: they are subject to change or removal without notice.
 */
public class NotEqualExpression extends BinaryEqualityExpression {
    /**
     * constructor
     */
    public NotEqualExpression(long where, Expression left, Expression right) {
        super(NE, where, left, right);
    }

    /**
     * Evaluate
     */
    Expression eval(int a, int b) {
        return new BooleanExpression(where, a != b);
    }

    Expression eval(long a, long b) {
        return new BooleanExpression(where, a != b);
    }

    Expression eval(float a, float b) {
        return new BooleanExpression(where, a != b);
    }

    Expression eval(double a, double b) {
        return new BooleanExpression(where, a != b);
    }

    Expression eval(boolean a, boolean b) {
        return new BooleanExpression(where, a != b);
    }

    /**
     * Simplify
     */
    Expression simplify() {
        if (left.isConstant() && !right.isConstant()) {
            return new NotEqualExpression(where, right, left);
        }
        return this;
    }

    /**
     * Code
     */
    void codeBranch(Environment env, Context ctx, Assembler asm, Label lbl, boolean whenTrue) {
        left.codeValue(env, ctx, asm);
        switch (left.type.getTypeCode()) {
        case TC_BOOLEAN:
        case TC_INT:
            if (!right.equals(0)) {
                right.codeValue(env, ctx, asm);
                asm.add(where, whenTrue ? opc_if_icmpne : opc_if_icmpeq, lbl, whenTrue);
                return;
            }
            break;
        case TC_LONG:
            right.codeValue(env, ctx, asm);
            asm.add(where, opc_lcmp);
            break;
        case TC_FLOAT:
            right.codeValue(env, ctx, asm);
            asm.add(where, opc_fcmpl);
            break;
        case TC_DOUBLE:
            right.codeValue(env, ctx, asm);
            asm.add(where, opc_dcmpl);
            break;
        case TC_ARRAY:
        case TC_CLASS:
        case TC_NULL:
            if (right.equals(0)) {
                asm.add(where, whenTrue ? opc_ifnonnull : opc_ifnull, lbl, whenTrue);
            } else {
                right.codeValue(env, ctx, asm);
                asm.add(where, whenTrue ? opc_if_acmpne : opc_if_acmpeq, lbl, whenTrue);
            }
            return;
        default:
            throw new CompilerError("Unexpected Type");
        }
        asm.add(where, whenTrue ? opc_ifne : opc_ifeq, lbl, whenTrue);
    }
}
