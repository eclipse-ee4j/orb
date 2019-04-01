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

/**
 * An object to represent a name and type constant pool data item.
 *
 * WARNING: The contents of this source file are not part of any supported API. Code that depends on them does so at its
 * own risk: they are subject to change or removal without notice.
 */
final class NameAndTypeData {
    MemberDefinition field;

    /**
     * Constructor
     */
    NameAndTypeData(MemberDefinition field) {
        this.field = field;
    }

    /**
     * Hashcode
     */
    public int hashCode() {
        return field.getName().hashCode() * field.getType().hashCode();
    }

    /**
     * Equality
     */
    public boolean equals(Object obj) {
        if ((obj != null) && (obj instanceof NameAndTypeData)) {
            NameAndTypeData nt = (NameAndTypeData) obj;
            return field.getName().equals(nt.field.getName()) && field.getType().equals(nt.field.getType());
        }
        return false;
    }

    /**
     * Convert to string
     */
    public String toString() {
        return "%%" + field.toString() + "%%";
    }
}
