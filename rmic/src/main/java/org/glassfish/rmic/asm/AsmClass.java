/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.rmic.asm;

import org.glassfish.rmic.tools.java.ClassDeclaration;
import org.glassfish.rmic.tools.java.ClassDefinition;
import org.glassfish.rmic.tools.java.ClassNotFound;
import org.glassfish.rmic.tools.java.Environment;
import org.glassfish.rmic.tools.java.Identifier;

/**
 * This represents a class for RMIC to process. It is built from a class file using ASM.
 */
class AsmClass extends ClassDefinition {

    private final AsmClassFactory factory;

    AsmClass(AsmClassFactory factory, String name, int modifiers, ClassDeclaration declaration, ClassDeclaration superClassDeclaration,
            ClassDeclaration[] interfaceDeclarations) {
        super(name, 0, declaration, modifiers, null, null);
        this.factory = factory;
        superClass = superClassDeclaration;
        interfaces = interfaceDeclarations;
    }

    @Override
    public void loadNested(Environment env) {
        try {
            Identifier outerClass = factory.getOuterClassName(getName());
            if (outerClass != null)
                this.outerClass = env.getClassDefinition(outerClass);
        } catch (ClassNotFound ignore) {
        }
    }

    private boolean basicCheckDone = false;
    private boolean basicChecking = false;

    // This code is copied from BinaryClass.java which ensures that inherited method
    // information is gathered. Consider promoting this to the super class.
    protected void basicCheck(Environment env) throws ClassNotFound {
        if (tracing)
            env.dtEnter("AsmClass.basicCheck: " + getName());

        if (basicChecking || basicCheckDone) {
            if (tracing)
                env.dtExit("AsmClass.basicCheck: OK " + getName());
            return;
        }

        if (tracing)
            env.dtEvent("AsmClass.basicCheck: CHECKING " + getName());
        basicChecking = true;

        super.basicCheck(env);

        // Collect inheritance information.
        if (doInheritanceChecks) {
            collectInheritedMethods(env);
        }

        basicCheckDone = true;
        basicChecking = false;
        if (tracing)
            env.dtExit("AsmClass.basicCheck: " + getName());
    }

}
