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
 * The <code>ACTIVITY_REQUIRED</code> system exception may be raised on any method for which an Activity context is
 * required. It indicates that an Activity context was necessary to perform the invoked operation, but one was not found
 * associated with the calling thread.
 *
 * @see <A href="../../../../guide/idl/jidlExceptions.html">documentation on Java&nbsp;IDL exceptions</A>
 * @version 1.0, 03/05/2004
 * @since J2SE 1.5
 */

public final class ACTIVITY_REQUIRED extends SystemException {

    /**
     * Constructs an <code>ACTIVITY_REQUIRED</code> exception with minor code set to 0 and CompletionStatus set to
     * COMPLETED_NO.
     */
    public ACTIVITY_REQUIRED() {
        this("");
    }

    /**
     * Constructs an <code>ACTIVITY_REQUIRED</code> exception with the specified message.
     *
     * @param detailMessage string containing a detailed message.
     */
    public ACTIVITY_REQUIRED(String detailMessage) {
        this(detailMessage, 0, CompletionStatus.COMPLETED_NO);
    }

    /**
     * Constructs an <code>ACTIVITY_REQUIRED</code> exception with the specified minor code and completion status.
     *
     * @param minorCode minor code.
     * @param completionStatus completion status.
     */
    public ACTIVITY_REQUIRED(int minorCode, CompletionStatus completionStatus) {
        this("", minorCode, completionStatus);
    }

    /**
     * Constructs an <code>ACTIVITY_REQUIRED</code> exception with the specified message, minor code, and completion status.
     *
     * @param detailMessage string containing a detailed message.
     * @param minorCode minor code.
     * @param completionStatus completion status.
     */
    public ACTIVITY_REQUIRED(String detailMessage, int minorCode, CompletionStatus completionStatus) {
        super(detailMessage, minorCode, completionStatus);
    }
}
