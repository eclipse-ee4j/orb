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
 * This exception indicates that an implementation limit was exceeded in the ORB run time. For example, an ORB may reach
 * the maximum number of references it can hold simultaneously in an address space, the size of a parameter may have
 * exceeded the allowed maximum, or an ORB may impose a maximum on the number of clients or servers that can run
 * simultaneously.
 * <P>
 * It contains a minor code, which gives more detailed information about what caused the exception, and a completion
 * status. It may also contain a string describing the exception.
 *
 * @see <A href="../../../../guide/idl/jidlExceptions.html">documentation on Java&nbsp;IDL exceptions</A>
 * @version 1.16, 09/09/97
 * @since JDK1.2
 */

public final class IMP_LIMIT extends SystemException {
    /**
     * Constructs an <code>IMP_LIMIT</code> exception with a default minor code of 0 and a completion state of COMPLETED_NO.
     */
    public IMP_LIMIT() {
        this("");
    }

    /**
     * Constructs an <code>IMP_LIMIT</code> exception with the specified detail message, a minor code of 0, and a completion
     * state of COMPLETED_NO.
     *
     * @param s the String containing a detail message
     */
    public IMP_LIMIT(String s) {
        this(s, 0, CompletionStatus.COMPLETED_NO);
    }

    /**
     * Constructs an <code>IMP_LIMIT</code> exception with the specified minor code and completion status.
     *
     * @param minor the minor code
     * @param completed the completion status
     */
    public IMP_LIMIT(int minor, CompletionStatus completed) {
        this("", minor, completed);
    }

    /**
     * Constructs an <code>IMP_LIMIT</code> exception with the specified detail message, minor code, and completion status.
     * A detail message is a String that describes this particular exception.
     *
     * @param s the String containing a detail message
     * @param minor the minor code
     * @param completed the completion status
     */
    public IMP_LIMIT(String s, int minor, CompletionStatus completed) {
        super(s, minor, completed);
    }
}
