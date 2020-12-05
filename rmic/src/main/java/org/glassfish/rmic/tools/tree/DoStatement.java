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

import org.glassfish.rmic.tools.java.*;
import org.glassfish.rmic.tools.asm.Assembler;
import org.glassfish.rmic.tools.asm.Label;
import java.io.PrintStream;
import java.util.Hashtable;

/**
 * WARNING: The contents of this source file are not part of any
 * supported API.  Code that depends on them does so at its own risk:
 * they are subject to change or removal without notice.
 */
public
class DoStatement extends Statement {
    Statement body;
    Expression cond;

    /**
     * Constructor
     */
    public DoStatement(long where, Statement body, Expression cond) {
        super(DO, where);
        this.body = body;
        this.cond = cond;
    }

    /**
     * Check statement
     */
    Vset check(Environment env, Context ctx, Vset vset, Hashtable<Object, Object> exp) {
        checkLabel(env,ctx);
        CheckContext newctx = new CheckContext(ctx, this);
        // remember what was unassigned on entry
        Vset vsEntry = vset.copy();
        vset = body.check(env, newctx, reach(env, vset), exp);
        vset = vset.join(newctx.vsContinue);
        // get to the test either by falling through the body, or through
        // a "continue" statement.
        ConditionVars cvars =
            cond.checkCondition(env, newctx, vset, exp);
        cond = convert(env, newctx, Type.tBoolean, cond);
        // make sure the back-branch fits the entry of the loop
        ctx.checkBackBranch(env, this, vsEntry, cvars.vsTrue);
        // exit the loop through the test returning false, or a "break"
        vset = newctx.vsBreak.join(cvars.vsFalse);
        return ctx.removeAdditionalVars(vset);
    }

    /**
     * Inline
     */
    public Statement inline(Environment env, Context ctx) {
        ctx = new Context(ctx, this);
        if (body != null) {
            body = body.inline(env, ctx);
        }
        cond = cond.inlineValue(env, ctx);
        return this;
    }

    /**
     * Create a copy of the statement for method inlining
     */
    public Statement copyInline(Context ctx, boolean valNeeded) {
        DoStatement s = (DoStatement)clone();
        s.cond = cond.copyInline(ctx);
        if (body != null) {
            s.body = body.copyInline(ctx, valNeeded);
        }
        return s;
    }

    /**
     * The cost of inlining this statement
     */
    public int costInline(int thresh, Environment env, Context ctx) {
        return 1 + cond.costInline(thresh, env, ctx)
                + ((body != null) ? body.costInline(thresh, env, ctx) : 0);
    }

    /**
     * Code
     */
    public void code(Environment env, Context ctx, Assembler asm) {
        Label l1 = new Label();
        asm.add(l1);

        CodeContext newctx = new CodeContext(ctx, this);

        if (body != null) {
            body.code(env, newctx, asm);
        }
        asm.add(newctx.contLabel);
        cond.codeBranch(env, newctx, asm, l1, true);
        asm.add(newctx.breakLabel);
    }

    /**
     * Print
     */
    public void print(PrintStream out, int indent) {
        super.print(out, indent);
        out.print("do ");
        body.print(out, indent);
        out.print(" while ");
        cond.print(out);
        out.print(";");
    }
}
