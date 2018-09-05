/*
 * Copyright (c) 1994, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.rmic.tools.asm;

import org.glassfish.rmic.tools.java.*;
import org.glassfish.rmic.tools.tree.StringExpression;
import java.io.IOException;
import java.io.DataOutputStream;

/**
 * This is a string expression constant. This constant
 * represents an Java string constant.
 *
 * WARNING: The contents of this source file are not part of any
 * supported API.  Code that depends on them does so at its own risk:
 * they are subject to change or removal without notice.
 */
final
class StringExpressionConstantData extends ConstantPoolData {
    StringExpression str;

    /**
     * Constructor
     */
    StringExpressionConstantData(ConstantPool tab, StringExpression str) {
        this.str = str;
        tab.put(str.getValue());
    }

    /**
     * Write the constant to the output stream
     */
    void write(Environment env, DataOutputStream out, ConstantPool tab) throws IOException {
        out.writeByte(CONSTANT_STRING);
        out.writeShort(tab.index(str.getValue()));
    }

    /**
     * Return the order of the constant
     */
    int order() {
        return 0;
    }

    /**
     * toString
     */
    public String toString() {
        return "StringExpressionConstantData[" + str.getValue() + "]=" + str.getValue().hashCode();
    }
}
