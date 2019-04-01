/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 1997-1999 IBM Corp. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.tools.corba.ee.idl;

// NOTES:
// -capitalize and parseTypeModifier should probably be in the
//  generators package.
// -D58319<daz> Add version() method.

import java.text.MessageFormat;
import java.util.ResourceBundle;

public class ResourceBundleUtil {
    // <d58319>
    /**
     * Fetch the version number of this build of the IDL Parser Framework. This method may be called before or after the
     * framework has been initialized. If the framework is inititialized, the version information is extracted from the
     * message properties object; otherwise, it is extracted from the indicated resouce bundle.
     *
     * @return the version number.
     **/
    public static String getVersion() {
        String version = getMessage("Version.product", getMessage("Version.number"));
        return version;
    } // getVersion

    //////////////
    // Message-related methods

    public static String getMessage(String key, String... fill) {
        String pattern = getResourceBundle().getString(key);
        MessageFormat mf = new MessageFormat(pattern);
        return mf.format(fill, new StringBuffer(), null).toString();
    } // getMessage

    /**
     * Register a ResourceBundle. This file will be searched for in the CLASSPATH.
     */
    public static void registerResourceBundle(ResourceBundle bundle) {
        if (bundle != null)
            fBundle = bundle;
    } // registerResourceBundle

    /** Gets the current ResourceBundle. */
    public static ResourceBundle getResourceBundle() {
        if (fBundle == null) {
            fBundle = ResourceBundle.getBundle("com.sun.tools.corba.ee.idl.idl");
        }
        return fBundle;
    } // getResourceBundle

    private static ResourceBundle fBundle = null;
} // class ResourceBundleUtil
