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
import java.util.Date;

public class Version {

    public static final String  PROJECT_NAME = "RMI-IIOP";
    public static final String  VERSION = "1.0";
    public static final String  BUILD = "0.0";
    public static final String  BUILD_TIME = "unknown";
    public static final String  FULL = PROJECT_NAME + " " + VERSION + " (" 
        + BUILD_TIME + ")";
    
    public static String asString () {
        return FULL;
    }
    
    public static void main (String[] args) {
        System.out.println(FULL);
    }
}
