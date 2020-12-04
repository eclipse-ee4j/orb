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

package sun.rmi.rmic;

import java.io.File;
import sun.tools.java.ClassDefinition;

/**
 * Generator defines the protocol for back-end implementations to be added
 * to rmic.  See the rmic.properties file for a description of the format for
 * adding new Generators to rmic.
 * <p>
 * Classes implementing this interface must have a public default constructor
 * which should set any required arguments to their defaults.  When Main
 * encounters a command line argument which maps to a specific Generator
 * subclass, it will instantiate one and call parseArgs(...).  At some later
 * point, Main will invoke the generate(...) method once for _each_ class passed
 * on the command line.
 *
 * @version     1.0, 2/23/98
 * @author      Bryan Atsatt
 */
public interface Generator {

    /**
     * Examine and consume command line arguments.
     * @param argv The command line arguments. Ignore null
     * and unknown arguments. Set each consumed argument to null.
     * @param main Report any errors using the main.error() methods.
     * @return true if no errors, false otherwise.
     */
    public boolean parseArgs(String argv[], Main main);
    
    /**
     * Generate output. Any source files created which need compilation should
     * be added to the compiler environment using the addGeneratedFile(File)
     * method.
     *
     * @param env       The compiler environment
     * @param cdef      The definition for the implementation class or interface from
     *              which to generate output
     * @param destDir   The directory for the root of the package hierarchy
     *                          for generated files. May be null.
     */
    public void generate(BatchEnvironment env, ClassDefinition cdef, File destDir);
}
