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
class AndExpression extends BinaryLogicalExpression {
    /**
     * constructor
     */
    public AndExpression(long where, Expression left, Expression right) {
        super(AND, where, left, right);
    }

    /*
     * Check an "and" expression.
     *
     * cvars is modified so that
     *    cvar.vsTrue indicates variables with a known value if
     *        both the left and right hand side are true
     *    cvars.vsFalse indicates variables with a known value
     *        either the left or right hand side is false
     */
    public void checkCondition(Environment env, Context ctx, Vset vset,
                               Hashtable<Object, Object> exp, ConditionVars cvars) {
        // Find out when the left side is true/false
        left.checkCondition(env, ctx, vset, exp, cvars);
        left = convert(env, ctx, Type.tBoolean, left);
        Vset vsTrue = cvars.vsTrue.copy();
        Vset vsFalse = cvars.vsFalse.copy();

        // Only look at the right side if the left side is true
        right.checkCondition(env, ctx, vsTrue, exp, cvars);
        right = convert(env, ctx, Type.tBoolean, right);

        // cvars.vsTrue already reports when both returned true
        // cvars.vsFalse must be set to either the left or right side
        //    returning false
        cvars.vsFalse = cvars.vsFalse.join(vsFalse);
    }

    /**
     * Evaluate
     */
    Expression eval(boolean a, boolean b) {
        return new BooleanExpression(where, a && b);
    }

    /**
     * Simplify
     */
    Expression simplify() {
        if (left.equals(true)) {
            return right;
        }
        if (right.equals(false)) {
            // Preserve effects of left argument.
            return new CommaExpression(where, left, right).simplify();
        }
        if (right.equals(true)) {
            return left;
        }
        if (left.equals(false)) {
            return left;
        }
        return this;
    }

    /**
     * Code
     */
    void codeBranch(Environment env, Context ctx, Assembler asm, Label lbl, boolean whenTrue) {
        if (whenTrue) {
            Label lbl2 = new Label();
            left.codeBranch(env, ctx, asm, lbl2, false);
            right.codeBranch(env, ctx, asm, lbl, true);
            asm.add(lbl2);
        } else {
            left.codeBranch(env, ctx, asm, lbl, false);
            right.codeBranch(env, ctx, asm, lbl, false);
        }
    }
}
