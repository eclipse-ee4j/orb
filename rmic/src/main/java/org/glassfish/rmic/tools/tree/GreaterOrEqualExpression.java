/*
 * Copyright (c) 1994, 2020 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Eclipse Distribution License
 * v. 1.0 which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License v. 2.0 are satisfied: GNU General Public License v2.0
 * w/Classpath exception which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR BSD-3-Clause OR GPL-2.0 WITH
 * Classpath-exception-2.0
 */

package org.glassfish.rmic.tools.tree;

import org.glassfish.rmic.tools.asm.Assembler;
import org.glassfish.rmic.tools.asm.Label;
import org.glassfish.rmic.tools.java.CompilerError;
import org.glassfish.rmic.tools.java.Environment;

/**
 * WARNING: The contents of this source file are not part of any
 * supported API.  Code that depends on them does so at its own risk:
 * they are subject to change or removal without notice.
 */
public
class GreaterOrEqualExpression extends BinaryCompareExpression {
    /**
     * constructor
     */
    public GreaterOrEqualExpression(long where, Expression left, Expression right) {
        super(GE, where, left, right);
    }

    /**
     * Evaluate
     */
    Expression eval(int a, int b) {
        return new BooleanExpression(where, a >= b);
    }
    Expression eval(long a, long b) {
        return new BooleanExpression(where, a >= b);
    }
    Expression eval(float a, float b) {
        return new BooleanExpression(where, a >= b);
    }
    Expression eval(double a, double b) {
        return new BooleanExpression(where, a >= b);
    }

    /**
     * Simplify
     */
    Expression simplify() {
        if (left.isConstant() && !right.isConstant()) {
            return new LessOrEqualExpression(where, right, left);
        }
        return this;
    }

    /**
     * Code
     */
    void codeBranch(Environment env, Context ctx, Assembler asm, Label lbl, boolean whenTrue) {
        left.codeValue(env, ctx, asm);
        switch (left.type.getTypeCode()) {
          case TC_INT:
            if (!right.equals(0)) {
                right.codeValue(env, ctx, asm);
                asm.add(where, whenTrue ? opc_if_icmpge : opc_if_icmplt, lbl, whenTrue);
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
          default:
            throw new CompilerError("Unexpected Type");
        }
        asm.add(where, whenTrue ? opc_ifge : opc_iflt, lbl, whenTrue);
    }
}
