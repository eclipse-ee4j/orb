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
 * WARNING: The contents of this source file are not part of any
 * supported API.  Code that depends on them does so at its own risk:
 * they are subject to change or removal without notice.
 */
public
class BooleanExpression extends ConstantExpression {
    boolean value;

    /**
     * Constructor
     */
    public BooleanExpression(long where, boolean value) {
        super(BOOLEANVAL, where, Type.tBoolean);
        this.value = value;
    }

    /**
     * Get the value
     */
    public Object getValue() {
        return value ? 1 : 0;
    }

    /**
     * Check if the expression is equal to a value
     */
    public boolean equals(boolean b) {
        return value == b;
    }


    /**
     * Check if the expression is equal to its default static value
     */
    public boolean equalsDefault() {
        return !value;
    }


    /*
     * Check a "not" expression.
     *
     * cvars is modified so that
     *    cvar.vsTrue indicates variables with a known value if
     *         the expression is true.
     *    cvars.vsFalse indicates variables with a known value if
     *         the expression is false
     *
     * For constant expressions, set the side that corresponds to our
     * already known value to vset.  Set the side that corresponds to the
     * other way to "impossible"
     */

    public void checkCondition(Environment env, Context ctx,
                               Vset vset, Hashtable<Object, Object> exp, ConditionVars cvars) {
        if (value) {
            cvars.vsFalse = Vset.DEAD_END;
            cvars.vsTrue = vset;
        } else {
            cvars.vsFalse = vset;
            cvars.vsTrue = Vset.DEAD_END;
        }
    }


    /**
     * Code
     */
    void codeBranch(Environment env, Context ctx, Assembler asm, Label lbl, boolean whenTrue) {
        if (value == whenTrue) {
            asm.add(where, opc_goto, lbl);
        }
    }
    public void codeValue(Environment env, Context ctx, Assembler asm) {
        asm.add(where, opc_ldc, value ? 1 : 0);
    }

    /**
     * Print
     */
    public void print(PrintStream out) {
        out.print(value ? "true" : "false");
    }
}
