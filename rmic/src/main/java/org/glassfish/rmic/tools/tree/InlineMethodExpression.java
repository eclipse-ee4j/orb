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

import java.io.PrintStream;

import org.glassfish.rmic.tools.asm.Assembler;
import org.glassfish.rmic.tools.java.CompilerError;
import org.glassfish.rmic.tools.java.Environment;
import org.glassfish.rmic.tools.java.MemberDefinition;
import org.glassfish.rmic.tools.java.Type;

/**
 * WARNING: The contents of this source file are not part of any
 * supported API.  Code that depends on them does so at its own risk:
 * they are subject to change or removal without notice.
 */
public
class InlineMethodExpression extends Expression {
    MemberDefinition field;
    Statement body;

    /**
     * Constructor
     */
    InlineMethodExpression(long where, Type type, MemberDefinition field, Statement body) {
        super(INLINEMETHOD, where, type);
        this.field = field;
        this.body = body;
    }
    /**
     * Inline
     */
    public Expression inline(Environment env, Context ctx) {
        body = body.inline(env, new Context(ctx, this));
        if (body == null) {
            return null;
        } else if (body.op == INLINERETURN) {
            Expression expr = ((InlineReturnStatement)body).expr;
            if (expr != null && type.isType(TC_VOID)) {
                throw new CompilerError("value on inline-void return");
            }
            return expr;
        } else {
            return this;
        }
    }
    public Expression inlineValue(Environment env, Context ctx) {
        // When this node was constructed, "copyInline" walked the body
        // with a "valNeeded" flag which made all returns either void
        // or value-bearing.  The type of this node reflects that
        // earlier choice.  The present inline/inlineValue distinction
        // is ignored.
        return inline(env, ctx);
    }

    /**
     * Create a copy of the expression for method inlining
     */
    public Expression copyInline(Context ctx) {
        InlineMethodExpression e = (InlineMethodExpression)clone();
        if (body != null) {
            e.body = body.copyInline(ctx, true);
        }
        return e;
    }

    /**
     * Code
     */
    public void code(Environment env, Context ctx, Assembler asm) {
        // pop the result if there is any (usually, type is already void)
        super.code(env, ctx, asm);
    }
    public void codeValue(Environment env, Context ctx, Assembler asm) {
        CodeContext newctx = new CodeContext(ctx, this);
        body.code(env, newctx, asm);
        asm.add(newctx.breakLabel);
    }

    /**
     * Print
     */
    public void print(PrintStream out) {
        out.print("(" + opNames[op] + "\n");
        body.print(out, 1);
        out.print(")");
    }
}
