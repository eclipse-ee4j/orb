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
 * This class represents an Java method type.
 * It overrides the relevant methods in class Type.
 *
 * WARNING: The contents of this source file are not part of any
 * supported API.  Code that depends on them does so at its own risk:
 * they are subject to change or removal without notice.
 *
 * @author      Arthur van Hoff
 */
public final
class MethodType extends Type {
    /**
     * The return type.
     */
    Type returnType;

    /**
     * The argument types.
     */
    Type argTypes[];

    /**
     * Construct a method type. Use Type.tMethod to create
     * a new method type.
     * @see Type.tMethod
     */
    MethodType(String typeSig, Type returnType, Type argTypes[]) {
        super(TypeCode.METHOD, typeSig);
        this.returnType = returnType;
        this.argTypes = argTypes;
    }

    public Type getReturnType() {
        return returnType;
    }

    public Type getArgumentTypes()[] {
        return argTypes;
    }

    public boolean equalArguments(Type t) {
        if (t.getTypeCode() != TC_METHOD) {
            return false;
        }
        MethodType m = (MethodType)t;
        if (argTypes.length != m.argTypes.length) {
            return false;
        }
        for (int i = argTypes.length - 1 ; i >= 0 ; i--) {
            if (argTypes[i] != m.argTypes[i]) {
                return false;
            }
        }
        return true;
    }

    public int stackSize() {
        int n = 0;
        for (int i = 0 ; i < argTypes.length ; i++) {
            n += argTypes[i].stackSize();
        }
        return n;
    }

    public String typeString(String id, boolean abbrev, boolean ret) {
        StringBuilder sb = new StringBuilder();
        sb.append(id);
        sb.append('(');
        for (int i = 0 ; i < argTypes.length ; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(argTypes[i].typeString("", abbrev, ret));
        }
        sb.append(')');

        return ret ? getReturnType().typeString(sb.toString(), abbrev, ret) : sb.toString();
    }
}
