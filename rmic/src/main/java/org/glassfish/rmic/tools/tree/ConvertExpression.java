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
import java.util.Hashtable;

/**
 * WARNING: The contents of this source file are not part of any
 * supported API.  Code that depends on them does so at its own risk:
 * they are subject to change or removal without notice.
 */
public
class ConvertExpression extends UnaryExpression {
    /**
     * Constructor
     */
    public ConvertExpression(long where, Type type, Expression right) {
        super(CONVERT, where, type, right);
    }

    /**
     * Check the value
     */
    public Vset checkValue(Environment env, Context ctx, Vset vset, Hashtable<Object, Object> exp) {
        return right.checkValue(env, ctx, vset, exp);
    }

    /**
     * Simplify
     */
    Expression simplify() {
        switch (right.op) {
          case BYTEVAL:
          case CHARVAL:
          case SHORTVAL:
          case INTVAL: {
            int value = ((IntegerExpression)right).value;
            switch (type.getTypeCode()) {
              case TC_BYTE:     return new ByteExpression(right.where, (byte)value);
              case TC_CHAR:     return new CharExpression(right.where, (char)value);
              case TC_SHORT:    return new ShortExpression(right.where, (short)value);
              case TC_INT:      return new IntExpression(right.where, value);
              case TC_LONG:     return new LongExpression(right.where, (long)value);
              case TC_FLOAT:    return new FloatExpression(right.where, (float)value);
              case TC_DOUBLE:   return new DoubleExpression(right.where, (double)value);
            }
            break;
          }
          case LONGVAL: {
            long value = ((LongExpression)right).value;
            switch (type.getTypeCode()) {
              case TC_BYTE:     return new ByteExpression(right.where, (byte)value);
              case TC_CHAR:     return new CharExpression(right.where, (char)value);
              case TC_SHORT:    return new ShortExpression(right.where, (short)value);
              case TC_INT:      return new IntExpression(right.where, (int)value);
              case TC_FLOAT:    return new FloatExpression(right.where, (float)value);
              case TC_DOUBLE:   return new DoubleExpression(right.where, (double)value);
            }
            break;
          }
          case FLOATVAL: {
            float value = ((FloatExpression)right).value;
            switch (type.getTypeCode()) {
              case TC_BYTE:     return new ByteExpression(right.where, (byte)value);
              case TC_CHAR:     return new CharExpression(right.where, (char)value);
              case TC_SHORT:    return new ShortExpression(right.where, (short)value);
              case TC_INT:      return new IntExpression(right.where, (int)value);
              case TC_LONG:     return new LongExpression(right.where, (long)value);
              case TC_DOUBLE:   return new DoubleExpression(right.where, (double)value);
            }
            break;
          }
          case DOUBLEVAL: {
            double value = ((DoubleExpression)right).value;
            switch (type.getTypeCode()) {
              case TC_BYTE:     return new ByteExpression(right.where, (byte)value);
              case TC_CHAR:     return new CharExpression(right.where, (char)value);
              case TC_SHORT:    return new ShortExpression(right.where, (short)value);
              case TC_INT:      return new IntExpression(right.where, (int)value);
              case TC_LONG:     return new LongExpression(right.where, (long)value);
              case TC_FLOAT:    return new FloatExpression(right.where, (float)value);
            }
            break;
          }
        }
        return this;
    }

    /**
     * Check if the expression is equal to a value
     */
    public boolean equals(int i) {
        return right.equals(i);
    }
    public boolean equals(boolean b) {
        return right.equals(b);
    }

    /**
     * Inline
     */
    public Expression inline(Environment env, Context ctx) {
        // super.inline throws away the op.
        // This is sometimes incorrect, since casts can have side effects.
        if (right.type.inMask(TM_REFERENCE) && type.inMask(TM_REFERENCE)) {
            try {
                if (!env.implicitCast(right.type, type))
                    return inlineValue(env, ctx);
            } catch (ClassNotFound e) {
                throw new CompilerError(e);
            }
        }
        return super.inline(env, ctx);
    }

    /**
     * Code
     */
    public void codeValue(Environment env, Context ctx, Assembler asm) {
        right.codeValue(env, ctx, asm);
        codeConversion(env, ctx, asm, right.type, type);
    }

    /**
     * Print
     */
    public void print(PrintStream out) {
        out.print("(" + opNames[op] + " " + type.toString() + " ");
        right.print(out);
        out.print(")");
    }
}
