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

import org.glassfish.rmic.tools.java.*;
import org.glassfish.rmic.tools.asm.Assembler;
import org.glassfish.rmic.tools.asm.Label;
import java.util.Hashtable;

/**
 * WARNING: The contents of this source file are not part of any
 * supported API.  Code that depends on them does so at its own risk:
 * they are subject to change or removal without notice.
 */
public
class NotExpression extends UnaryExpression {
    /**
     * Constructor
     */
    public NotExpression(long where, Expression right) {
        super(NOT, where, Type.tBoolean, right);
    }

    /**
     * Select the type of the expression
     */
    void selectType(Environment env, Context ctx, int tm) {
        right = convert(env, ctx, Type.tBoolean, right);
    }

    /*
     * Check a "not" expression.
     *
     * cvars is modified so that
     *    cvar.vsTrue indicates variables with a known value if
     *         the expression is true.
     *    cvars.vsFalse indicates variables with a known value if
     *         the expression is false
     *
     * For "not" expressions, we look at the inside expression, and then
     * swap true and false.
     */

    public void checkCondition(Environment env, Context ctx, Vset vset,
                               Hashtable<Object, Object> exp, ConditionVars cvars) {
        right.checkCondition(env, ctx, vset, exp, cvars);
        right = convert(env, ctx, Type.tBoolean, right);
        // swap true and false
        Vset temp = cvars.vsFalse;
        cvars.vsFalse = cvars.vsTrue;
        cvars.vsTrue = temp;
    }

    /**
     * Evaluate
     */
    Expression eval(boolean a) {
        return new BooleanExpression(where, !a);
    }

    /**
     * Simplify
     */
    Expression simplify() {
        // Check if the expression can be optimized
        switch (right.op) {
          case NOT:
            return ((NotExpression)right).right;

          case EQ:
          case NE:
          case LT:
          case LE:
          case GT:
          case GE:
            break;

          default:
            return this;
        }

        // Can't negate real comparisons
        BinaryExpression bin = (BinaryExpression)right;
        if (bin.left.type.inMask(TM_REAL)) {
            return this;
        }

        // Negate comparison
        switch (right.op) {
          case EQ:
            return new NotEqualExpression(where, bin.left, bin.right);
          case NE:
            return new EqualExpression(where, bin.left, bin.right);
          case LT:
            return new GreaterOrEqualExpression(where, bin.left, bin.right);
          case LE:
            return new GreaterExpression(where, bin.left, bin.right);
          case GT:
            return new LessOrEqualExpression(where, bin.left, bin.right);
          case GE:
            return new LessExpression(where, bin.left, bin.right);
        }
        return this;
    }

    /**
     * Code
     */
    void codeBranch(Environment env, Context ctx, Assembler asm, Label lbl, boolean whenTrue) {
        right.codeBranch(env, ctx, asm, lbl, !whenTrue);
    }

    /**
     * Instead of relying on the default code generation which uses
     * conditional branching, generate a simpler stream using XOR.
     */
    public void codeValue(Environment env, Context ctx, Assembler asm) {
        right.codeValue(env, ctx, asm);
        asm.add(where, opc_ldc, 1);
        asm.add(where, opc_ixor);
    }

}
