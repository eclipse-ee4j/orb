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
 * This class represents an Java class type. It overrides the relevant methods in class Type.
 *
 * WARNING: The contents of this source file are not part of any supported API. Code that depends on them does so at its
 * own risk: they are subject to change or removal without notice.
 *
 * @author Arthur van Hoff
 */
public final class ClassType extends Type {
    private static final char QUOTE = '"';
    /**
     * The fully qualified class name.
     */
    Identifier className;

    /**
     * Construct a class type. Use Type.tClass to create a new class type.
     */
    ClassType(String typeSig, Identifier className) {
        super(TypeCode.CLASS, typeSig);
        this.className = className;
    }

    public Identifier getClassName() {
        return className;
    }

    public String typeString(String id, boolean abbrev, boolean ret) {
        String s = (abbrev ? getClassName().getFlatName() : Identifier.lookup(getClassName().getQualifier(), getClassName().getFlatName())).toString();
        return (id.length() > 0) ? s + " " + id : s;
    }

    @Override
    public String toStringValue(Object value) {
        if (value == null || isStringType()) {
            return null;
        } else {
            return QUOTE + value.toString() + QUOTE;
        }
    }

    private boolean isStringType() {
        return !className.toString().equals(String.class.getName());
    }
}
