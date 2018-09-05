/*
 * Copyright (c) 1998, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 1998-1999 IBM Corp. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.rmic.iiop;

import org.glassfish.rmic.tools.java.ClassDefinition;
import org.glassfish.rmic.tools.java.ClassNotFound;
import org.glassfish.rmic.tools.java.CompilerError;

import java.util.Vector;

/**
 * NCInterfaceType represents any non-special, non-conforming interface.
 * <p>
 * The static forNCInterface(...) method must be used to obtain an instance.
 * @author      Bryan Atsatt
 */
public class NCInterfaceType extends InterfaceType {

    //_____________________________________________________________________
    // Public Interfaces
    //_____________________________________________________________________

    /**
     * Create an NCInterfaceType for the given class.
     *
     * If the class is not a properly formed or if some other error occurs, the
     * return value will be null, and errors will have been reported to the
     * supplied BatchEnvironment.
     */
    public static NCInterfaceType forNCInterface( ClassDefinition classDef,
                                                  ContextStack stack) {
        if (stack.anyErrors()) return null;

        boolean doPop = false;
        try {
            // Do we already have it?

            org.glassfish.rmic.tools.java.Type theType = classDef.getType();
            Type existing = getType(theType,stack);

            if (existing != null) {

                if (!(existing instanceof NCInterfaceType)) return null; // False hit.

                                // Yep, so return it...

                return (NCInterfaceType) existing;
            }

            NCInterfaceType it = new NCInterfaceType(stack, classDef);
            putType(theType,it,stack);
            stack.push(it);
            doPop = true;

            if (it.initialize(stack)) {
                stack.pop(true);
                return it;
            } else {
                removeType(theType,stack);
                stack.pop(false);
                return null;
            }
        } catch (CompilerError e) {
            if (doPop) stack.pop(false);
            return null;
        }
    }

    /**
     * Return a string describing this type.
     */
    public String getTypeDescription () {
        return "Non-conforming interface";
    }

    //_____________________________________________________________________
    // Internal/Subclass Interfaces
    //_____________________________________________________________________

    /**
     * Create a NCInterfaceType instance for the given class.  The resulting
     * object is not yet completely initialized.
     */
    private NCInterfaceType(ContextStack stack, ClassDefinition classDef) {
        super(stack,classDef,TYPE_NC_INTERFACE | TM_INTERFACE | TM_COMPOUND);
    }

    //_____________________________________________________________________
    // Internal Interfaces
    //_____________________________________________________________________

    /**
     * Initialize this instance.
     */
    private boolean initialize (ContextStack stack) {

        if (stack.getEnv().getParseNonConforming()) {

            Vector directInterfaces = new Vector();
            Vector directMethods = new Vector();
            Vector directMembers = new Vector();

            try {

                // need to include parent interfaces in IDL generation...
                addNonRemoteInterfaces( directInterfaces,stack );

                // Get methods...

                if (addAllMethods(getClassDefinition(),directMethods,false,false,stack) != null) {

                    // Get conforming constants...

                    if (addConformingConstants(directMembers,false,stack)) {

                        // We're ok, so pass 'em up...

                        if (!initialize(directInterfaces,directMethods,directMembers,stack,false)) {
                            return false;
                        }
                    }
                }
                return true;

            } catch (ClassNotFound e) {
                classNotFound(stack,e);
            }
            return false;
        } else {
            return initialize(null,null,null,stack,false);
        }
    }
}
