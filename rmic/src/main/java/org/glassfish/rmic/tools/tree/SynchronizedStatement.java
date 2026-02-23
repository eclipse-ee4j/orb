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
import java.util.Hashtable;

import org.glassfish.rmic.tools.asm.Assembler;
import org.glassfish.rmic.tools.asm.CatchData;
import org.glassfish.rmic.tools.asm.Label;
import org.glassfish.rmic.tools.asm.TryData;
import org.glassfish.rmic.tools.java.ClassDefinition;
import org.glassfish.rmic.tools.java.Environment;
import org.glassfish.rmic.tools.java.Type;

/**
 * WARNING: The contents of this source file are not part of any
 * supported API.  Code that depends on them does so at its own risk:
 * they are subject to change or removal without notice.
 */
public
class SynchronizedStatement extends Statement {
    Expression expr;
    Statement body;
    boolean needReturnSlot;   // set by inner return statement

    /**
     * Constructor
     */
    public SynchronizedStatement(long where, Expression expr, Statement body) {
        super(SYNCHRONIZED, where);
        this.expr = expr;
        this.body = body;
    }

    /**
     * Check statement
     */
    Vset check(Environment env, Context ctx, Vset vset, Hashtable<Object, Object> exp) {
        checkLabel(env, ctx);
        CheckContext newctx = new CheckContext(ctx, this);
        vset = reach(env, vset);
        vset = expr.checkValue(env, newctx, vset, exp);
        if (expr.type.equals(Type.tNull)) {
            env.error(expr.where, "synchronized.null");
        }
        expr = convert(env, newctx, Type.tClass(idJavaLangObject), expr);
        vset = body.check(env, newctx, vset, exp);
        return ctx.removeAdditionalVars(vset.join(newctx.vsBreak));
    }

    /**
     * Inline
     */
    public Statement inline(Environment env, Context ctx) {
        if (body != null) {
            body = body.inline(env, ctx);
        }
        expr = expr.inlineValue(env, ctx);
        return this;
    }

    /**
     * Create a copy of the statement for method inlining
     */
    public Statement copyInline(Context ctx, boolean valNeeded) {
        SynchronizedStatement s = (SynchronizedStatement)clone();
        s.expr = expr.copyInline(ctx);
        if (body != null) {
            s.body = body.copyInline(ctx, valNeeded);
        }
        return s;
    }

    /**
     * Compute cost of inlining this statement
     */
    public int costInline(int thresh, Environment env, Context ctx){
        int cost = 1;
        if (expr != null) {
            cost += expr.costInline(thresh, env,ctx);
            if (cost >= thresh) return cost;
        }
        if (body != null) {
            cost += body.costInline(thresh, env,ctx);
        }
        return cost;
    }

    /**
     * Code
     */
    public void code(Environment env, Context ctx, Assembler asm) {
        ClassDefinition clazz = ctx.field.getClassDefinition();
        expr.codeValue(env, ctx, asm);
        ctx = new Context(ctx);

        if (needReturnSlot) {
            Type returnType = ctx.field.getType().getReturnType();
            LocalMember localfield = new LocalMember(0, clazz, 0, returnType,
                                                   idFinallyReturnValue);
            ctx.declare(env, localfield);
            Environment.debugOutput("Assigning return slot to " + localfield.number);
        }

        LocalMember f1 = new LocalMember(where, clazz, 0, Type.tObject, null);
        LocalMember f2 = new LocalMember(where, clazz, 0, Type.tInt, null);
        Integer num1 = ctx.declare(env, f1);
        Integer num2 = ctx.declare(env, f2);

        Label endLabel = new Label();

        TryData td = new TryData();
        td.add(null);

        // lock the object
        asm.add(where, opc_astore, num1);
        asm.add(where, opc_aload, num1);
        asm.add(where, opc_monitorenter);

        // Main body
        CodeContext bodyctx = new CodeContext(ctx, this);
        asm.add(where, opc_try, td);
        if (body != null) {
            body.code(env, bodyctx, asm);
        } else {
            asm.add(where, opc_nop);
        }
        asm.add(bodyctx.breakLabel);
        asm.add(td.getEndLabel());

        // Cleanup afer body
        asm.add(where, opc_aload, num1);
        asm.add(where, opc_monitorexit);
        asm.add(where, opc_goto, endLabel);

        // Catch code
        CatchData cd = td.getCatch(0);
        asm.add(cd.getLabel());
        asm.add(where, opc_aload, num1);
        asm.add(where, opc_monitorexit);
        asm.add(where, opc_athrow);

        // Final body
        asm.add(bodyctx.contLabel);
        asm.add(where, opc_astore, num2);
        asm.add(where, opc_aload, num1);
        asm.add(where, opc_monitorexit);
        asm.add(where, opc_ret, num2);

        asm.add(endLabel);
    }

    /**
     * Print
     */
    public void print(PrintStream out, int indent) {
        super.print(out, indent);
        out.print("synchronized ");
        expr.print(out);
        out.print(" ");
        if (body != null) {
            body.print(out, indent);
        } else {
            out.print("{}");
        }
    }
}
