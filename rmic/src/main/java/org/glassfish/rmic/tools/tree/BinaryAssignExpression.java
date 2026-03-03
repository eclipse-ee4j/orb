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
 * WARNING: The contents of this source file are not part of any
 * supported API.  Code that depends on them does so at its own risk:
 * they are subject to change or removal without notice.
 */
public
class BinaryAssignExpression extends BinaryExpression {
    Expression implementation;

    /**
     * Constructor
     */
    BinaryAssignExpression(int op, long where, Expression left, Expression right) {
        super(op, where, left.type, left, right);
    }

    public Expression getImplementation() {
        if (implementation != null)
            return implementation;
        return this;
    }

    /**
     * Order the expression based on precedence
     */
    public Expression order() {
        if (precedence() >= left.precedence()) {
            UnaryExpression e = (UnaryExpression)left;
            left = e.right;
            e.right = order();
            return e;
        }
        return this;
    }

    /**
     * Check void expression
     */
    public Vset check(Environment env, Context ctx, Vset vset, Hashtable<Object,Object> exp) {
        return checkValue(env, ctx, vset, exp);
    }

    /**
     * Inline
     */
    public Expression inline(Environment env, Context ctx) {
        if (implementation != null)
            return implementation.inline(env, ctx);
        return inlineValue(env, ctx);
    }
    public Expression inlineValue(Environment env, Context ctx) {
        if (implementation != null)
            return implementation.inlineValue(env, ctx);
        left = left.inlineLHS(env, ctx);
        right = right.inlineValue(env, ctx);
        return this;
    }

    public Expression copyInline(Context ctx) {
        if (implementation != null)
            return implementation.copyInline(ctx);
        return super.copyInline(ctx);
    }

    public int costInline(int thresh, Environment env, Context ctx) {
        if (implementation != null)
            return implementation.costInline(thresh, env, ctx);
        return super.costInline(thresh, env, ctx);
    }
}
