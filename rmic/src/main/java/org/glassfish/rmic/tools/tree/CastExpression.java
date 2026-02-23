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

import org.glassfish.rmic.tools.java.ClassDefinition;
import org.glassfish.rmic.tools.java.ClassNotFound;
import org.glassfish.rmic.tools.java.Environment;
import org.glassfish.rmic.tools.java.Type;

/**
 * WARNING: The contents of this source file are not part of any
 * supported API.  Code that depends on them does so at its own risk:
 * they are subject to change or removal without notice.
 */
public
class CastExpression extends BinaryExpression {
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
            if (left.type.isType(TC_ARRAY) ||
                 sourceClass.permitInlinedAccess(env,
                                  env.getClassDeclaration(left.type)))
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
