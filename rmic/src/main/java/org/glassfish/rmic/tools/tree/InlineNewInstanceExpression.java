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
import org.glassfish.rmic.tools.asm.Label;
import org.glassfish.rmic.tools.asm.Assembler;
import java.io.PrintStream;
import java.util.Vector;

/**
 * WARNING: The contents of this source file are not part of any supported API. Code that depends on them does so at its
 * own risk: they are subject to change or removal without notice.
 */
public class InlineNewInstanceExpression extends Expression {
    MemberDefinition field;
    Statement body;

    /**
     * Constructor
     */
    InlineNewInstanceExpression(long where, Type type, MemberDefinition field, Statement body) {
        super(INLINENEWINSTANCE, where, type);
        this.field = field;
        this.body = body;
    }

    /**
     * Inline
     */
    public Expression inline(Environment env, Context ctx) {
        return inlineValue(env, ctx);
    }

    public Expression inlineValue(Environment env, Context ctx) {
        if (body != null) {
            LocalMember v = (LocalMember) field.getArguments().elementAt(0);
            Context newctx = new Context(ctx, this);
            newctx.declare(env, v);
            body = body.inline(env, newctx);
        }
        if ((body != null) && (body.op == INLINERETURN)) {
            body = null;
        }
        return this;
    }

    /**
     * Create a copy of the expression for method inlining
     */
    public Expression copyInline(Context ctx) {
        InlineNewInstanceExpression e = (InlineNewInstanceExpression) clone();
        e.body = body.copyInline(ctx, true);
        return e;
    }

    /**
     * Code
     */
    public void code(Environment env, Context ctx, Assembler asm) {
        codeCommon(env, ctx, asm, false);
    }

    public void codeValue(Environment env, Context ctx, Assembler asm) {
        codeCommon(env, ctx, asm, true);
    }

    private void codeCommon(Environment env, Context ctx, Assembler asm, boolean forValue) {
        asm.add(where, opc_new, field.getClassDeclaration());
        if (body != null) {
            LocalMember v = (LocalMember) field.getArguments().elementAt(0);
            CodeContext newctx = new CodeContext(ctx, this);
            newctx.declare(env, v);
            asm.add(where, opc_astore, v.number);
            body.code(env, newctx, asm);
            asm.add(newctx.breakLabel);
            if (forValue) {
                asm.add(where, opc_aload, v.number);
            }
        }
    }

    /**
     * Print
     */
    public void print(PrintStream out) {
        LocalMember v = (LocalMember) field.getArguments().elementAt(0);
        out.println("(" + opNames[op] + "#" + v.hashCode() + "=" + field.hashCode());
        if (body != null) {
            body.print(out, 1);
        } else {
            out.print("<empty>");
        }
        out.print(")");
    }
}
