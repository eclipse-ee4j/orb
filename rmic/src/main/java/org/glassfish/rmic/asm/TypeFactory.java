/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.rmic.asm;

import org.glassfish.rmic.tools.java.Identifier;
import org.glassfish.rmic.tools.java.Type;

/**
 * A Factory to create MethodType objects from ASM method descriptors.
 */
class TypeFactory {
    static Type createType(String descriptor) {
        return toRmicType(org.objectweb.asm.Type.getType(descriptor));
    }

    static Type createMethodType(String descriptor) {
        org.objectweb.asm.Type returnType = org.objectweb.asm.Type.getReturnType(descriptor);
        return Type.tMethod(toRmicType(returnType), toTypeArray(org.objectweb.asm.Type.getArgumentTypes(descriptor)));
    }

    private static Type[] toTypeArray(org.objectweb.asm.Type[] argumentTypes) {
        Type[] result = new Type[argumentTypes.length];
        for (int i = 0; i < result.length; i++)
            result[i] = toRmicType(argumentTypes[i]);
        return result;
    }

    private static Type toRmicType(org.objectweb.asm.Type asmType) {
        switch (asmType.getSort()) {
        case org.objectweb.asm.Type.VOID:
            return Type.tVoid;
        case org.objectweb.asm.Type.BOOLEAN:
            return Type.tBoolean;
        case org.objectweb.asm.Type.BYTE:
            return Type.tByte;
        case org.objectweb.asm.Type.SHORT:
            return Type.tShort;
        case org.objectweb.asm.Type.INT:
            return Type.tInt;
        case org.objectweb.asm.Type.LONG:
            return Type.tLong;
        case org.objectweb.asm.Type.FLOAT:
            return Type.tFloat;
        case org.objectweb.asm.Type.DOUBLE:
            return Type.tDouble;
        case org.objectweb.asm.Type.CHAR:
            return Type.tChar;
        case org.objectweb.asm.Type.ARRAY:
            return toArrayType(asmType);
        default:
            return Type.tClass(Identifier.lookup(asmType.getClassName()));
        }
    }

    private static Type toArrayType(org.objectweb.asm.Type asmType) {
        Type type = toRmicType(asmType.getElementType());
        for (int i = 0; i < asmType.getDimensions(); i++)
            type = Type.tArray(type);
        return type;
    }
}
