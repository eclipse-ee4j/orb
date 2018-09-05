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
import java.util.Properties ;

/**
 * Abstraction of a compiler, used to implement IDLJ, RMIC, and Javac
 * wrappers.
 */
public abstract class Compiler
{
    /**
     * Compile the given files according to the other parameters.
     *
     *@param files           Array of files to compile (assumes full paths)
     *@param arguments       Arguments to the compiler
     *@param outputDirectory Directory in which to place generated files
     *@param reportDirectory Directory in which to place dump files of
     *                       the compiler's stdout and stderr
     *
     *@exception Exception   Error occured
     *                       (probably bad exit value)
     */
    public abstract void compile(String files[],
                                 Vector arguments,
                                 String outputDirectory,
                                 String reportDirectory) throws Exception;

    /**
     * Perform the compile in a separate process.  It's easier to do it
     * that way since the compiler's output streams can be dumped to files.
     * This waits for completion or a maximum timeout (defined in Options)
     *
     *@param className  Name of the class of the compiler
     *@param progArgs   Arguments to the compiler (including file names)
     *@param outputDirectory  Directory in which to place generated files
     *@param reportDirectory  Directory in which to place IO dumps
     *@param compilerName  Identifying name of the compiler for the IO
     *                     files (to create "javac.err.txt", etc)
     *@exception Exception  Exception  Error occured (probably bad exit value)
     */
    protected void compileExternally(String className,
                                     String[] progArgs,
                                     String outputDirectory,
                                     String reportDirectory,
                                     String compilerName) throws Exception
    {
        // Make certain the directories exist
        // Note: this must be done here as well as in the test harness
        // in case a test (like corba.codebase) changes the output directory
        // in the test itself!
        CORBAUtil.mkdir(outputDirectory);
        CORBAUtil.mkdir(reportDirectory);

        FileOutputDecorator exec 
            = new FileOutputDecorator(new ExternalExec(false));
   
        Properties props = new Properties() ;
        int emmaPort = EmmaControl.setCoverageProperties( props ) ;
        exec.initialize(className,
                        compilerName,
                        props,
                        null,
                        progArgs,
                        reportDirectory + compilerName + ".out.txt",
                        reportDirectory + compilerName + ".err.txt",
                        null,
                        emmaPort ) ;
    
        exec.start();
        int result = 1;

        try {

            result = exec.waitFor(Options.getMaximumTimeout());

        } catch (Exception e) {
            exec.stop();
            throw e;
        }

        if (result != Controller.SUCCESS) 
            throw new Exception(compilerName 
                                + " compile failed with result: " 
                                + result);

        exec.stop();
    }
}
