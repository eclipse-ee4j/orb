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

package com.sun.corba.ee.impl.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;

public class ORBProperties {

    public static final String ORB_CLASS = 
        "org.omg.CORBA.ORBClass=com.sun.corba.ee.impl.orb.ORBImpl";
    public static final String ORB_SINGLETON_CLASS = 
        "org.omg.CORBA.ORBSingletonClass=com.sun.corba.ee.impl.orb.ORBSingleton";
    
    public static void main (String[] args) {

        try {
            // Check if orb.properties exists
            String javaHome = System.getProperty("java.home");
            File propFile = new File(javaHome + File.separator
                                     + "lib" + File.separator
                                     + "orb.properties");
            
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

        } catch (Exception ex) { }
        
    }
}
