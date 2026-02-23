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

import java.io.DataOutputStream;
import java.io.IOException;

import org.glassfish.rmic.tools.java.Environment;
import org.glassfish.rmic.tools.java.MemberDefinition;

/**
 * This class is used to assemble the local variable table.
 *
 * WARNING: The contents of this source file are not part of any
 * supported API.  Code that depends on them does so at its own risk:
 * they are subject to change or removal without notice.
 *
 * @author Arthur van Hoff
 */
final
class LocalVariableTable {
    LocalVariable locals[] = new LocalVariable[8];
    int len;

    /**
     * Define a new local variable. Merge entries where possible.
     */
    void define(MemberDefinition field, int slot, int from, int to) {
        if (from >= to) {
            return;
        }
        for (int i = 0 ; i < len ; i++) {
            if ((locals[i].field == field) && (locals[i].slot == slot) &&
                (from <= locals[i].to) && (to >= locals[i].from)) {
                locals[i].from = Math.min(locals[i].from, from);
                locals[i].to = Math.max(locals[i].to, to);
                return;
            }
        }
        if (len == locals.length) {
            LocalVariable newlocals[] = new LocalVariable[len * 2];
            System.arraycopy(locals, 0, newlocals, 0, len);
            locals = newlocals;
        }
        locals[len++] = new LocalVariable(field, slot, from, to);
    }

    /**
     * Trim overlapping local ranges.  Java forbids shadowing of
     * locals in nested scopes, but non-nested scopes may still declare
     * locals with the same name.  Because local variable ranges are
     * computed using flow analysis as part of assembly, it isn't
     * possible to simply make sure variable ranges end where the
     * enclosing lexical scope ends.  This method makes sure that
     * variables with the same name don't overlap, giving priority to
     * fields with higher slot numbers that should have appeared later
     * in the source.
     */
    private void trim_ranges() {
        for (int i=0; i<len; i++) {
            for (int j=i+1; j<len; j++) {
                if ((locals[i].field.getName()==locals[j].field.getName())
                        && (locals[i].from <= locals[j].to)
                        && (locals[i].to >= locals[j].from)) {
                    // At this point we know that both ranges are
                    // the same name and there is also overlap or they abut
                    if (locals[i].slot < locals[j].slot) {
                        if (locals[i].from < locals[j].from) {
                          locals[i].to = Math.min(locals[i].to, locals[j].from);
                        } else {
                          // We've detected two local variables with the
                          // same name, and the one with the greater slot
                          // number starts before the other.  This order
                          // reversal may happen with locals with the same
                          // name declared in both a try body and an
                          // associated catch clause.  This is rare, and
                          // we give up.
                        }
                    } else if (locals[i].slot > locals[j].slot) {
                        if (locals[i].from > locals[j].from) {
                          locals[j].to = Math.min(locals[j].to, locals[i].from);
                        } else {
                          // Same situation as above; just give up.
                        }
                    } else {
                        // This case can happen if there are two variables
                        // with the same name and slot numbers, and ranges
                        // that abut.  AFAIK the only way this can occur
                        // is with multiple static initializers.  Punt.
                    }
                }
            }
        }
    }

    /**
     * Write out the data.
     */
    void write(Environment env, DataOutputStream out, ConstantPool tab) throws IOException {
        trim_ranges();
        out.writeShort(len);
        for (int i = 0 ; i < len ; i++) {
            //System.out.println("pc=" + locals[i].from + ", len=" + (locals[i].to - locals[i].from) + ", nm=" + locals[i].field.getName() + ", slot=" + locals[i].slot);
            out.writeShort(locals[i].from);
            out.writeShort(locals[i].to - locals[i].from);
            out.writeShort(tab.index(locals[i].field.getName().toString()));
            out.writeShort(tab.index(locals[i].field.getType().getTypeSignature()));
            out.writeShort(locals[i].slot);
        }
    }
}
