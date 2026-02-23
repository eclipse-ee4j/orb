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

package org.glassfish.rmic.tools.asm;

import java.io.DataOutputStream;
import java.io.IOException;

import org.glassfish.rmic.tools.java.Environment;

/**
 * A numeric constant pool item. Can either be integer, float, long or double.
 *
 * WARNING: The contents of this source file are not part of any
 * supported API.  Code that depends on them does so at its own risk:
 * they are subject to change or removal without notice.
 */
final
class NumberConstantData extends ConstantPoolData {
    Number num;

    /**
     * Constructor
     */
    NumberConstantData(ConstantPool tab, Number num) {
        this.num = num;
    }

    /**
     * Write the constant to the output stream
     */
    void write(Environment env, DataOutputStream out, ConstantPool tab) throws IOException {
        if (num instanceof Integer) {
            out.writeByte(CONSTANT_INTEGER);
            out.writeInt(num.intValue());
        } else if (num instanceof Long) {
            out.writeByte(CONSTANT_LONG);
            out.writeLong(num.longValue());
        } else if (num instanceof Float) {
            out.writeByte(CONSTANT_FLOAT);
            out.writeFloat(num.floatValue());
        } else if (num instanceof Double) {
            out.writeByte(CONSTANT_DOUBLE);
            out.writeDouble(num.doubleValue());
        }
    }
    /**
     * Return the order of the constant
     */
    int order() {
        return (width() == 1) ? 0 : 3;
    }

    /**
     * Return the number of entries that it takes up in the constant pool
     */
    int width() {
        return ((num instanceof Double) || (num instanceof Long)) ? 2 : 1;
    }
}
