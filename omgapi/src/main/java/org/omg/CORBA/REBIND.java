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
 * <code>REBIND</code> is raised when the current effective RebindPolicy, 
 * has a value of NO_REBIND or NO_RECONNECT and an invocation on a bound 
 * object reference results in a LocateReply message with status
 * OBJECT_FORWARD or a Reply message with status LOCATION_FORWARD.
 * This exception is also raised if the current effective RebindPolicy has
 * a value of NO_RECONNECT and a connection must be reopened.
 *
 * @see <A href="../../../../guide/idl/jidlExceptions.html">documentation on
 *      Java&nbsp;IDL exceptions</A>
 * @version 1.0, 03/05/2004
 * @since   J2SE 1.5
 */

public final class REBIND extends SystemException {

    /**
     * Constructs an <code>REBIND</code> exception with
     * minor code set to 0 and CompletionStatus set to COMPLETED_NO.
     */
    public REBIND() {
        this("");
    }

    /**
     * Constructs an <code>REBIND</code> exception with the 
     * specified message.
     * 
     * @param detailMessage string containing a detailed message.
     */
    public REBIND(String detailMessage) {
        this(detailMessage, 0, CompletionStatus.COMPLETED_NO);
    }

    /**
     * Constructs an <code>REBIND</code> exception with the 
     * specified minor code and completion status.
     * 
     * @param minorCode minor code.
     * @param completionStatus completion status.
     */
    public REBIND(int minorCode, 
                  CompletionStatus completionStatus) {
        this("", minorCode, completionStatus);
    }

    /**
     * Constructs an <code>REBIND</code> exception with the 
     * specified message, minor code, and completion status.
     * 
     * @param detailMessage string containing a detailed message.
     * @param minorCode minor code.
     * @param completionStatus completion status.
     */
    public REBIND(String detailMessage, 
                  int minorCode, 
                  CompletionStatus completionStatus) {
        super(detailMessage, minorCode, completionStatus);
    }
}
