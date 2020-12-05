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

package org.glassfish.rmic.tools.java;

import org.glassfish.rmic.TypeCode;

/**
 * This class represents an Java array type.
 * It overrides the relevant methods in class Type.
 *
 * WARNING: The contents of this source file are not part of any
 * supported API.  Code that depends on them does so at its own risk:
 * they are subject to change or removal without notice.
 *
 * @author      Arthur van Hoff
 */
public final
class ArrayType extends Type {
    /**
     * The type of the element.
     */
    Type elemType;

    /**
     * Construct an array type. Use Type.tArray to create
     * a new array type.
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
