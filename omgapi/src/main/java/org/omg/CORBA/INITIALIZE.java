/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.omg.CORBA;

/**
 * Exception thrown when an ORB has encountered a failure during its initialization, such as failure to acquire
 * networking resources or detecting a configuration error.
 * <P>
 * It contains a minor code, which gives more detailed information about what caused the exception, and a completion
 * status. It may also contain a string describing the exception.
 *
 * @see <A href="../../../../guide/idl/jidlExceptions.html">documentation on Java&nbsp;IDL exceptions</A>
 * @version 1.19, 09/09/97
 * @since JDK1.2
 */

public final class INITIALIZE extends SystemException {
    /**
     * Constructs an <code>INITIALIZE</code> exception with a default minor code of 0 and a completion state of
     * <code>CompletionStatus.COMPLETED_NO</code>.
     */
    public INITIALIZE() {
        this("");
    }

    /**
     * Constructs an <code>INITIALIZE</code> exception with the specified detail message, a minor code of 0, and a
     * completion state of <code>CompletionStatus.COMPLETED_NO</code>.
     *
     * @param s the String containing a detail message
     */
    public INITIALIZE(String s) {
        this(s, 0, CompletionStatus.COMPLETED_NO);
    }

    /**
     * Constructs an <code>INITIALIZE</code> exception with the specified minor code and completion status.
     *
     * @param minor the minor code
     * @param completed an instance of <code>CompletionStatus</code> indicating the completion status of the method that
     * threw this exception
     */
    public INITIALIZE(int minor, CompletionStatus completed) {
        this("", minor, completed);
    }

    /**
     * Constructs an <code>INITIALIZE</code> exception with the specified detail message, minor code, and completion status.
     * A detail message is a String that describes this particular exception.
     *
     * @param s the String containing a detail message
     * @param minor the minor code
     * @param completed an instance of <code>CompletionStatus</code> indicating the completion status of the method that
     * threw this exception
     */
    public INITIALIZE(String s, int minor, CompletionStatus completed) {
        super(s, minor, completed);
    }
}
