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
 * This exception is raised if a client attempts to retrieve the result of a deferred synchronous call, but the response
 * for the request is not yet available.
 * <P>
 * It contains a minor code, which gives more detailed information about what caused the exception, and a completion
 * status. It may also contain a string describing the exception.
 *
 * @see <A href="../../../../guide/idl/jidlExceptions.html">documentation on Java&nbsp;IDL exceptions</A>
 * @version 1.17, 09/09/97
 * @since JDK1.2
 */

public final class NO_RESPONSE extends SystemException {
    /**
     * Constructs a <code>NO_RESPONSE</code> exception with a default minor code of 0, a completion state of
     * CompletionStatus.COMPLETED_NO, and a null description.
     */
    public NO_RESPONSE() {
        this("");
    }

    /**
     * Constructs a <code>NO_RESPONSE</code> exception with the specified description message, a minor code of 0, and a
     * completion state of COMPLETED_NO.
     *
     * @param s the String containing a description message
     */
    public NO_RESPONSE(String s) {
        this(s, 0, CompletionStatus.COMPLETED_NO);
    }

    /**
     * Constructs a <code>NO_RESPONSE</code> exception with the specified minor code and completion status.
     *
     * @param minor the minor code
     * @param completed the completion status
     */
    public NO_RESPONSE(int minor, CompletionStatus completed) {
        this("", minor, completed);
    }

    /**
     * Constructs a <code>NO_RESPONSE</code> exception with the specified description message, minor code, and completion
     * status.
     *
     * @param s the String containing a description message
     * @param minor the minor code
     * @param completed the completion status
     */
    public NO_RESPONSE(String s, int minor, CompletionStatus completed) {
        super(s, minor, completed);
    }
}
