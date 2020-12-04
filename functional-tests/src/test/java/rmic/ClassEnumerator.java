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

package rmic;

import java.util.Enumeration;
import java.util.Vector;
import java.util.Hashtable;
import java.io.File;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipException;
import java.util.Comparator;
import java.util.Arrays;

/**
 * This class provides static methods to enumerate all classes in
 * a specified class path.
 */
public class ClassEnumerator {

    /**
     * Get classes using the specified path string.
     */
    public static Vector getClasses (String pathstr, boolean sort) {
        Vector list = new Vector(4096);
        Hashtable roots = new Hashtable(20);
        Object nullValue = new Object();
        ClassPathEntry[] path = null;

        try {
            path = parsePath(pathstr);
            for (int i = path.length; --i >= 0; ) {
                if (path[i].zip != null) {
                    Enumeration e = path[i].zip.entries();
                    while (e.hasMoreElements()) {
                        ZipEntry entry = (ZipEntry)e.nextElement();
                        String name = entry.getName();
                        int index = name.indexOf(".class");
                        if (index >=0) {
                            name = name.replace('/','.').substring(0,index);
                            list.addElement(name);
                        }
                    }
                } else {

                    // Just gather the unique root directories...

                    File rootDir = path[i].dir;
                    if (rootDir.getPath().equals(".")) {
                        rootDir = new File(System.getProperty("user.dir"));
                    }

                    // _REVISIT_ Forcing lower case here could cause us to skip a
                    //           real root (if actually different case); however,
                    //           if we don't do this, the "user.dir" property can
                    //           screw us up by being in different case!

                    String pathName = rootDir.getPath().toLowerCase();

                    if (!roots.containsKey(pathName)) {
                        roots.put(pathName,rootDir);
                    }
                }
            }

            // Process the root directories...

            for (Enumeration e = roots.keys(); e.hasMoreElements() ;) {
                File rootDir = (File) roots.get(e.nextElement());
                int rootLen = rootDir.getPath().length() + 1;
                addClasses(rootLen,rootDir,list,roots);
            }

            // Release resources...

        } finally {

            if (path != null) {
                for (int i = path.length; --i >= 0; ) {
                    if (path[i].zip != null) {
                        try {
                            path[i].zip.close();
                        } catch (IOException e) {}
                    }
                }
            }
        }

        // Sort it if we're supposed to...

        if (sort) {
            int size = list.size();
            String[] temp = new String[size];
            list.copyInto(temp);
            Arrays.sort(temp,new StringComparator());
            list = new Vector(size);
            for (int i = 0; i < size; i++) {
                list.addElement(temp[i]);
            }
        }

        return list;
    }

    /**
     * Get classes using the class path returned by <code>getFullClassPath()</code>.
     */
    public static Vector getClasses (boolean sort) {
        String cp = getFullClassPath();
        return getClasses(cp,sort);
    }

    /**
     * Return a class path constructed by concatenating the System
     * Property values for:
     * <pre>
     *    java.sys.class.path
     *    java.class.path
     *    env.class.path
     * </pre>
     * in that order.
     */
    public static String getFullClassPath () {
        String syscp = System.getProperty("java.sys.class.path");
        String appcp = System.getProperty("java.class.path");
        String envcp = System.getProperty("env.class.path");
        String cp = null;

        if (syscp != null) {
            cp = syscp;
        }

        if (appcp != null) {
            if (cp == null) {
                cp = appcp;
            } else {
                cp = cp + File.pathSeparator + appcp;
            }
        }

        if (envcp != null) {
            if (cp == null) {
                cp = envcp;
            } else {
                cp = cp + File.pathSeparator + envcp;
            }
        }

        if (cp == null) {
            cp = ".";
        }

        return cp;
    }

    private static ClassPathEntry[] parsePath (String pathstr) {

        char dirSeparator = File.pathSeparatorChar;
        int i, j, n;
        ClassPathEntry[] path;

        if (pathstr.length() == 0) {
            path = new ClassPathEntry[0];
        }

        // Count the number of path separators
        i = n = 0;
        while ((i = pathstr.indexOf(dirSeparator, i)) != -1) {
            n++; i++;
        }
        // Build the class path
        path = new ClassPathEntry[n+1];
        int len = pathstr.length();
        for (i = n = 0; i < len; i = j + 1) {
            if ((j = pathstr.indexOf(dirSeparator, i)) == -1) {
                j = len;
            }
            if (i == j) {
                path[n] = new ClassPathEntry();
                path[n++].dir = new File(".");
            } else {
                File file = new File(pathstr.substring(i, j));
                if (file.exists()) {
                    if (file.isFile()) {
                        try {
                            ZipFile zip = new ZipFile(file);
                            path[n] = new ClassPathEntry();
                            path[n++].zip = zip;
                        } catch (ZipException e) {
                        } catch (IOException e) {
                                // Ignore exceptions, at least for now...
                        }
                    } else {
                        path[n] = new ClassPathEntry();
                        path[n++].dir = file;
                    }
                }
            }
        }
        // Trim class path to exact size
        ClassPathEntry[] result = new ClassPathEntry[n];
        System.arraycopy((Object)path, 0, (Object)result, 0, n);
        return result;
    }

    private static void addClasses(int rootLen, File dir, Vector list, Hashtable roots) {
        String[] files = dir.list();
        for (int i = 0; i < files.length; i++) {
            File file = new File(dir,files[i]);
            if (file.isDirectory()) {

                // Does our list of roots contain this directory? Must
                // use a case-insensitive compare...

                String path = file.getPath().toLowerCase();
                if (roots.get(path) == null) {

                    // No, so add it...

                    addClasses(rootLen,file,list,roots);
                }
            } else {
                String name = file.getPath();
                int index = name.lastIndexOf(".class");
                if (index >= 0) {
                    name = name.replace(File.separatorChar,'.').substring(rootLen,index);
                    list.addElement(name);
                }
            }
        }
    }
}

class ClassPathEntry {
    File dir;
    ZipFile zip;
}

class StringComparator implements Comparator {

    public int compare(Object o1, Object o2) {
        String s1 = (String) o1;
        String s2 = (String) o2;
        return s1.compareTo(s2);
    }
}
