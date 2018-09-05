/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.misc;

import java.util.ResourceBundle;
import java.util.MissingResourceException;

public class CorbaResourceUtil {
    public static String getString(String key) {
        if (!resourcesInitialized) {
            initResources();
        }

        try {
            return resources.getString(key);
        } catch (MissingResourceException ignore) {
        }
        return null;
    }

    public static String getText(String key) {
        String message = getString(key);
        if (message == null) {
            message = "no text found: \"" + key + "\"";
        }
        return message;
    }

    public static String getText(String key, Object... args )
    {
        String format = getString(key);

        if (format == null) {
            StringBuilder sb = new StringBuilder(
                "no text found: key = \"" ) ;
            sb.append( key ) ;
            sb.append( "\", arguments = " ) ;
            
            for (int ctr=0; ctr<args.length; ctr++) {
                if (ctr != 0) {
                    sb.append( ", " ) ;
                }

                sb.append( "\"{" ) ;
                sb.append( ctr ) ;
                sb.append( "}\"" ) ;
            }

            format = sb.toString() ;
        }

        return java.text.MessageFormat.format(format, args);
    }

    private static boolean resourcesInitialized = false;
    private static ResourceBundle resources;

    private static void initResources() {
        try {
            resources =
                ResourceBundle.getBundle("com.sun.corba.ee.impl.resources.sunorb");
            resourcesInitialized = true;
        } catch (MissingResourceException e) {
            throw new Error("fatal: missing resource bundle: " +
                            e.getClassName());
        }
    }

}
