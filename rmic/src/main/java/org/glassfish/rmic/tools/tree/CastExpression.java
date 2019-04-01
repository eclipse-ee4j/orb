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
import java.util.Hashtable;

/**
 * WARNING: The contents of this source file are not part of any supported API. Code that depends on them does so at its
 * own risk: they are subject to change or removal without notice.
 */
public class CastExpression extends BinaryExpression {
    /**
     * constructor
     */
    public CastExpression(long where, Expression left, Expression right) {
        super(CAST, where, left.type, left, right);
    }

    /**
     * Check the expression
     */
    public Vset checkValue(Environment env, Context ctx, Vset vset, Hashtable<Object, Object> exp) {
        type = left.toType(env, ctx);
        vset = right.checkValue(env, ctx, vset, exp);

        if (type.isType(TC_ERROR) || right.type.isType(TC_ERROR)) {
            // An error was already reported
            return vset;
        }

        if (type.equals(right.type)) {
            // The types are already the same
            return vset;
        }

        try {
            if (env.explicitCast(right.type, type)) {
                right = new ConvertExpression(where, type, right);
                return vset;
            }
        } catch (ClassNotFound e) {
            env.error(where, "class.not.found", e.name, opNames[op]);
        }

        // The cast is not allowed
        env.error(where, "invalid.cast", right.type, type);
        return vset;
    }

    /**
     * Check if constant
     */
    public boolean isConstant() {
        if (type.inMask(TM_REFERENCE) && !type.equals(Type.tString)) {
            // must be a primitive type, or String
            return false;
        }
        return right.isConstant();
    }

    /**
     * Inline
     */
    public Expression inline(Environment env, Context ctx) {
        return right.inline(env, ctx);
    }

    public Expression inlineValue(Environment env, Context ctx) {
        return right.inlineValue(env, ctx);
    }

    public int costInline(int thresh, Environment env, Context ctx) {
        if (ctx == null) {
            return 1 + right.costInline(thresh, env, ctx);
        }
        // sourceClass is the current class trying to inline this method
        ClassDefinition sourceClass = ctx.field.getClassDefinition();
        try {
            // We only allow the inlining if the current class can access
            // the casting class
            if (left.type.isType(TC_ARRAY) || sourceClass.permitInlinedAccess(env, env.getClassDeclaration(left.type)))
                return 1 + right.costInline(thresh, env, ctx);
        } catch (ClassNotFound e) {
        }
        return thresh;
    }

    /**
     * Print
     */
    public void print(PrintStream out) {
        out.print("(" + opNames[op] + " ");
        if (type.isType(TC_ERROR)) {
            left.print(out);
        } else {
            out.print(type);
        }
        out.print(" ");
        right.print(out);
        out.print(")");
    }
}
