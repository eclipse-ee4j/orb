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
import java.io.PrintStream;
import java.util.Hashtable;

/**
 * WARNING: The contents of this source file are not part of any supported API. Code that depends on them does so at its
 * own risk: they are subject to change or removal without notice.
 */
public class ThrowStatement extends Statement {
    Expression expr;

    /**
     * Constructor
     */
    public ThrowStatement(long where, Expression expr) {
        super(THROW, where);
        this.expr = expr;
    }

    /**
     * Check statement
     */
    Vset check(Environment env, Context ctx, Vset vset, Hashtable<Object, Object> exp) {
        checkLabel(env, ctx);
        try {
            vset = reach(env, vset);
            expr.checkValue(env, ctx, vset, exp);
            if (expr.type.isType(TC_CLASS)) {
                ClassDeclaration c = env.getClassDeclaration(expr.type);
                if (exp.get(c) == null) {
                    exp.put(c, this);
                }
                ClassDefinition def = c.getClassDefinition(env);
                ClassDeclaration throwable = env.getClassDeclaration(idJavaLangThrowable);
                if (!def.subClassOf(env, throwable)) {
                    env.error(where, "throw.not.throwable", def);
                }
                expr = convert(env, ctx, Type.tObject, expr);
            } else if (!expr.type.isType(TC_ERROR)) {
                env.error(expr.where, "throw.not.throwable", expr.type);
            }
        } catch (ClassNotFound e) {
            env.error(where, "class.not.found", e.name, opNames[op]);
        }
        CheckContext exitctx = ctx.getTryExitContext();
        if (exitctx != null) {
            exitctx.vsTryExit = exitctx.vsTryExit.join(vset);
        }
        return DEAD_END;
    }

    /**
     * Inline
     */
    public Statement inline(Environment env, Context ctx) {
        expr = expr.inlineValue(env, ctx);
        return this;
    }

    /**
     * Create a copy of the statement for method inlining
     */
    public Statement copyInline(Context ctx, boolean valNeeded) {
        ThrowStatement s = (ThrowStatement) clone();
        s.expr = expr.copyInline(ctx);
        return s;
    }

    /**
     * The cost of inlining this statement
     */
    public int costInline(int thresh, Environment env, Context ctx) {
        return 1 + expr.costInline(thresh, env, ctx);
    }

    /**
     * Code
     */
    public void code(Environment env, Context ctx, Assembler asm) {
        expr.codeValue(env, ctx, asm);
        asm.add(where, opc_athrow);
    }

    /**
     * Print
     */
    public void print(PrintStream out, int indent) {
        super.print(out, indent);
        out.print("throw ");
        expr.print(out);
        out.print(":");
    }
}
