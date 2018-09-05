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

/**
 * WARNING: The contents of this source file are not part of any
 * supported API.  Code that depends on them does so at its own risk:
 * they are subject to change or removal without notice.
 */
public
class BinaryEqualityExpression extends BinaryExpression {
    /**
     * constructor
     */
    public BinaryEqualityExpression(int op, long where, Expression left, Expression right) {
        super(op, where, Type.tBoolean, left, right);
    }

    /**
     * Select the type
     */
    void selectType(Environment env, Context ctx, int tm) {
        Type t;
        if ((tm & TM_ERROR) != 0) {
            // who cares.  One of them is an error.
            return;
        } else if ((tm & (TM_CLASS | TM_ARRAY | TM_NULL)) != 0) {
            try {
                if (env.explicitCast(left.type, right.type) ||
                    env.explicitCast(right.type, left.type)) {
                    return;
                }
                env.error(where, "incompatible.type",
                          left.type, left.type, right.type);
            } catch (ClassNotFound e) {
                env.error(where, "class.not.found", e.name, opNames[op]);
            }
            return;
        } else if ((tm & TM_DOUBLE) != 0) {
            t = Type.tDouble;
        } else if ((tm & TM_FLOAT) != 0) {
            t = Type.tFloat;
        } else if ((tm & TM_LONG) != 0) {
            t = Type.tLong;
        } else if ((tm & TM_BOOLEAN) != 0) {
            t = Type.tBoolean;
        } else {
            t = Type.tInt;
        }
        left = convert(env, ctx, t, left);
        right = convert(env, ctx, t, right);
    }
}
