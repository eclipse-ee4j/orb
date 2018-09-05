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
import java.io.PrintStream;

/**
 * WARNING: The contents of this source file are not part of any
 * supported API.  Code that depends on them does so at its own risk:
 * they are subject to change or removal without notice.
 */
public
class IntExpression extends IntegerExpression {
    /**
     * Constructor
     */
    public IntExpression(long where, int value) {
        super(INTVAL, where, Type.tInt, value);
    }

    /**
     * Equality, this is needed so that switch statements
     * can put IntExpressions in a hashtable
     */
    public boolean equals(Object obj) {
        if ((obj != null) && (obj instanceof IntExpression)) {
            return value == ((IntExpression)obj).value;
        }
        return false;
    }

    /**
     * Hashcode, this is needed so that switch statements
     * can put IntExpressions in a hashtable
     */
    public int hashCode() {
        return value;
    }

    /**
     * Print
     */
    public void print(PrintStream out) {
        out.print(value);
    }
}
