/*
 * Copyright (c) 1995, 2020 Oracle and/or its affiliates. All rights reserved.
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
