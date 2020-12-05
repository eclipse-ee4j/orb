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
import java.io.PrintStream;

/**
 * WARNING: The contents of this source file are not part of any
 * supported API.  Code that depends on them does so at its own risk:
 * they are subject to change or removal without notice.
 */
public
class StringExpression extends ConstantExpression {
    String value;

    /**
     * Constructor
     */
    public StringExpression(long where, String value) {
        super(STRINGVAL, where, Type.tString);
        this.value = value;
    }

    public boolean equals(String s) {
        return value.equals(s);
    }
    public boolean isNonNull() {
        return true;            // string literal is never null
    }

    /**
     * Code
     */
    public void codeValue(Environment env, Context ctx, Assembler asm) {
        asm.add(where, opc_ldc, this);
    }

    /**
     * Get the value
     */
    public Object getValue() {
        return value;
    }

    /**
     * Hashcode
     */
    public int hashCode() {
        return value.hashCode() ^ 3213;
    }

    /**
     * Equality
     */
    public boolean equals(Object obj) {
        if ((obj != null) && (obj instanceof StringExpression)) {
            return value.equals(((StringExpression)obj).value);
        }
        return false;
    }

    /**
     * Print
     */
    public void print(PrintStream out) {
        out.print("\"" + value + "\"");
    }
}
