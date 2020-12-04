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
import java.util.Hashtable;

/**
 * WARNING: The contents of this source file are not part of any
 * supported API.  Code that depends on them does so at its own risk:
 * they are subject to change or removal without notice.
 */
public
class CommaExpression extends BinaryExpression {
    /**
     * constructor
     */
    public CommaExpression(long where, Expression left, Expression right) {
        super(COMMA, where, (right != null) ? right.type : Type.tVoid, left, right);
    }

    /**
     * Check void expression
     */
    public Vset check(Environment env, Context ctx, Vset vset, Hashtable<Object, Object> exp) {
        vset = left.check(env, ctx, vset, exp);
        vset = right.check(env, ctx, vset, exp);
        return vset;
    }

    /**
     * Select the type
     */
    void selectType(Environment env, Context ctx, int tm) {
        type = right.type;
    }

    /**
     * Simplify
     */
    Expression simplify() {
        if (left == null) {
            return right;
        }
        if (right == null) {
            return left;
        }
        return this;
    }

    /**
     * Inline
     */
    public Expression inline(Environment env, Context ctx) {
        if (left != null) {
            left = left.inline(env, ctx);
        }
        if (right != null) {
            right = right.inline(env, ctx);
        }
        return simplify();
    }
    public Expression inlineValue(Environment env, Context ctx) {
        if (left != null) {
            left = left.inline(env, ctx);
        }
        if (right != null) {
            right = right.inlineValue(env, ctx);
        }
        return simplify();
    }

    /**
     * Code
     */
    int codeLValue(Environment env, Context ctx, Assembler asm) {
        if (right == null) {
            // throw an appropriate error
            return super.codeLValue(env, ctx, asm);
        } else {
            // Fully code the left-hand side.  Do the LValue part of the
            // right-hand side now.  The remainder will be done by codeLoad or
            // codeStore
            if (left != null) {
                left.code(env, ctx, asm);
            }
            return right.codeLValue(env, ctx, asm);
        }
    }

    void codeLoad(Environment env, Context ctx, Assembler asm) {
        // The left-hand part has already been handled by codeLValue.

        if (right == null) {
            // throw an appropriate error
            super.codeLoad(env, ctx, asm);
        } else {
            right.codeLoad(env, ctx, asm);
        }
    }

    void codeStore(Environment env, Context ctx, Assembler asm) {
        // The left-hand part has already been handled by codeLValue.
        if (right == null) {
            // throw an appropriate error
            super.codeStore(env, ctx, asm);
        } else {
            right.codeStore(env, ctx, asm);
        }
    }

    public void codeValue(Environment env, Context ctx, Assembler asm) {
        if (left != null) {
            left.code(env, ctx, asm);
        }
        right.codeValue(env, ctx, asm);
    }
    public void code(Environment env, Context ctx, Assembler asm) {
        if (left != null) {
            left.code(env, ctx, asm);
        }
        if (right != null) {
            right.code(env, ctx, asm);
        }
    }
}
