/*
 * Copyright (c) 2018, 2020 Oracle and/or its affiliates.
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

package org.glassfish.rmic.asm;

import org.glassfish.rmic.tools.java.ClassDeclaration;
import org.glassfish.rmic.tools.java.ClassDefinition;
import org.glassfish.rmic.tools.java.ClassNotFound;
import org.glassfish.rmic.tools.java.Environment;
import org.glassfish.rmic.tools.java.Identifier;
import org.glassfish.rmic.tools.java.MemberDefinition;
import org.glassfish.rmic.tools.java.Type;

import java.util.Vector;

public class AsmMemberDefinition extends MemberDefinition {
    private final ClassDeclaration[] exceptions;
    private final String memberValueString;

    /**
     * Constructor for a method definition
     * @param where the location of the definition relative to the class
     * @param clazz the containing class
     * @param modifiers the access modifiers
     * @param type the constructed type
     * @param name the name of the method
     * @param exceptions the checked exceptions throwable by the method
     */
    AsmMemberDefinition(long where, ClassDefinition clazz, int modifiers, Type type, Identifier name, String[] exceptions) {
        super(where, clazz, modifiers, type, name, null, null);

        this.memberValueString = null;
        this.exceptions = toClassDeclarations(exceptions);
    }

    /**
     * Constructor for a field definition
     * @param where the location of the definition relative to the class
     * @param clazz the containing class
     * @param modifiers the access modifiers
     * @param type the constructed type
     * @param name the name of the method
     * @param value the default value for the field
     */
    AsmMemberDefinition(long where, ClassDefinition clazz, int modifiers, Type type, Identifier name, Object value) {
        super(where, clazz, modifiers, type, name, null, null);

        memberValueString = type.toStringValue(value);
        exceptions = null;
    }

    private ClassDeclaration[] toClassDeclarations(String[] classNames) {
        if (classNames == null) return new ClassDeclaration[0];

        ClassDeclaration[] result = new ClassDeclaration[classNames.length];
        for (int i = 0; i < classNames.length; i++)
            result[i] = new ClassDeclaration(Identifier.lookup(classNames[i].replace('/','.')));
        return result;

    }

    @Override
    public String getMemberValueString(Environment env) throws ClassNotFound {
        return memberValueString;
    }

    @Override
    public ClassDeclaration[] getExceptions(Environment env) {
        return exceptions;
    }

    @Override
    public Vector<MemberDefinition> getArguments() {
        return null;
    }
}
