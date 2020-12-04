/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates.
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

package rmic;

/*
 * @test
 */
public class IDLReferenceTest extends IDLTest {

    public static final String[] ADDITIONAL_ARGS = {"-alwaysGenerate",
                                                    "-noValueMethods"};

    public static final String CLASS_LIST_FILE = ".classlist";
    public static final String IDL_FILE =  ".idl";
    public static final String IDL_REF_FILE =  ".idlref";
    public static final String[] IGNORE_PREFIX =  {"/*", " *", "*",};

    private String[] classes = null;

    /**
     * Return an array of fully qualified class names for which generation
     * should occur. Return empty array if none.
     */
    protected String[] getGenerationClasses () throws Throwable {
        return getClasses();
    }

    /**
     * Perform the test.
     */
    protected void doTest () throws Throwable {
        getClasses();
        for (int i = 0; i < classes.length; i++) {
            compareUnorderedResources(classes[i],IDL_FILE,IDL_REF_FILE,IGNORE_PREFIX);
        }
    }

    /**
     * Append additional (i.e. after -idl and before classes) rmic arguments
     * to 'currentArgs'. This implementation will set the output directory if
     * the OUTPUT_DIRECTORY flag was passed on the command line.
     */
    protected String[] getAdditionalRMICArgs (String[] currentArgs) {
        return super.getAdditionalRMICArgs(ADDITIONAL_ARGS);
    }

    private synchronized String[] getClasses () throws Throwable {
        if (classes == null) {
            classes = getResourceAsArray(getClass().getName(),CLASS_LIST_FILE,"#");
        }
        return classes;
    }

}
