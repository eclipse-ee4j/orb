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
import java.io.PrintStream;

/**
 * WARNING: The contents of this source file are not part of any
 * supported API.  Code that depends on them does so at its own risk:
 * they are subject to change or removal without notice.
 */
public
class InlineReturnStatement extends Statement {
    Expression expr;

    /**
     * Constructor
     */
    public InlineReturnStatement(long where, Expression expr) {
        super(INLINERETURN, where);
        this.expr = expr;
    }

    /**
     * Get the destination context of a break
     */
    Context getDestination(Context ctx) {
        for (; ctx != null ; ctx = ctx.prev) {
            if ((ctx.node != null) && ((ctx.node.op == INLINEMETHOD) || (ctx.node.op == INLINENEWINSTANCE))) {
                return ctx;
            }
        }
        return null;
    }

    /**
     * Inline
     */
    public Statement inline(Environment env, Context ctx) {
        if (expr != null) {
            expr = expr.inlineValue(env, ctx);
        }
        return this;
    }

    /**
     * Create a copy of the statement for method inlining
     */
    public Statement copyInline(Context ctx, boolean valNeeded) {
        InlineReturnStatement s = (InlineReturnStatement)clone();
        if (expr != null) {
            s.expr = expr.copyInline(ctx);
        }
        return s;
    }

    /**
     * The cost of inlining this statement
     */
    public int costInline(int thresh, Environment env, Context ctx) {
        return 1 + ((expr != null) ? expr.costInline(thresh, env, ctx) : 0);
    }

    /**
     * Code
     */
    public void code(Environment env, Context ctx, Assembler asm) {
        if (expr != null) {
            expr.codeValue(env, ctx, asm);
        }
        CodeContext destctx = (CodeContext)getDestination(ctx);
        asm.add(where, opc_goto, destctx.breakLabel);
    }

    /**
     * Print
     */
    public void print(PrintStream out, int indent) {
        super.print(out, indent);
        out.print("inline-return");
        if (expr != null) {
            out.print(" ");
            expr.print(out);
        }
        out.print(";");
    }
}
