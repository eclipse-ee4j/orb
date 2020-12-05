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
import sun.tools.java.Identifier;

/**
 * Util provides static utility methods used by other rmic classes.
 * @author Bryan Atsatt
 */

public class Util implements sun.rmi.rmic.Constants {

    /**
     * Return the directory that should be used for output for a given
     * class.
     * @param theClass The fully qualified name of the class.
     * @param rootDir The directory to use as the root of the
     * package heirarchy.  May be null, in which case the current
     * working directory is used as the root.
     */
    public static File getOutputDirectoryFor(Identifier theClass,
                                             File rootDir,
                                             BatchEnvironment env) {
        
        File outputDir = null;
        String className = theClass.getFlatName().toString().replace('.', SIGC_INNERCLASS);             
        String qualifiedClassName = className;
        String packagePath = null;
        String packageName = theClass.getQualifier().toString();
                
        if (packageName.length() > 0) {
            qualifiedClassName = packageName + "." + className;
            packagePath = packageName.replace('.', File.separatorChar);
        }

        // Do we have a root directory?
        
        if (rootDir != null) {
                    
            // Yes, do we have a package name?
                
            if (packagePath != null) {
                    
                // Yes, so use it as the root. Open the directory...
                            
                outputDir = new File(rootDir, packagePath);
                            
                // Make sure the directory exists...
                            
                ensureDirectory(outputDir,env);
                    
            } else {
                    
                // Default package, so use root as output dir...
                    
                outputDir = rootDir;
            }               
        } else {
                    
            // No root directory. Get the current working directory...
                    
            String workingDirPath = System.getProperty("user.dir");
            File workingDir = new File(workingDirPath);
                    
            // Do we have a package name?
                    
            if (packagePath == null) {
                        
                // No, so use working directory...
               
                outputDir = workingDir;
                        
            } else {
                        
                // Yes, so use working directory as the root...
                            
                outputDir = new File(workingDir, packagePath);
                                    
                // Make sure the directory exists...
                                    
                ensureDirectory(outputDir,env);
            }
        }

        // Finally, return the directory...
            
        return outputDir;
    }
 
    private static void ensureDirectory (File dir, BatchEnvironment env) {
        if (!dir.exists()) {
            dir.mkdirs();
            if (!dir.exists()) {
                env.error(0,"rmic.cannot.create.dir",dir.getAbsolutePath());
                throw new InternalError();
            }
        }
    }
}

