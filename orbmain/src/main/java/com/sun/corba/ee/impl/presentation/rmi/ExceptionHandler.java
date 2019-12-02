/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2019 Payara Services Ltd.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.presentation.rmi ;

import org.omg.CORBA_2_3.portable.OutputStream ;

import org.omg.CORBA.portable.ApplicationException ;

public interface ExceptionHandler 
{
    /** Return true iff cls is an exception class that is 
     * assignment compatible with an exception declared
     * on the method used to create this ExceptionHandler.
     * @param cls Class to check
     * @return If class is a declared exception
     */
    boolean isDeclaredException( Class cls );

    /** Write the exception ex to os.  ex must be assignment 
     * compatible with an exception
     * declared on the method used to create this 
     * ExceptionHandler.
     * @param os Stream to write to.
     * @param ex Exception to write.
     */
    void writeException( OutputStream os, Exception ex );

    /** Read the exception contained in the InputStream
     * in the ApplicationException.  If ae represents
     * an exception that is assignment compatible with
     * an exception on the method used to create this
     * exception handler, return the exception, 
     * otherwise return an UnexpectedException wrapping 
     * the exception in ae.
     * @param ae Exception to get input stream to read exception from.
     * @return Exception from from stream.
     */
    Exception readException( ApplicationException ae );
}
