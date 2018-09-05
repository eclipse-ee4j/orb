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

import java.util.Hashtable;
import java.io.File;
import java.io.FileInputStream;

/**
 * DirectoryLoader is a simple ClassLoader which loads from a specified
 * file system directory.
 * @author Bryan Atsatt
 */

public class DirectoryLoader extends ClassLoader {

    private Hashtable cache;
    private File root;

    /**
     * Constructor.
     */
    public DirectoryLoader (File rootDir) {
        cache = new Hashtable();
        if (rootDir == null || !rootDir.isDirectory()) {
            throw new IllegalArgumentException();
        }
        root = rootDir;
    }

    private DirectoryLoader () {}

    /**
     * Convenience version of loadClass which sets 'resolve' == true.
     */
    public Class loadClass(String className) throws ClassNotFoundException {
        return loadClass(className, true);
    }

    /**
     * This is the required version of loadClass which is called
     * both from loadClass above and from the internal function
     * FindClassFromClass.
     */
    public synchronized Class loadClass(String className, boolean resolve)
        throws ClassNotFoundException {
        Class result;
        byte  classData[];

        // Do we already have it in the cache?

        result = (Class) cache.get(className);

        if (result == null) {

            // Nope, can we get if from the system class loader?

            try {

                result = super.findSystemClass(className);

            } catch (ClassNotFoundException e) {

                // No, so try loading it...

                classData = getClassFileData(className);

                if (classData == null) {
                    throw new ClassNotFoundException();
                }

                // Parse the class file data...

                result = defineClass(classData, 0, classData.length);

                if (result == null) {
                    throw new ClassFormatError();
                }

                // Resolve it...

                if (resolve) resolveClass(result);

                // Add to cache...

                cache.put(className, result);
            }
        }

        return result;
    }

    /**
     * Reurn a byte array containing the contents of the class file.  Returns null
     * if an exception occurs.
     */
    private byte[] getClassFileData (String className) {

        byte result[] = null;
        FileInputStream stream = null;

        // Get the file...

        File classFile = new File(root,className.replace('.',File.separatorChar) + ".class");

        // Now get the bits...

        try {
            stream = new FileInputStream(classFile);
            result = new byte[stream.available()];
            stream.read(result);
        } catch(ThreadDeath death) {
            throw death;
        } catch (Throwable e) {
        }

        finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch(ThreadDeath death) {
                    throw death;
                } catch (Throwable e) {
                }
            }
        }

        return result;
    }
}
