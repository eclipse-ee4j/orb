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

/**
 * WARNING: The contents of this source file are not part of any
 * supported API.  Code that depends on them does so at its own risk:
 * they are subject to change or removal without notice.
 */
public
class BinaryShiftExpression extends BinaryExpression {
    /**
     * constructor
     */
    public BinaryShiftExpression(int op, long where, Expression left, Expression right) {
        super(op, where, left.type, left, right);
    }

    /**
     * Evaluate the expression
     */
    Expression eval() {
        // The eval code in BinaryExpression.java only works correctly
        // for arithmetic expressions.  For shift expressions, we get cases
        // where the left and right operand may legitimately be of mixed
        // types (long and int).  This is a fix for 4082814.
        if (left.op == LONGVAL && right.op == INTVAL) {
            return eval(((LongExpression)left).value,
                        ((IntExpression)right).value);
        }

        // Delegate the rest of the cases to our parent, so as to minimize
        // impact on existing behavior.
        return super.eval();
    }

    /**
     * Select the type
     */
    void selectType(Environment env, Context ctx, int tm) {
        if (left.type == Type.tLong) {
            type = Type.tLong;
        } else if (left.type.inMask(TM_INTEGER)) {
            type = Type.tInt;
            left = convert(env, ctx, type, left);
        } else {
            type = Type.tError;
        }
        if (right.type.inMask(TM_INTEGER)) {
            right = new ConvertExpression(where, Type.tInt, right);
        } else {
            right = convert(env, ctx, Type.tInt, right);
        }
    }
}
