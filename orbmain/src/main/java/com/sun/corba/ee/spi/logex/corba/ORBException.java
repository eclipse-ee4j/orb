/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.spi.logex.corba ;

/**
 *
 * @author ken
 */
import java.lang.annotation.Documented ;
import java.lang.annotation.Target ;
import java.lang.annotation.ElementType ;
import java.lang.annotation.Retention ;
import java.lang.annotation.RetentionPolicy ;

/** This annotation is applied to an interface or abstract class that is used
 * to define logging and/or constructing CORBA exceptions.
 */
@Documented
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ORBException {
    /** Return true if this is an OMG exception with the OMG VMCID, 
     * false if it is using the old SUN VMCID.
     * @return {@code false} by default
     */
    boolean omgException() default false ;

    /** Return the group ID to be used in computing the message ID.
     * @return The group ID
     */
    int group() ;
}

