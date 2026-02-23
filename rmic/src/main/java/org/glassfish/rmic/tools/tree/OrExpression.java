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

import java.util.Hashtable;

import org.glassfish.rmic.tools.asm.Assembler;
import org.glassfish.rmic.tools.asm.Label;
import org.glassfish.rmic.tools.java.Environment;
import org.glassfish.rmic.tools.java.Type;

/**
 * WARNING: The contents of this source file are not part of any
 * supported API.  Code that depends on them does so at its own risk:
 * they are subject to change or removal without notice.
 */
public
class OrExpression extends BinaryLogicalExpression {
    /**
     * constructor
     */
    public OrExpression(long where, Expression left, Expression right) {
        super(OR, where, left, right);
    }

    /*
     * Check an "or" expression.
     *
     * cvars is modified so that
     *    cvar.vsTrue indicates variables with a known value if
     *        either the left and right hand side isn true
     *    cvars.vsFalse indicates variables with a known value if
     *        both the left or right hand side are false
     */
    public void checkCondition(Environment env, Context ctx, Vset vset,
                               Hashtable<Object, Object> exp, ConditionVars cvars) {
        // Find out when the left side is true/false
        left.checkCondition(env, ctx, vset, exp, cvars);
        left = convert(env, ctx, Type.tBoolean, left);
        Vset vsTrue = cvars.vsTrue.copy();
        Vset vsFalse = cvars.vsFalse.copy();

        // Only look at the right side if the left side is false
        right.checkCondition(env, ctx, vsFalse, exp, cvars);
        right = convert(env, ctx, Type.tBoolean, right);

        // cvars.vsFalse actually reports that both returned false
        // cvars.vsTrue must be set back to either left side or the right
        //     side returning false;
        cvars.vsTrue = cvars.vsTrue.join(vsTrue);
    }

    /**
     * Evaluate
     */
    Expression eval(boolean a, boolean b) {
        return new BooleanExpression(where, a || b);
    }

    /**
     * Simplify
     */
    Expression simplify() {
        if (right.equals(false)) {
            return left;
        }
        if (left.equals(true)) {
            return left;
        }
        if (left.equals(false)) {
            return right;
        }
        if (right.equals(true)) {
            // Preserve effects of left argument.
            return new CommaExpression(where, left, right).simplify();
        }
        return this;
    }

    /**
     * Code
     */
    void codeBranch(Environment env, Context ctx, Assembler asm, Label lbl, boolean whenTrue) {
        if (whenTrue) {
            left.codeBranch(env, ctx, asm, lbl, true);
            right.codeBranch(env, ctx, asm, lbl, true);
        } else {
            Label lbl2 = new Label();
            left.codeBranch(env, ctx, asm, lbl2, true);
            right.codeBranch(env, ctx, asm, lbl, false);
            asm.add(lbl2);
        }
    }
}
