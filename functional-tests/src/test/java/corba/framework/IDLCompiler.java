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
import java.io.File;

/**
 * Wrapper around calling a real IDL compiler.
 */
public class IDLCompiler extends Compiler
{
    /**
     * Command line option for specifying the output directory
     */
    protected String OUTPUT_DIR_OPTION = "-td";

    /**
     * Default IDL compiler class name
     */
    protected static final String DEFAULT_IDL_COMPILER_CLASS 
        = "com.sun.tools.corba.ee.idl.toJavaPortable.Compile";
    
    /**
     * Returns the class name of the compiler.
     */
    public String compilerClassName()
    {
        return DEFAULT_IDL_COMPILER_CLASS;
    }
    
    /**
     * Compile the given IDL files.  Files are given as absolute paths.
     * The text output messages from the compile are placed in files
     * named idlcompile.out.txt and idlcompile.err.txt in the
     * given report directory.
     *
     *@param    files           Absolute paths to IDL files for compilation
     *                          (can be null)
     *@param    arguments       Command line arguments to the compiler
     *@param    stubDirectory   Where the resulting .java files should go
     *@param    reportDirectory Where the output/error stream dumps should go
     *
     *@exception    Exception   Any error generated during compile or setup,
     *                          such as abnormal termination
     */
    public void compile(String files [],
                        Vector arguments,
                        String stubDirectory,
                        String reportDirectory) throws Exception
    {
        if (files == null || files.length == 0)
            return;

        // Probably the right way to do this modification (which is
        // specific to our compiler) would've been to subclass IDLCompiler
        // and use the subclass until our compiler is better.

        // If there's only one file, use "idlcompiler" as the base of the
        // name for the stdout/stderr streams files.
        if (files.length == 1) {
            compileHelper(files[0], 
                          "idlcompiler", 
                          arguments, 
                          stubDirectory,
                          reportDirectory);
        } else {
            // Currently, our IDL compiler can only handle one file at
            // a time.  This means we must make multiple executions to
            // get everything compiled!
            for (int i = 0; i < files.length; i++) {
                String fn = null;
                try {
                    // Try to obtain the filename (without .idl) so
                    // that the base of the output file names will
                    // be idlcompiler_{filename}
                    File file = new File(files[i]);
                    String fileName = file.getName();
                    int dotIndex = fileName.indexOf(".idl");
                    if (dotIndex > 0) 
                        fileName = fileName.substring(0, dotIndex);
                    fn = fileName;
                } catch (Throwable t) {
                    // If something goes wrong, just make it
                    // idlcompiler_{file number in the sequence}
                    fn = "" + i;
                }

                // Do the compilation for this file
                compileHelper(files[i],
                              "idlcompiler_" + fn,
                              arguments,
                              stubDirectory,
                              reportDirectory);
            }
        }
    }


    /**
     * Helper that compiles one file externally.  When our IDL compiler
     * supports multiple files, this can be moved back into compile
     * above.
     */
    private void compileHelper(String file,
                               String outputFileName,
                               Vector arguments,
                               String stubDirectory,
                               String reportDirectory) throws Exception
    {

        Vector args = new Vector(1 + arguments.size() + 2);
        args.add(OUTPUT_DIR_OPTION);
        args.add(stubDirectory);

        if (arguments != null)
            args.addAll(arguments);

        args.add(file);

        compileExternally(compilerClassName(),
                          CORBAUtil.toArray(args),
                          stubDirectory,
                          reportDirectory,
                          outputFileName);
    }
}
