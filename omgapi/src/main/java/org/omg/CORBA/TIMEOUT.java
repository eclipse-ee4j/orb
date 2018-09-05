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
 * <code>TIMEOUT</code> is raised when no delivery has been made and the
 * specified time-to-live period has been exceeded. It is a standard system
 * exception because time-to-live QoS can be applied to any invocation.
 *
 * @see <A href="../../../../guide/idl/jidlExceptions.html">documentation on
 *      Java&nbsp;IDL exceptions</A>
 * @version 1.0, 03/05/2004
 * @since   J2SE 1.5
 */

public final class TIMEOUT extends SystemException {

    /**
     * Constructs an <code>TIMEOUT</code> exception with
     * minor code set to 0 and CompletionStatus set to COMPLETED_NO.
     */
    public TIMEOUT() {
        this("");
    }

    /**
     * Constructs an <code>TIMEOUT</code> exception with the 
     * specified message.
     * 
     * @param detailMessage string containing a detailed message.
     */
    public TIMEOUT(String detailMessage) {
        this(detailMessage, 0, CompletionStatus.COMPLETED_NO);
    }

    /**
     * Constructs an <code>TIMEOUT</code> exception with the 
     * specified minor code and completion status.
     * 
     * @param minorCode minor code.
     * @param completionStatus completion status.
     */
    public TIMEOUT(int minorCode, 
                   CompletionStatus completionStatus) {
        this("", minorCode, completionStatus);
    }

    /**
     * Constructs an <code>TIMEOUT</code> exception with the 
     * specified message, minor code, and completion status.
     * 
     * @param detailMessage string containing a detailed message.
     * @param minorCode minor code.
     * @param completionStatus completion status.
     */
    public TIMEOUT(String detailMessage, 
                   int minorCode, 
                   CompletionStatus completionStatus) {
        super(detailMessage, minorCode, completionStatus);
    }
}
