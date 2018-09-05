/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package corba.framework;

import java.util.Vector;

/**
 * Wrapper around calling a real RMIC.
 */
public class RMICompiler extends Compiler
{
    /**
     * Command line option for specifying the output directory
     */
    protected String OUTPUT_DIR_OPTION = "-d";

    /**
     * Default RMIC class name
     */
    protected static final String DEFAULT_RMIC_CLASS 
        = "sun.rmi.rmic.Main";
    
    /**
     * Returns the class name of the compiler.
     */
    public String compilerClassName()
    {
        return DEFAULT_RMIC_CLASS;
    }
    
    /**
     * Compile the given class files.  Files are given as absolute paths.
     * The text output messages from the compile are placed in files
     * named rmic.out.txt and rmic.err.txt in the
     * given report directory.
     *
     *@param    files           Absolute paths to class files for compilation
     *                          (can be null)
     *@param    arguments       Command line arguments to the compiler
     *@param    outputDirectory Where the resulting files should go
     *@param    reportDirectory Where the output/error stream dumps should go
     *
     *@exception    Exception   Any error generated during compile or setup,
     *                          such as abnormal termination
     */
    public void compile(String files [],
                        Vector arguments,
                        String outputDirectory,
                        String reportDirectory) throws Exception
    {
        if (files == null || files.length == 0)
            return;

        Vector args = new Vector(files.length + arguments.size() + 4);
        args.add(OUTPUT_DIR_OPTION);
        args.add(outputDirectory);
        args.add("-classpath");
        args.add(Options.getClasspath());

        if (arguments != null)
            args.addAll(arguments);

        for(int i = 0; i < files.length; i++)
            args.add(files[i]);

        compileExternally(compilerClassName(),
                          CORBAUtil.toArray(args),
                          outputDirectory,
                          reportDirectory,
                          "rmic");
    }
}
