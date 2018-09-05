/*
 * Copyright (c) 1995, 2018 Oracle and/or its affiliates. All rights reserved.
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
 * This class is used to assemble the local variables in the local
 * variable table.
 *
 * WARNING: The contents of this source file are not part of any
 * supported API.  Code that depends on them does so at its own risk:
 * they are subject to change or removal without notice.
 *
 * @author Arthur van Hoff
 */
public final
class LocalVariable {
    MemberDefinition field;
    int slot;
    int from;
    int to;

    public LocalVariable(MemberDefinition field, int slot) {
        if (field == null) {
            new Exception().printStackTrace();
        }
        this.field = field;
        this.slot = slot;
        to = -1;
    }

    LocalVariable(MemberDefinition field, int slot, int from, int to) {
        this.field = field;
        this.slot = slot;
        this.from = from;
        this.to = to;
    }

    public String toString() {
        return field + "/" + slot;
    }
}
