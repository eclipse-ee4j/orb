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
import org.glassfish.rmic.tools.asm.*;
import java.io.PrintStream;
import java.util.Hashtable;

/**
 * WARNING: The contents of this source file are not part of any supported API. Code that depends on them does so at its
 * own risk: they are subject to change or removal without notice.
 */
public class ArrayExpression extends NaryExpression {
    /**
     * Constructor
     */
    public ArrayExpression(long where, Expression args[]) {
        super(ARRAY, where, Type.tError, null, args);
    }

    /**
     * Check expression type
     */
    public Vset checkValue(Environment env, Context ctx, Vset vset, Hashtable<Object, Object> exp) {
        env.error(where, "invalid.array.expr");
        return vset;
    }

    public Vset checkInitializer(Environment env, Context ctx, Vset vset, Type t, Hashtable<Object, Object> exp) {
        if (!t.isType(TC_ARRAY)) {
            if (!t.isType(TC_ERROR)) {
                env.error(where, "invalid.array.init", t);
            }
            return vset;
        }
        type = t;
        t = t.getElementType();
        for (int i = 0; i < args.length; i++) {
            vset = args[i].checkInitializer(env, ctx, vset, t, exp);
            args[i] = convert(env, ctx, t, args[i]);
        }
        return vset;
    }

    /**
     * Inline
     */
    public Expression inline(Environment env, Context ctx) {
        Expression e = null;
        for (int i = 0; i < args.length; i++) {
            args[i] = args[i].inline(env, ctx);
            if (args[i] != null) {
                e = (e == null) ? args[i] : new CommaExpression(where, e, args[i]);
            }
        }
        return e;
    }

    public Expression inlineValue(Environment env, Context ctx) {
        for (int i = 0; i < args.length; i++) {
            args[i] = args[i].inlineValue(env, ctx);
        }
        return this;
    }

    /**
     * Code
     */
    public void codeValue(Environment env, Context ctx, Assembler asm) {
        int t = 0;
        asm.add(where, opc_ldc, args.length);
        switch (type.getElementType().getTypeCode()) {
        case TC_BOOLEAN:
            asm.add(where, opc_newarray, T_BOOLEAN);
            break;
        case TC_BYTE:
            asm.add(where, opc_newarray, T_BYTE);
            break;
        case TC_SHORT:
            asm.add(where, opc_newarray, T_SHORT);
            break;
        case TC_CHAR:
            asm.add(where, opc_newarray, T_CHAR);
            break;
        case TC_INT:
            asm.add(where, opc_newarray, T_INT);
            break;
        case TC_LONG:
            asm.add(where, opc_newarray, T_LONG);
            break;
        case TC_FLOAT:
            asm.add(where, opc_newarray, T_FLOAT);
            break;
        case TC_DOUBLE:
            asm.add(where, opc_newarray, T_DOUBLE);
            break;

        case TC_ARRAY:
            asm.add(where, opc_anewarray, type.getElementType());
            break;

        case TC_CLASS:
            asm.add(where, opc_anewarray, env.getClassDeclaration(type.getElementType()));
            break;

        default:
            throw new CompilerError("codeValue");
        }

        for (int i = 0; i < args.length; i++) {

            // If the array element is the default initial value,
            // then don't bother generating code for this element.
            if (args[i].equalsDefault())
                continue;

            asm.add(where, opc_dup);
            asm.add(where, opc_ldc, i);
            args[i].codeValue(env, ctx, asm);
            switch (type.getElementType().getTypeCode()) {
            case TC_BOOLEAN:
            case TC_BYTE:
                asm.add(where, opc_bastore);
                break;
            case TC_CHAR:
                asm.add(where, opc_castore);
                break;
            case TC_SHORT:
                asm.add(where, opc_sastore);
                break;
            default:
                asm.add(where, opc_iastore + type.getElementType().getTypeCodeOffset());
            }
        }
    }
}
