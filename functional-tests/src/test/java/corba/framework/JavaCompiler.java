/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates.
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

package corba.framework;

import java.util.Vector;
import java.util.StringTokenizer;

/**
 * Wrapper around calling javac.
 */
public class JavaCompiler extends Compiler
{
    private boolean newVM() 
    {
        String version = System.getProperty( "java.version" ) ;
        StringTokenizer st = new StringTokenizer( version, "." ) ;

        // Assume that version is major.minor.patch format.
        // We can ignore the patch, which need not be a string
        // (e.g. 1.3.1_01 is a valid version).
        int major = Integer.parseInt( st.nextToken() ) ;
        int minor = Integer.parseInt( st.nextToken() ) ;

        // If we ever have a 2.x.y version, it would be new.
        // For now, 1.4 and greater are new VMs.
        return (major > 1) || (minor > 3) ;
    }

    /**
     * Returns the class name of the compiler.
     */
    public String compilerClassName()
    {
        // Break this up to avoid rename conflicts on sun tools java.
        return (newVM() ? "com.sun." : "sun.") 
            + "tools.javac.Main" ;
    }

    /**
     * Compile the given .java files.  Files are given as absolute paths.
     * The text output messages from the compile are placed in files
     * named javac.out.txt and javac.err.txt in the
     * given report directory.
     *
     *@param    files           Absolute paths to .java files for compilation
     *                          (can be null)
     *@param    arguments       Command line arguments to the compiler
     *@param    outputDirectory Where the resulting .class should go
     *@param    reportDirectory Where the output/error stream dumps should go
     *
     *@exception    Exception   Any error generated during compile or setup,
     *                          such as abnormal termination
     */
    public void compile(String files[],
                        Vector arguments,
                        String outputDirectory,
                        String reportDirectory) throws Exception
    {
        if (files == null || files.length == 0)
            return;

        Vector args = new Vector(5 + (arguments == null ? 0 : arguments.size()) + files.length);
        args.add("-g");
        args.add("-d");
        args.add(outputDirectory);
        // args.add("-bootclasspath");
        // args.add(Options.getClasspath());
        args.add( "-Xbootclasspath/p:" + 
            System.getProperty( "corba.test.orb.classpath" ) ) ;

        if (arguments != null)
            args.addAll(arguments);

        for(int i = 0; i < files.length; i++)
            args.add(files[i]);

        compileExternally(compilerClassName(),
                          CORBAUtil.toArray(args),
                          outputDirectory,
                          reportDirectory,
                          "javac");
    }
}
