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

import org.glassfish.rmic.tools.java.Environment;

/**
 * Parenthesized expressions.
 *
 * WARNING: The contents of this source file are not part of any
 * supported API.  Code that depends on them does so at its own risk:
 * they are subject to change or removal without notice.
 */

public
class ExprExpression extends UnaryExpression {
    /**
     * Constructor
     */
    public ExprExpression(long where, Expression right) {
        super(EXPR, where, right.type, right);
    }

    /**
     * Check a condition.  We must pass it on to our unparenthesised form.
     */
    public void checkCondition(Environment env, Context ctx, Vset vset,
                               Hashtable<Object, Object> exp, ConditionVars cvars) {
        right.checkCondition(env, ctx, vset, exp, cvars);
        type = right.type;
    }

    /**
     * Check the expression if it appears as an lvalue.
     * We just pass it on to our unparenthesized subexpression.
     * (Part of fix for 4090372)
     */
    public Vset checkAssignOp(Environment env, Context ctx,
                              Vset vset, Hashtable<Object, Object> exp, Expression outside) {
        vset = right.checkAssignOp(env, ctx, vset, exp, outside);
        type = right.type;
        return vset;
    }

    /**
     * Delegate to our subexpression.
     * (Part of fix for 4090372)
     */
    public FieldUpdater getUpdater(Environment env, Context ctx) {
        return right.getUpdater(env, ctx);
    }

    // Allow (x) = 9;
    //
    // I will hold off on this until I'm sure about it.  Nobody's
    // going to clammer for this one.
    //
    // public Vset checkLHS(Environment env, Context ctx,
    //     Vset vset, Hashtable<Object, Object> exp) {
    //     vset = right.check(env, ctx, vset, exp);
    //     type = right.type;
    //     return vset;
    // }

    public boolean isNull() {
        return right.isNull();
    }

    public boolean isNonNull() {
        return right.isNonNull();
    }

    // Probably not necessary
    public Object getValue() {
        return right.getValue();
    }

    /**
     * Delegate to our subexpression.
     * See the comment in AddExpression#inlineValueSB() for
     * information about this method.
     */
    protected StringBuffer inlineValueSB(Environment env,
                                         Context ctx,
                                         StringBuffer buffer) {
        return right.inlineValueSB(env, ctx, buffer);
    }

    /**
     * Select the type of the expression
     */
    void selectType(Environment env, Context ctx, int tm) {
        type = right.type;
    }

    /**
     * Simplify
     */
    Expression simplify() {
        return right;
    }
}
