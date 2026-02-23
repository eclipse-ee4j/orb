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

import org.glassfish.rmic.tools.asm.Assembler;
import org.glassfish.rmic.tools.asm.Label;
import org.glassfish.rmic.tools.java.Environment;
import org.glassfish.rmic.tools.java.Type;

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
