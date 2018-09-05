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
import java.io.PrintStream;

/**
 * WARNING: The contents of this source file are not part of any
 * supported API.  Code that depends on them does so at its own risk:
 * they are subject to change or removal without notice.
 */
public
class NaryExpression extends UnaryExpression {
    Expression args[];

    /**
     * Constructor
     */
    NaryExpression(int op, long where, Type type, Expression right, Expression args[]) {
        super(op, where, type, right);
        this.args = args;
    }

    /**
     * Create a copy of the expression for method inlining
     */
    public Expression copyInline(Context ctx) {
        NaryExpression e = (NaryExpression)clone();
        if (right != null) {
            e.right = right.copyInline(ctx);
        }
        e.args = new Expression[args.length];
        for (int i = 0 ; i < args.length ; i++) {
            if (args[i] != null) {
                e.args[i] = args[i].copyInline(ctx);
            }
        }
        return e;
    }

    /**
     * The cost of inlining this expression
     */
    public int costInline(int thresh, Environment env, Context ctx) {
        int cost = 3;
        if (right != null)
            cost += right.costInline(thresh, env, ctx);
        for (int i = 0 ; (i < args.length) && (cost < thresh) ; i++) {
            if (args[i] != null) {
                cost += args[i].costInline(thresh, env, ctx);
            }
        }
        return cost;
    }

    /**
     * Print
     */
    public void print(PrintStream out) {
        out.print("(" + opNames[op] + "#" + hashCode());
        if (right != null) {
            out.print(" ");
            right.print(out);
        }
        for (int i = 0 ; i < args.length ; i++) {
            out.print(" ");
            if (args[i] != null) {
                args[i].print(out);
            } else {
                out.print("<null>");
            }
        }
        out.print(")");
    }
}
