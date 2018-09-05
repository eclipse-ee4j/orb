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

import java.util.Vector;
import java.io.OutputStream;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.HashSet;
import test.Util;
import test.Test;

/*
 * @test
 */
public abstract class RMICTest extends Test {

    public static final String SKIP_RMIC_FLAG = "-normic";
    private static HashSet files = new HashSet();

    /**
     * Return an array of fully qualified class names for which generation
     * should occur. Return empty array if none.
     */
    protected abstract String[] getGenerationClasses () throws Throwable;

    /**
     * Perform the test.
     */
    protected abstract void doTest () throws Throwable;

    /**
     * Return the primary generator argument (e.g. "-iiop" or "-idl").
     */
    protected abstract String getGeneratorArg ();

    /**
     * Append additional (i.e. after -idl and before classes) rmic arguments
     * to 'currentArgs'. This implementation will set the output directory if
     * the OUTPUT_DIRECTORY flag was passed on the command line.
     */
    protected String[] getAdditionalRMICArgs (String[] currentArgs) {

        String[] result = currentArgs;
        String dir = (String)getArgs().get(OUTPUT_DIRECTORY);

        if (dir != null) {

            result = new String[currentArgs.length + 2];
            System.arraycopy(currentArgs,0,result,0,currentArgs.length);
            result[currentArgs.length] = "-d";
            result[currentArgs.length + 1] = dir;
        }

        return result;
    }

    /**
     * Return true if generation shuold occur once per process, false
     * if they should be generated prior to each call to doTest(). Default
     * is true.
     */
    protected boolean generateOnlyOnce () {
        return true;
    }
   
    /**
     * Return the output stream to which rmic should write output.
     */
    protected OutputStream getOutputStream () {
        return System.out;
    }
 
    /**
     * Generate rmic output for the supplied class.
     */
    protected void generate (String theClass, OutputStream out) throws Exception {
        String[] list = new String[1];
        list[0] = theClass;
        generate(list,out);
    }
    
    /**
     * Generate rmic output for the supplied classes.
     */
    protected void generate (String[] classes,OutputStream out) throws Exception {
        String[] additionalArgs = new String[0];
        String generatorArg = getGeneratorArg();
        additionalArgs = getAdditionalRMICArgs(additionalArgs);
        boolean onlyOnce = generateOnlyOnce();
        generate(classes,generatorArg,additionalArgs,onlyOnce,out);
    }

    /**
     * Run the test.
     */
    public void run () {
        try {

            if (!getArgs().containsKey(SKIP_RMIC_FLAG)) {

                // Do generation...

                generate(getGenerationClasses(),getOutputStream());
            }

            // Do the test...

            doTest();

        } catch (ThreadDeath death) {
            throw death;
        } catch (Throwable e) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            e.printStackTrace(new PrintStream(out));
            status = new Error("Caught " + out.toString());
        }
    }

    /**
     * Generate.
     * @param classes An array of fully qualified class names.
     * @param generatorArg The primary generator argument.
     * @param additionalRMICArgs An array of additional arguments  (e.g. after -idl and
     * before classes) to rmic.
     * @param onlyOnce Return true if stubs should only be generated once per process.
     * @param out Where to write output.
     */
    public static synchronized void generate (  String[] classes,
                                                String generatorArg,
                                                String[] additionalRMICArgs,
                                                boolean onlyOnce,
                                                OutputStream out) throws Exception {

        // Create a list of classes to compile...

        Vector list = new Vector(classes.length);
        for (int i = 0; i < classes.length; i++) {

            String theClass = classes[i];

            // Do we need to compile this class?

            if (!files.contains(theClass)) {

                // Yes.

                list.addElement(theClass);
            }
        }

        // Anything to do?

        int count = list.size();

        if (count > 0) {

            // Yep. Convert vector to array...

            String[] compileEm = new String[count];
            list.copyInto(compileEm);

            // Do the compile...

            Util.rmic(generatorArg,additionalRMICArgs,compileEm,false,out);

            // Are we supposed to compile these guys only once?

            if (onlyOnce) {

                // Yes, so add them to the static table...

                for (int i = 0; i < count; i++) {
                    files.add(compileEm[i]);
                }
            }
        }
    }
}
