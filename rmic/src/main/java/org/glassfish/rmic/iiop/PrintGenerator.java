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

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import org.glassfish.rmic.tools.java.CompilerError;
import org.glassfish.rmic.tools.java.ClassDefinition;
import org.glassfish.rmic.IndentingWriter;
import org.glassfish.rmic.Main;

/**
 * An IDL generator for rmic.
 *
 * @author Bryan Atsatt
 */
public class PrintGenerator implements org.glassfish.rmic.Generator, org.glassfish.rmic.iiop.Constants {

    private static final int JAVA = 0;
    private static final int IDL = 1;
    private static final int BOTH = 2;

    private int whatToPrint; // Initialized in parseArgs.
    private boolean global = false;
    private boolean qualified = false;
    private boolean trace = false;
    private boolean valueMethods = false;

    private IndentingWriter out;

    /**
     * Default constructor for Main to use.
     */
    public PrintGenerator() {
        OutputStreamWriter writer = new OutputStreamWriter(System.out);
        out = new IndentingWriter(writer);
    }

    /**
     * Examine and consume command line arguments.
     *
     * @param argv The command line arguments. Ignore null
     * @param error Report any errors using the main.error() methods.
     * @return true if no errors, false otherwise.
     */
    public boolean parseArgs(String argv[], Main main) {
        for (int i = 0; i < argv.length; i++) {
            if (argv[i] != null) {
                String arg = argv[i].toLowerCase();
                if (arg.equals("-xprint")) {
                    whatToPrint = JAVA;
                    argv[i] = null;
                    if (i + 1 < argv.length) {
                        if (argv[i + 1].equalsIgnoreCase("idl")) {
                            argv[++i] = null;
                            whatToPrint = IDL;
                        } else if (argv[i + 1].equalsIgnoreCase("both")) {
                            argv[++i] = null;
                            whatToPrint = BOTH;
                        }
                    }
                } else if (arg.equals("-xglobal")) {
                    global = true;
                    argv[i] = null;
                } else if (arg.equals("-xqualified")) {
                    qualified = true;
                    argv[i] = null;
                } else if (arg.equals("-xtrace")) {
                    trace = true;
                    argv[i] = null;
                } else if (arg.equals("-xvaluemethods")) {
                    valueMethods = true;
                    argv[i] = null;
                }
            }
        }
        return true;
    }

    /**
     * Generate output. Any source files created which need compilation should be added to the compiler environment using
     * the addGeneratedFile(File) method.
     *
     * @param env The compiler environment
     * @param destDir The directory for the root of the package hierarchy
     * @param cdef The definition for the implementation class or interface from which to generate output
     */
    public void generate(org.glassfish.rmic.BatchEnvironment env, File destDir, ClassDefinition cdef) {

        BatchEnvironment ourEnv = (BatchEnvironment) env;
        ContextStack stack = new ContextStack(ourEnv);
        stack.setTrace(trace);

        if (valueMethods) {
            ourEnv.setParseNonConforming(true);
        }

        // Get our top level type...

        CompoundType topType = CompoundType.forCompound(cdef, stack);

        if (topType != null) {

            try {

                // Collect up all the compound types...

                Type[] theTypes = topType.collectMatching(TM_COMPOUND);

                for (int i = 0; i < theTypes.length; i++) {

                    out.pln("\n-----------------------------------------------------------\n");

                    Type theType = theTypes[i];

                    switch (whatToPrint) {
                    case JAVA:
                        theType.println(out, qualified, false, false);
                        break;

                    case IDL:
                        theType.println(out, qualified, true, global);
                        break;

                    case BOTH:
                        theType.println(out, qualified, false, false);
                        theType.println(out, qualified, true, global);
                        break;

                    default:
                        throw new CompilerError("Unknown type!");
                    }
                }

                out.flush();

            } catch (IOException e) {
                throw new CompilerError("PrintGenerator caught " + e);
            }
        }
    }
}
