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
public class ThisExpression extends Expression {
    LocalMember field;
    Expression implementation;
    Expression outerArg;

    /**
     * Constructor
     */
    public ThisExpression(long where) {
        super(THIS, where, Type.tObject);
    }

    protected ThisExpression(int op, long where) {
        super(op, where, Type.tObject);
    }

    public ThisExpression(long where, LocalMember field) {
        super(THIS, where, Type.tObject);
        this.field = field;
        field.readcount++;
    }

    public ThisExpression(long where, Context ctx) {
        super(THIS, where, Type.tObject);
        field = ctx.getLocalField(idThis);
        field.readcount++;
    }

    /**
     * Constructor for "x.this()"
     */
    public ThisExpression(long where, Expression outerArg) {
        this(where);
        this.outerArg = outerArg;
    }

    public Expression getImplementation() {
        if (implementation != null)
            return implementation;
        return this;
    }

    /**
     * From the 'this' in an expression of the form outer.this(...), or the 'super' in an expression of the form
     * outer.super(...), return the "outer" expression, or null if there is none.
     */
    public Expression getOuterArg() {
        return outerArg;
    }

    /**
     * Check expression
     */
    public Vset checkValue(Environment env, Context ctx, Vset vset, Hashtable<Object, Object> exp) {
        if (ctx.field.isStatic()) {
            env.error(where, "undef.var", opNames[op]);
            type = Type.tError;
            return vset;
        }
        if (field == null) {
            field = ctx.getLocalField(idThis);
            field.readcount++;
        }
        if (field.scopeNumber < ctx.frameNumber) {
            // get a "this$C" copy via the current object
            implementation = ctx.makeReference(env, field);
        }
        if (!vset.testVar(field.number)) {
            env.error(where, "access.inst.before.super", opNames[op]);
        }
        if (field == null) {
            type = ctx.field.getClassDeclaration().getType();
        } else {
            type = field.getType();
        }
        return vset;
    }

    public boolean isNonNull() {
        return true;
    }

    // A 'ThisExpression' node can never appear on the LHS of an assignment in a correct
    // program, but handle this case anyhow to provide a safe error recovery.

    public FieldUpdater getAssigner(Environment env, Context ctx) {
        return null;
    }

    public FieldUpdater getUpdater(Environment env, Context ctx) {
        return null;
    }

    /**
     * Inline
     */
    public Expression inlineValue(Environment env, Context ctx) {
        if (implementation != null)
            return implementation.inlineValue(env, ctx);
        if (field != null && field.isInlineable(env, false)) {
            Expression e = (Expression) field.getValue(env);
            // System.out.println("INLINE = "+ e + ", THIS");
            if (e != null) {
                e = e.copyInline(ctx);
                e.type = type; // in case op==SUPER
                return e;
            }
        }
        return this;
    }

    /**
     * Create a copy of the expression for method inlining
     */
    public Expression copyInline(Context ctx) {
        if (implementation != null)
            return implementation.copyInline(ctx);
        ThisExpression e = (ThisExpression) clone();
        if (field == null) {
            // The expression is copied into the context of a method
            e.field = ctx.getLocalField(idThis);
            e.field.readcount++;
        } else {
            e.field = field.getCurrentInlineCopy(ctx);
        }
        if (outerArg != null) {
            e.outerArg = outerArg.copyInline(ctx);
        }
        return e;
    }

    /**
     * Code
     */
    public void codeValue(Environment env, Context ctx, Assembler asm) {
        asm.add(where, opc_aload, field.number);
    }

    /**
     * Print
     */
    public void print(PrintStream out) {
        if (outerArg != null) {
            out.print("(outer=");
            outerArg.print(out);
            out.print(" ");
        }
        String pfx = (field == null) ? "" : field.getClassDefinition().getName().getFlatName().getName() + ".";
        pfx += opNames[op];
        out.print(pfx + "#" + ((field != null) ? field.hashCode() : 0));
        if (outerArg != null)
            out.print(")");
    }
}
