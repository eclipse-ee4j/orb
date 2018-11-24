/*
 * Copyright (c) 1994, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.rmic.tools.java;

import org.glassfish.rmic.TypeCode;

/**
 * This class represents an Java array type. It overrides the relevant methods in class Type.
 *
 * WARNING: The contents of this source file are not part of any supported API. Code that depends on them does so at its
 * own risk: they are subject to change or removal without notice.
 *
 * @author Arthur van Hoff
 */
public final class ArrayType extends Type {
    /**
     * The type of the element.
     */
    Type elemType;

    /**
     * Construct an array type. Use Type.tArray to create a new array type.
     */
    ArrayType(String typeSig, Type elemType) {
        super(TypeCode.ARRAY, typeSig);
        this.elemType = elemType;
    }

    public Type getElementType() {
        return elemType;
    }

    public int getArrayDimension() {
        return elemType.getArrayDimension() + 1;
    }

    public String typeString(String id, boolean abbrev, boolean ret) {
        return getElementType().typeString(id, abbrev, ret) + "[]";
    }
}
