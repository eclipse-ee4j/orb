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

import org.glassfish.rmic.tools.java.ClassDefinition;
import org.glassfish.rmic.tools.java.ClassNotFound;
import org.glassfish.rmic.tools.java.CompilerError;

import java.util.Vector;

/**
 * AbstractType represents any non-special interface which does not
 * inherit from java.rmi.Remote, for which all methods throw RemoteException.
 * <p>
 * The static forAbstract(...) method must be used to obtain an instance, and will
 * return null if the ClassDefinition is non-conforming.
 * @author      Bryan Atsatt
 */
public class AbstractType extends RemoteType {

    //_____________________________________________________________________
    // Public Interfaces
    //_____________________________________________________________________

    /**
     * Create an AbstractType for the given class.
     *
     * If the class is not a properly formed or if some other error occurs, the
     * return value will be null, and errors will have been reported to the
     * supplied BatchEnvironment.
     */
    public static AbstractType forAbstract(ClassDefinition classDef,
                                           ContextStack stack,
                                           boolean quiet)
    {
        boolean doPop = false;
        AbstractType result = null;

        try {

            // Do we already have it?

            org.glassfish.rmic.tools.java.Type theType = classDef.getType();
            Type existing = getType(theType,stack);

            if (existing != null) {

                if (!(existing instanceof AbstractType)) return null; // False hit.

                                // Yep, so return it...

                return (AbstractType) existing;

            }

            // Could this be an abstract?

            if (couldBeAbstract(stack,classDef,quiet)) {

                // Yes, so try it...

                AbstractType it = new AbstractType(stack, classDef);
                putType(theType,it,stack);
                stack.push(it);
                doPop = true;

                if (it.initialize(quiet,stack)) {
                    stack.pop(true);
                    result = it;
                } else {
                    removeType(theType,stack);
                    stack.pop(false);
                }
            }
        } catch (CompilerError e) {
            if (doPop) stack.pop(false);
        }

        return result;
    }

    /**
     * Return a string describing this type.
     */
    public String getTypeDescription () {
        return "Abstract interface";
    }

    //_____________________________________________________________________
    // Internal/Subclass Interfaces
    //_____________________________________________________________________

    /**
     * Create a AbstractType instance for the given class.  The resulting
     * object is not yet completely initialized.
     */
    private AbstractType(ContextStack stack, ClassDefinition classDef) {
        super(stack,classDef,TYPE_ABSTRACT | TM_INTERFACE | TM_COMPOUND);
    }

    //_____________________________________________________________________
    // Internal Interfaces
    //_____________________________________________________________________


    private static boolean couldBeAbstract(ContextStack stack, ClassDefinition classDef,
                                           boolean quiet) {

        // Return true if interface and not remote...

        boolean result = false;

        if (classDef.isInterface()) {
            BatchEnvironment env = stack.getEnv();

            try {
                result = ! env.defRemote.implementedBy(env, classDef.getClassDeclaration());
                if (!result) failedConstraint(15,quiet,stack,classDef.getName());
            } catch (ClassNotFound e) {
                classNotFound(stack,e);
            }
        } else {
            failedConstraint(14,quiet,stack,classDef.getName());
        }


        return result;
    }


    /**
     * Initialize this instance.
     */
    private boolean initialize (boolean quiet,ContextStack stack) {

        boolean result = false;
        ClassDefinition self = getClassDefinition();

        try {

            // Get methods...

            Vector directMethods = new Vector();

            if (addAllMethods(self,directMethods,true,quiet,stack) != null) {

                // Do we have any methods?

                boolean validMethods = true;

                if (directMethods.size() > 0) {

                                // Yes. Walk 'em, ensuring each is a valid remote method...

                    for (int i = 0; i < directMethods.size(); i++) {

                        if (! isConformingRemoteMethod((Method) directMethods.elementAt(i),true)) {
                            validMethods = false;
                        }
                    }
                }

                if (validMethods) {

                    // We're ok, so pass 'em up...

                    result = initialize(null,directMethods,null,stack,quiet);
                }
            }
        } catch (ClassNotFound e) {
            classNotFound(stack,e);
        }

        return result;
    }
}
