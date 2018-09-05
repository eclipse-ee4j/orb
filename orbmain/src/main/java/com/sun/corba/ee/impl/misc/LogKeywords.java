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
/**
 * All the Keywords that will be used in Logging Messages for CORBA need to
 * be defined here. The LogKeywords will be useful for searching log messages
 * based on the standard keywords, it is also useful to work with LogAnalyzing
 * tools.
 * We will try to standardize these keywords in JSR 117 Logging
 */
public class LogKeywords {

    /** 
     ** Keywords for Lifecycle Loggers. 
     ** _REVISIT_ After it is clearly defined in JSR 117
     **/
    public final static String LIFECYCLE_CREATE     = "<<LIFECYCLE CREATE>>";
    public final static String LIFECYCLE_INITIALIZE = "<<LIFECYCLE INITIALIZE>>";
    public final static String LIFECYCLE_SHUTDOWN   = "<<LIFECYCLE SHUTDOWN>>";
    public final static String LIFECYCLE_DESTROY    = "<<LIFECYCLE DESTROY>>";
    public final static String NAMING_RESOLVE       = "<<NAMING RESOLVE>>";
    public final static String NAMING_LIST          = "<<NAMING LIST>>";
    public final static String NAMING_BIND          = "<<NAMING BIND>>";
    public final static String NAMING_UNBIND        = "<<NAMING UNBIND>>";
    public final static String NAMING_REBIND        = "<<NAMING REBIND>>";
}

    
    

     

