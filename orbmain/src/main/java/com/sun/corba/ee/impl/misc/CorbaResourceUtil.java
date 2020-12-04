/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates.
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
