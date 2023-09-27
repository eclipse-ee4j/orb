/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2020 Payara Services Ltd.
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

package com.sun.corba.ee.impl.naming.cosnaming;

import java.io.*;
import org.omg.CosNaming.NameComponent;

public class NamingUtils {
    // Do not instantiate this class
    private NamingUtils() {
    };

    /**
     * Debug flag which must be true for debug streams to be created and dprint output to be generated.
     */
    public static boolean debug = false;

    /**
     * Prints the message to the debug stream if debugging is enabled.
     * 
     * @param msg the debug message to print.
     */
    public static void dprint(String msg) {
        if (debug && debugStream != null)
            debugStream.println(msg);
    }

    /**
     * Prints the message to the error stream (System.err is default).
     * 
     * @param msg the error message to print.
     */
    public static void errprint(String msg) {
        if (errStream != null)
            errStream.println(msg);
        else
            System.err.println(msg);
    }

    /**
     * Prints the stacktrace of the supplied exception to the error stream.
     * 
     * @param e any Java exception.
     */
    public static void printException(java.lang.Exception e) {
        if (errStream != null)
            e.printStackTrace(errStream);
        else
            e.printStackTrace();
    }

    /**
     * Create a debug print stream to the supplied log file.
     * 
     * @param logFile the file to which debug output will go.
     * @exception IOException thrown if the file cannot be opened for output.
     */
    public static void makeDebugStream(File logFile) throws java.io.IOException {
        // Create an outputstream for debugging
        java.io.OutputStream logOStream = new java.io.FileOutputStream(logFile);
        java.io.DataOutputStream logDStream = new java.io.DataOutputStream(logOStream);
        debugStream = new java.io.PrintStream(logDStream);

        // Emit first message
        debugStream.println("Debug Stream Enabled.");
    }

    /**
     * Create a error print stream to the supplied file.
     * 
     * @param errFile the file to which error messages will go.
     * @exception IOException thrown if the file cannot be opened for output.
     */
    public static void makeErrStream(File errFile) throws java.io.IOException {
        if (debug) {
            // Create an outputstream for errors
            java.io.OutputStream errOStream = new java.io.FileOutputStream(errFile);
            java.io.DataOutputStream errDStream = new java.io.DataOutputStream(errOStream);
            errStream = new java.io.PrintStream(errDStream);
            dprint("Error stream setup completed.");
        }
    }

    /**
     * A utility method that takes Array of NameComponent and converts into a directory structured name in the format of
     * /id1.kind1/id2.kind2.. This is used mainly for Logging.
     */
    static String getDirectoryStructuredName(NameComponent[] name) {
        StringBuilder directoryStructuredName = new StringBuilder("/");
        for (NameComponent component : name) {
            directoryStructuredName.append(component.id).append(".").append(component.kind);
        }
        return directoryStructuredName.toString();
    }

    /**
     * The debug printstream.
     */
    public static java.io.PrintStream debugStream;

    /**
     * The error printstream.
     */
    public static java.io.PrintStream errStream;
}
