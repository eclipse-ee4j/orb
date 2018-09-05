/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 1998-1999 IBM Corp. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package sun.rmi.rmic.iiop;

import java.io.IOException;
import sun.tools.java.CompilerError;
import sun.tools.java.ClassDefinition;
import sun.rmi.rmic.IndentingWriter;

/**
 * InterfaceType is an abstract base representing any non-special
 * interface type.
 *
 * @version     1.0, 2/27/98
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
