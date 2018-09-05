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

/** This annotation is applied to a method to indicate what CompletionStatus to use
 * for creating a CORBA exception.  It is an error to apply this annotation to a method
 * that does not return a subclass of SystemException.
 */
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface CS {
    /** The CORBA CompletionStatus to be used for this method.
     * Can only be used for a method that returns a subclass of SystemException.
     */
    CSValue value() default CSValue.NO ;
}

