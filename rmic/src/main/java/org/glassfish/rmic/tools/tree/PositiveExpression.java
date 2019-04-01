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

/**
 * WARNING: The contents of this source file are not part of any supported API. Code that depends on them does so at its
 * own risk: they are subject to change or removal without notice.
 */
public class PositiveExpression extends UnaryExpression {
    /**
     * Constructor
     */
    public PositiveExpression(long where, Expression right) {
        super(POS, where, right.type, right);
    }

    /**
     * Select the type of the expression
     */
    void selectType(Environment env, Context ctx, int tm) {
        if ((tm & TM_DOUBLE) != 0) {
            type = Type.tDouble;
        } else if ((tm & TM_FLOAT) != 0) {
            type = Type.tFloat;
        } else if ((tm & TM_LONG) != 0) {
            type = Type.tLong;
        } else {
            type = Type.tInt;
        }
        right = convert(env, ctx, type, right);
    }

    /**
     * Simplify
     */
    Expression simplify() {
        return right;
    }
}
