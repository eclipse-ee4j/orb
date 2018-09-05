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

package test;

import java.io.File;
import sun.tools.java.ClassPath;
import sun.tools.java.ClassFile;
import java.io.IOException;
import java.util.StringTokenizer;

/**
 * ClassUtils provides miscellaneous static utility methods related to
 * classes and their packages.
 * @author Bryan Atsatt
 */

public class ClassUtils {

    /**
     * Return the directory which contains a given class (either .java or .class).
     * Uses the current system classpath.
     * @param className Fully qualified class name.
     * @param requireFile True if .class or .java file must be found. False if
     * ok to return a directory which does not contain file.
     * @return the directory or null if none found (or zipped).
     */
    public static File packageDirectory (String className, boolean requireFile) {
        ClassPath path = new ClassPath(System.getProperty("java.class.path"));
        File result = packageDirectory(className,path,requireFile);
        try {
            path.close();
        } catch (IOException e) {}
            
        return result;
    }
        
    /**
     * Return the directory which contains a given class (either .java or .class).
     * @param className Fully qualified class name.
     * @param path the class path.
     * @param requireFile True if .class or .java file must be found. False if
     * ok to return a directory which does not contain file.
     * @return the directory or null if none found (or zipped).
     */
    public static File packageDirectory (String className, ClassPath path, boolean requireFile) {
        
        // Try binary first, then source, then directory...
            
        File result = packageDirectory(className,path,".class");
        if (result == null) {
            result = packageDirectory(className,path,".java");
            if (result == null && !requireFile) {
                int i = className.lastIndexOf('.');
                if (i >= 0) {
                    String packageName = className.substring(0,i);
                    ClassFile cls = path.getDirectory(packageName.replace('.',File.separatorChar));
                    if (cls != null && ! cls.isZipped()) {
                        result = new File(cls.getPath());
                    }
                }
            }
        }
        return result;
    }
        
    private static boolean directoryInPath(String dirPath, String path) {
        if (!dirPath.endsWith(File.separator)) {
            dirPath = dirPath + File.separator;
        }
        StringTokenizer st = new StringTokenizer(path,"\t\n\r"+File.pathSeparator);
        while (st.hasMoreTokens()) {
            String entry = st.nextToken();
            if (!entry.endsWith(".zip") &&
                !entry.endsWith(".jar")) {
                     
                if (entry.equals(".")) {
                    return true;
                } else {
                    if (!entry.endsWith(File.separator)) {
                        entry = entry + File.separator;  
                    }
                    if (entry.equalsIgnoreCase(dirPath)) {
                        return true;   
                    }
                }
            }
        }
      
        return false;
    }
        
    private static File packageDirectory (String className, ClassPath path, String fileExt) {
        
        ClassFile cls = path.getFile(className.replace('.',File.separatorChar) + fileExt);

        if (cls != null && ! cls.isZipped()) {
            File file = new File(cls.getPath());
            File dir = new File(file.getParent());
            return dir;
        }

        return null;
    }
}

