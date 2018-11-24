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
import java.util.Hashtable;

/**
 * WARNING: The contents of this source file are not part of any supported API. Code that depends on them does so at its
 * own risk: they are subject to change or removal without notice.
 */
abstract public class BinaryBitExpression extends BinaryExpression {
    /**
     * constructor
     */
    public BinaryBitExpression(int op, long where, Expression left, Expression right) {
        super(op, where, left.type, left, right);
    }

    /**
     * Select the type
     */
    void selectType(Environment env, Context ctx, int tm) {
        if ((tm & TM_BOOLEAN) != 0) {
            type = Type.tBoolean;
        } else if ((tm & TM_LONG) != 0) {
            type = Type.tLong;
        } else {
            type = Type.tInt;
        }
        left = convert(env, ctx, type, left);
        right = convert(env, ctx, type, right);
    }

    /**
     * Code
     */
    public void codeValue(Environment env, Context ctx, Assembler asm) {
        left.codeValue(env, ctx, asm);
        right.codeValue(env, ctx, asm);
        codeOperation(env, ctx, asm);
    }
}
