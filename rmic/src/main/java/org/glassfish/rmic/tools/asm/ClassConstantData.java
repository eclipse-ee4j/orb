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

package org.glassfish.rmic.tools.asm;

import org.glassfish.rmic.tools.java.*;
import java.io.IOException;
import java.io.DataOutputStream;

/**
 * This is a class constant pool item.
 *
 * WARNING: The contents of this source file are not part of any
 * supported API.  Code that depends on them does so at its own risk:
 * they are subject to change or removal without notice.
 */
final
class ClassConstantData extends ConstantPoolData {
    String name;

    /**
     * Constructor
     */

    ClassConstantData(ConstantPool tab, ClassDeclaration clazz) {
        String sig = clazz.getType().getTypeSignature();
        // sig is like "Lfoo/bar;", name is like "foo/bar".
        // We assume SIG_CLASS and SIG_ENDCLASS are 1 char each.
        name = sig.substring(1, sig.length()-1);
        tab.put(name);
    }

    // REMIND: this case should eventually go away.
    ClassConstantData(ConstantPool tab, Type t) {
        name = t.getTypeSignature();
        tab.put(name);
    }

    /**
     * Write the constant to the output stream
     */
    void write(Environment env, DataOutputStream out, ConstantPool tab) throws IOException {
        out.writeByte(CONSTANT_CLASS);
        out.writeShort(tab.index(name));
    }

    /**
     * Return the order of the constant
     */
    int order() {
        return 1;
    }

    public String toString() {
        return "ClassConstantData[" + name + "]";
    }
}
