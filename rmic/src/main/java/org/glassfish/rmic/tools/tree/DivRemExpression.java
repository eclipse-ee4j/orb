/*
 * Copyright (c) 1995, 2020 Oracle and/or its affiliates. All rights reserved.
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

import org.glassfish.rmic.tools.java.Environment;

/**
 * WARNING: The contents of this source file are not part of any
 * supported API.  Code that depends on them does so at its own risk:
 * they are subject to change or removal without notice.
 */
abstract public
class DivRemExpression extends BinaryArithmeticExpression {
    /**
     * constructor
     */
    public DivRemExpression(int op, long where, Expression left, Expression right) {
        super(op, where, left, right);
    }

    /**
     * Inline
     */
    public Expression inline(Environment env, Context ctx) {
        // Do not toss out integer divisions or remainders since they
        // can cause a division by zero.
        if (type.inMask(TM_INTEGER)) {
            right = right.inlineValue(env, ctx);
            if (right.isConstant() && !right.equals(0)) {
                // We know the division can be elided
                left = left.inline(env, ctx);
                return left;
            } else {
                left = left.inlineValue(env, ctx);
                try {
                    return eval().simplify();
                } catch (ArithmeticException e) {
                    env.error(where, "arithmetic.exception");
                    return this;
                }
            }
        } else {
            // float & double divisions don't cause arithmetic errors
            return super.inline(env, ctx);
        }
    }
}
