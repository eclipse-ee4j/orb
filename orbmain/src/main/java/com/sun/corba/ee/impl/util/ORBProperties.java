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

package com.sun.corba.ee.impl.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;

public class ORBProperties {

    public static final String ORB_CLASS = "org.omg.CORBA.ORBClass=com.sun.corba.ee.impl.orb.ORBImpl";
    public static final String ORB_SINGLETON_CLASS = "org.omg.CORBA.ORBSingletonClass=com.sun.corba.ee.impl.orb.ORBSingleton";

    public static void main(String[] args) {

        try {
            // Check if orb.properties exists
            String javaHome = System.getProperty("java.home");
            File propFile = new File(javaHome + File.separator + "lib" + File.separator + "orb.properties");

            if (propFile.exists())
                return;

            // Write properties to orb.properties
            FileOutputStream out = new FileOutputStream(propFile);
            PrintWriter pw = new PrintWriter(out);

            try {
                pw.println(ORB_CLASS);
                pw.println(ORB_SINGLETON_CLASS);
            } finally {
                pw.close();
                out.close();
            }

        } catch (Exception ex) {
        }

    }
}
