/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 1993-1997 IBM Corp. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package javax.rmi.CORBA;

import java.io.File;
import java.io.FileInputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Properties;

class GetORBPropertiesFileAction implements PrivilegedAction {
    private boolean debug = false;

    public GetORBPropertiesFileAction() {
    }

    private String getSystemProperty(final String name) {
        // This will not throw a SecurityException because this
        // class was loaded from rt.jar using the bootstrap classloader.
        String propValue = (String) AccessController.doPrivileged(new PrivilegedAction() {
            public java.lang.Object run() {
                return System.getProperty(name);
            }
        });

        return propValue;
    }

    private void getPropertiesFromFile(Properties props, String fileName) {
        try {
            File file = new File(fileName);
            if (!file.exists())
                return;

            FileInputStream in = new FileInputStream(file);

            try {
                props.load(in);
            } finally {
                in.close();
            }
        } catch (Exception exc) {
            if (debug)
                System.out.println("ORB properties file " + fileName + " not found: " + exc);
        }
    }

    public Object run() {
        Properties defaults = new Properties();

        String javaHome = getSystemProperty("java.home");
        String fileName = javaHome + File.separator + "lib" + File.separator + "orb.properties";

        getPropertiesFromFile(defaults, fileName);

        Properties results = new Properties(defaults);

        String userHome = getSystemProperty("user.home");
        fileName = userHome + File.separator + "orb.properties";

        getPropertiesFromFile(results, fileName);
        return results;
    }
}
