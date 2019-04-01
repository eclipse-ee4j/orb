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
 * Exception raised whenever an invocation on a deleted object was performed. It is an authoritative "hard" fault
 * report. Anyone receiving it is allowed (even expected) to delete all copies of this object reference and to perform
 * other appropriate "final recovery" style procedures. Bridges forward this exception to clients, also destroying any
 * records they may hold (for example, proxy objects used in reference translation). The clients could in turn purge any
 * of their own data structures.
 * <P>
 * It contains a minor code, which gives more detailed information about what caused the exception, and a completion
 * status. It may also contain a string describing the exception.
 * <P>
 * See the section <A href="../../../../guide/idl/jidlExceptions.html#minorcodemeanings">Minor Code Meanings</A> to see
 * the minor codes for this exception.
 *
 * @see <A href="../../../../guide/idl/jidlExceptions.html">documentation on Java&nbsp;IDL exceptions</A>
 * @version 1.13, 09/09/97
 * @since JDK1.2
 */

public final class OBJECT_NOT_EXIST extends SystemException {
    /**
     * Constructs an <code>OBJECT_NOT_EXIST</code> exception with a default minor code of 0, a completion state of
     * CompletionStatus.COMPLETED_NO, and a null description.
     */
    public OBJECT_NOT_EXIST() {
        this("");
    }

    /**
     * Constructs an <code>OBJECT_NOT_EXIST</code> exception with the specified description, a minor code of 0, and a
     * completion state of COMPLETED_NO.
     *
     * @param s the String containing a description message
     */
    public OBJECT_NOT_EXIST(String s) {
        this(s, 0, CompletionStatus.COMPLETED_NO);
    }

    /**
     * Constructs an <code>OBJECT_NOT_EXIST</code> exception with the specified minor code and completion status.
     *
     * @param minor the minor code
     * @param completed the completion status
     */
    public OBJECT_NOT_EXIST(int minor, CompletionStatus completed) {
        this("", minor, completed);
    }

    /**
     * Constructs an <code>OBJECT_NOT_EXIST</code> exception with the specified description message, minor code, and
     * completion status.
     *
     * @param s the String containing a description message
     * @param minor the minor code
     * @param completed the completion status
     */
    public OBJECT_NOT_EXIST(String s, int minor, CompletionStatus completed) {
        super(s, minor, completed);
    }
}
