/*
 * Copyright (c) 1998, 2020 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 1998-1999 IBM Corp. All rights reserved.
 * Copyright (c) 2019 Payara Services Ltd.
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

import java.util.Vector;

import org.glassfish.rmic.tools.java.ClassDefinition;
import org.glassfish.rmic.tools.java.ClassNotFound;
import org.glassfish.rmic.tools.java.CompilerError;

/**
 * NCClassType represents any non-special class which does not
 * extends one or more interfaces which inherit from java.rmi.Remote.
 * <p>
 * The static forImplementation(...) method must be used to obtain an instance,
 * and will return null if the ClassDefinition is non-conforming.
 *
 * @author      Bryan Atsatt
 */
public class NCClassType extends ClassType {

    //_____________________________________________________________________
    // Public Interfaces
    //_____________________________________________________________________

    /**
     * Create an NCClassType for the given class.
     *
     * If the class is not a properly formed or if some other error occurs, the
     * return value will be null, and errors will have been reported to the
     * supplied BatchEnvironment.
     */
    public static NCClassType forNCClass(ClassDefinition classDef,
                                         ContextStack stack) {

        if (stack.anyErrors()) return null;

        boolean doPop = false;
        try {
            // Do we already have it?

            org.glassfish.rmic.tools.java.Type theType = classDef.getType();
            Type existing = getType(theType,stack);

            if (existing != null) {

                if (!(existing instanceof NCClassType)) return null; // False hit.

                                // Yep, so return it...

                return (NCClassType) existing;

            }

            NCClassType it = new NCClassType(stack, classDef);
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
    @Override
    public String getTypeDescription () {
        return addExceptionDescription("Non-conforming class");
    }

    //_____________________________________________________________________
    // Internal/Subclass Interfaces
    //_____________________________________________________________________

    /**
     * Create a NCClassType instance for the given class.  The resulting
     * object is not yet completely initialized.
     */
    private NCClassType(ContextStack stack, ClassDefinition classDef) {
        super(stack,classDef,TYPE_NC_CLASS | TM_CLASS | TM_COMPOUND);
    }

    //_____________________________________________________________________
    // Internal Interfaces
    //_____________________________________________________________________

    /**
     * Initialize this instance.
     */
    private boolean initialize (ContextStack stack) {
        if (!initParents(stack)) {
            return false;
        }

        if (stack.getEnv().getParseNonConforming()) {

            Vector<InterfaceType> directInterfaces = new Vector<>();
            Vector<CompoundType.Method> directMethods = new Vector<>();
            Vector<CompoundType.Member> directMembers = new Vector<>();

            try {

                // Get methods...

                if (addAllMethods(getClassDefinition(),directMethods,false,false,stack) != null) {

                    // Update parent class methods...

                    if (updateParentClassMethods(getClassDefinition(),directMethods,false,stack) != null) {

                    // Get conforming constants...

                    if (addConformingConstants(directMembers,false,stack)) {

                        // We're ok, so pass 'em up...

                        if (!initialize(directInterfaces,directMethods,directMembers,stack,false)) {
                            return false;
                        }
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
