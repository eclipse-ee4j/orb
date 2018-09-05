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

import java.io.*;
import java.util.*;

/**
 * Allows extra paths to be specified when searching for classes.  This
 * gets around the security restriction of changing the classpath within
 * the program.  Also, even if there isn't a security restriction, I
 * think the default ClassLoader only does a getProperty once for the
 * value, so changing it is useless.
 * <P>
 * This is currently only used by Internal and ThreadExec, which execute the
 * class in the current process.  For the external processes, the
 * classpath is augmented on the command line to include the output
 * directory.
 * <P>
 * This follows the delegation model for class loading:  If the class has
 * already been loaded, it returns it.  Next, the system loader is tried.
 * Finally, if the system loader fails, the extra paths are searched.
 */
public class Loader extends ClassLoader
{

    /**
     * Vector of extra paths to search.
     */
    private Vector extraPaths = new Vector(10);

    /**
     *
     * Default constructor.
     *
     */
    public Loader()
    {
    }

    /**
     * Constructor allowing a Vector of search paths to be specified.
     *
     *@param paths  Extra paths to search when loading classes
     *
     */
    public Loader(Vector paths)
    {
        extraPaths = paths;
    }

    /**
     * Add another path to search when loading classes.
     *
     *@param path New path to search
     */
    public void addPath(String path)
    {
        extraPaths.add(path);
    }

    /**
     * Try to load the specified class using the extra paths.  This is 
     * called by the parent loader once it has tried all other means
     * (such as checking for it being loaded, or using the system loader).
     *
     *@param name name of the class to load
     *@exception ClassNotFoundException couldn't find the class
     *@return loaded Class instance
     */
    protected Class findClass(String name) throws ClassNotFoundException 
    {
        byte[] b = loadClassData(name);
        return defineClass(name, b, 0, b.length);
    }

    /**
     * Find the class by searching the extra paths, and read it into
     * a byte array.
     *
     *@param className  Fully qualified class name
     *@return byte array containing the contents of the class file
     *@exception ClassNotFoundException error loading the class
     */
    private byte[] loadClassData(String className) 
        throws ClassNotFoundException
    {
        byte data[] = null;

        // Build the file name and subdirectory from the
        // class name
        String filename = className.replace('.', File.separatorChar) 
                          + ".class";

        Enumeration paths = extraPaths.elements();

        // Search the extra paths
        while (paths.hasMoreElements() && data == null) {

            File file = new File((String)paths.nextElement()
                                 + File.separator 
                                 + filename);

            if (!file.exists())
                continue;

            try {
                
                // Found the file, so open it for reading
                FileInputStream in = new FileInputStream(file);
                
                // Protect against data loss (shouldn't happen)
                if (file.length() > Integer.MAX_VALUE)
                    throw new IOException (className
                                         + " exceeds max length");

                data = new byte[(int)file.length()];
                
                // Read in the file contents
                if (in.read(data) != data.length)
                    throw new IOException ("Lost data when loading "
                                         + className);
                
                in.close();
                
            } catch (Exception ex) {
                throw new ClassNotFoundException(className, ex);
            }
        }

        if (data == null)
            throw new ClassNotFoundException(className);

        return data;
    }
}
        
        
