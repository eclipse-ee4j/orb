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
 * Exception thrown when an invocation failed because the caller has insufficient privileges.
 * <P>
 * It contains a minor code, which gives more detailed information about what caused the exception, and a completion
 * status. It may also contain a string describing the exception.
 *
 * @see <A href="../../../../guide/idl/jidlExceptions.html">documentation on Java&nbsp;IDL exceptions</A>
 * @version 1.17, 09/09/97
 * @since JDK1.2
 */

public final class NO_PERMISSION extends SystemException {
    /**
     * Constructs a <code>NO_PERMISSION</code> exception with a default minor code of 0 and a completion state of
     * CompletionStatus.COMPLETED_NO, and a null description.
     */
    public NO_PERMISSION() {
        this("");
    }

    /**
     * Constructs a <code>NO_PERMISSION</code> exception with the specified description, a minor code of 0, and a completion
     * state of COMPLETED_NO.
     *
     * @param s the String containing a description message
     */
    public NO_PERMISSION(String s) {
        this(s, 0, CompletionStatus.COMPLETED_NO);
    }

    /**
     * Constructs a <code>NO_PERMISSION</code> exception with the specified minor code and completion status.
     *
     * @param minor the minor code
     * @param completed the completion status
     */
    public NO_PERMISSION(int minor, CompletionStatus completed) {
        this("", minor, completed);
    }

    /**
     * Constructs a <code>NO_PERMISSION</code> exception with the specified description message, minor code, and completion
     * status.
     *
     * @param s the String containing a description message
     * @param minor the minor code
     * @param completed the completion status
     */
    public NO_PERMISSION(String s, int minor, CompletionStatus completed) {
        super(s, minor, completed);
    }
}
