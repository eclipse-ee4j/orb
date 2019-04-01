/*
 * Copyright (c) 1994, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.rmic.tools.javac;

import org.glassfish.rmic.tools.java.*;
// JCOV
// end JCOV

import java.util.*;
import java.text.MessageFormat;

/**
 * Main program of the Java compiler
 *
 * WARNING: The contents of this source file are not part of any supported API. Code that depends on them does so at its
 * own risk: they are subject to change or removal without notice.
 *
 * @deprecated As of J2SE 1.3, the preferred way to compile Java language sources is by using the new compiler,
 * com.org.glassfish.rmic.tools.javac.Main.
 */
@Deprecated
public class Main implements Constants {

    private static ResourceBundle messageRB;

    /**
     * Initialize ResourceBundle
     */
    private static void initResource() {
        try {
            messageRB = ResourceBundle.getBundle("org.glassfish.rmic.tools.javac.resources.javac");
        } catch (MissingResourceException e) {
            throw new Error("Fatal: Resource for javac is missing");
        }
    }

    public static String getText(String key, String fixed) {
        return getText(key, fixed, null);
    }

    static String getText(String key, String fixed1, String fixed2) {
        return getText(key, fixed1, fixed2, null);
    }

    static String getText(String key, String fixed1, String fixed2, String fixed3) {
        if (messageRB == null) {
            initResource();
        }
        try {
            String message = messageRB.getString(key);
            return MessageFormat.format(message, fixed1, fixed2, fixed3);
        } catch (MissingResourceException e) {
            if (fixed1 == null)
                fixed1 = "null";
            if (fixed2 == null)
                fixed2 = "null";
            if (fixed3 == null)
                fixed3 = "null";
            String message = "JAVAC MESSAGE FILE IS BROKEN: key={0}, arguments={1}, {2}, {3}";
            return MessageFormat.format(message, key, fixed1, fixed2, fixed3);
        }
    }

}
