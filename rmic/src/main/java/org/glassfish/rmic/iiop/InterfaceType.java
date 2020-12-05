/*
 * Copyright (c) 1998, 2020 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 1998-1999 IBM Corp. All rights reserved.
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

package org.glassfish.rmic.iiop;

import org.glassfish.rmic.IndentingWriter;
import org.glassfish.rmic.tools.java.ClassDefinition;
import org.glassfish.rmic.tools.java.CompilerError;

import java.io.IOException;

/**
 * InterfaceType is an abstract base representing any non-special
 * interface type.
 *
 * @author      Bryan Atsatt
 */
public abstract class InterfaceType extends CompoundType {

    //_____________________________________________________________________
    // Public Interfaces
    //_____________________________________________________________________

    /**
     * Print this type.
     * @param writer The stream to print to.
     * @param useQualifiedNames If true, print qualified names; otherwise, print unqualified names.
     * @param useIDLNames If true, print IDL names; otherwise, print java names.
     * @param globalIDLNames If true and useIDLNames true, prepends "::".
     */
    public void print ( IndentingWriter writer,
                        boolean useQualifiedNames,
                        boolean useIDLNames,
                        boolean globalIDLNames) throws IOException {

        if (isInner()) {
            writer.p("// " + getTypeDescription() + " (INNER)");
        } else {
            writer.p("// " + getTypeDescription() + "");
        }
        writer.pln(" (" + getRepositoryID() + ")\n");
        printPackageOpen(writer,useIDLNames);

        if (!useIDLNames) {
            writer.p("public ");
        }

        writer.p("interface " + getTypeName(false,useIDLNames,false));
        printImplements(writer,"",useQualifiedNames,useIDLNames,globalIDLNames);
        writer.plnI(" {");
        printMembers(writer,useQualifiedNames,useIDLNames,globalIDLNames);
        writer.pln();
        printMethods(writer,useQualifiedNames,useIDLNames,globalIDLNames);
        writer.pln();

        if (useIDLNames) {
            writer.pOln("};");
        } else {
            writer.pOln("}");
        }
        printPackageClose(writer,useIDLNames);
    }

    //_____________________________________________________________________
    // Subclass/Internal Interfaces
    //_____________________________________________________________________

    /**
     * Create a InterfaceType instance for the given class. NOTE: This constructor
     * is ONLY for SpecialInterfaceType.
     */
    protected InterfaceType(ContextStack stack, int typeCode, ClassDefinition classDef) {
        super(stack,typeCode,classDef); // Call special parent constructor.

        if ((typeCode & TM_INTERFACE) == 0 || ! classDef.isInterface()) {
            throw new CompilerError("Not an interface");
        }
    }

    /**
     * Create a InterfaceType instance for the given class.  The resulting
     * object is not yet completely initialized. Subclasses must call
     * initialize(directInterfaces,directInterfaces,directConstants);
     */
    protected InterfaceType(ContextStack stack,
                            ClassDefinition classDef,
                            int typeCode) {
        super(stack,classDef,typeCode);

        if ((typeCode & TM_INTERFACE) == 0 || ! classDef.isInterface()) {
            throw new CompilerError("Not an interface");
        }
    }
}
