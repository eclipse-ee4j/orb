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
import java.util.Hashtable;

/**
 * WARNING: The contents of this source file are not part of any supported API. Code that depends on them does so at its
 * own risk: they are subject to change or removal without notice.
 */
public class BinaryAssignExpression extends BinaryExpression {
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
            UnaryExpression e = (UnaryExpression) left;
            left = e.right;
            e.right = order();
            return e;
        }
        return this;
    }

    /**
     * Check void expression
     */
    public Vset check(Environment env, Context ctx, Vset vset, Hashtable<Object, Object> exp) {
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
