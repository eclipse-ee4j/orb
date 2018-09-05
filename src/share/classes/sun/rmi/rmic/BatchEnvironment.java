/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 1998 IBM Corp. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package sun.rmi.rmic;

import java.io.File;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Vector;
import sun.tools.java.ClassPath;

/**
 * BatchEnvironment for rmic extends javac's version in four ways:
 * 1. It overrides errorString() to handle looking for rmic-specific
 * error messages in rmic's resource bundle
 * 2. It provides a mechanism for recording intermediate generated
 * files so that they can be deleted later.
 * 3. It holds a reference to the Main instance so that generators
 * can refer to it.
 * 4. It provides access to the ClassPath passed to the constructor.
 */

@SuppressWarnings({"deprecation"})
public class BatchEnvironment extends sun.tools.javac.BatchEnvironment {

    /** instance of Main which created this environment */
    private Main main;

    /**
     * Create a ClassPath object for rmic from a class path string.
     */
    public static ClassPath createClassPath(String classPathString) {
        ClassPath[] paths = classPaths(null, classPathString, null, null);
        return paths[1];
    }

    /**
     * Create a ClassPath object for rmic from the relevant command line
     * options for class path, boot class path, and extension directories.
     */
    public static ClassPath createClassPath(String classPathString,
                                            String sysClassPathString,
                                            String extDirsString)
    {
        ClassPath[] paths = classPaths(null,
                                       classPathString,
                                       sysClassPathString,
                                       extDirsString);
        return paths[1];
    }

    /**
     * Create a BatchEnvironment for rmic with the given class path,
     * stream for messages and Main.
     */
    public BatchEnvironment(OutputStream out, ClassPath path, Main main) {
        super(out, path);
        this.main = main;
    }

    /**
     * Get the instance of Main which created this environment.
     */
    public Main getMain() {
        return main;
    }

    /**
     * Get the ClassPath.
     */
    public ClassPath getClassPath() {
        return sourcePath;
    }

    /** list of generated source files created in this environment */
    private Vector generatedFiles = new Vector();

    /**
     * Remember a generated source file generated so that it
     * can be removed later, if appropriate.
     */
    public void addGeneratedFile(File file) {
        generatedFiles.addElement(file);
    }

    /**
     * Delete all the generated source files made during the execution
     * of this environment (those that have been registered with the
     * "addGeneratedFile" method).
     */
    public void deleteGeneratedFiles() {
        synchronized(generatedFiles) {
            Enumeration enumeration = generatedFiles.elements();
            while (enumeration.hasMoreElements()) {
                File file = (File) enumeration.nextElement();
                file.delete();
            }
            generatedFiles.removeAllElements();
        }
    }

    /**
     * Release resources, if any.
     */
    public void shutdown() {
        main = null;
        generatedFiles = null;
        super.shutdown();
    }

    /**
     * Return the formatted, localized string for a named error message
     * and supplied arguments.  For rmic error messages, with names that
     * being with "rmic.", look up the error message in rmic's resource
     * bundle; otherwise, defer to java's superclass method.
     */
    public String errorString(String err,
                              Object arg0, Object arg1, Object arg2)
    {
        if (err.startsWith("rmic.") || err.startsWith("warn.rmic.")) {
            String result =  Main.getText(err,
                                          (arg0 != null ? arg0.toString() : null),
                                          (arg1 != null ? arg1.toString() : null),
                                          (arg2 != null ? arg2.toString() : null));

            if (err.startsWith("warn.")) {
                result = "warning: " + result;
            }
            return result;
        } else {
            return super.errorString(err, arg0, arg1, arg2);
        }
    }
    public void reset() {
    }
}
