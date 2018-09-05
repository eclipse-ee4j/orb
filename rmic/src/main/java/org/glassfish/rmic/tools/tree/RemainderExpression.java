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
 * WARNING: The contents of this source file are not part of any
 * supported API.  Code that depends on them does so at its own risk:
 * they are subject to change or removal without notice.
 */
public
class RemainderExpression extends DivRemExpression {
    /**
     * constructor
     */
    public RemainderExpression(long where, Expression left, Expression right) {
        super(REM, where, left, right);
    }

    /**
     * Evaluate
     */
    Expression eval(int a, int b) {
        return new IntExpression(where, a % b);
    }
    Expression eval(long a, long b) {
        return new LongExpression(where, a % b);
    }
    Expression eval(float a, float b) {
        return new FloatExpression(where, a % b);
    }
    Expression eval(double a, double b) {
        return new DoubleExpression(where, a % b);
    }

    /**
     * Code
     */
    void codeOperation(Environment env, Context ctx, Assembler asm) {
        asm.add(where, opc_irem + type.getTypeCodeOffset());
    }
}
