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
