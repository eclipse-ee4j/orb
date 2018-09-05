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
 * The CORBA <code>TRANSACTION_MODE</code> exception is thrown
 * by the client ORB if it detects a mismatch between the 
 * InvocationPolicy in the IOR and the chosen invocation path
 * (i.e, direct or routed invocation).
 * It contains a minor code, which gives information about
 * what caused the exception, and a completion status. It may also contain
 * a string describing the exception.
 * The OMG CORBA core 2.4 specification has details.
 *
 * @see <A href="../../../../guide/idl/jidlExceptions.html">documentation on
 * Java&nbsp;IDL exceptions</A>
 * @version     1.0 10/22/2000
 */

public final class TRANSACTION_MODE extends SystemException {
    /**
     * Constructs a <code>TRANSACTION_MODE</code> exception with a default 
     * minor code of 0, a completion state of CompletionStatus.COMPLETED_NO,
     * and a null description.
     */
    public TRANSACTION_MODE() {
        this("");
    }

    /**
     * Constructs a <code>TRANSACTION_MODE</code> exception with the specified
     * description message, a minor code of 0, and a completion state of 
     * COMPLETED_NO.
     * @param s the String containing a detail message
     */
    public TRANSACTION_MODE(String s) {
        this(s, 0, CompletionStatus.COMPLETED_NO);
    }

    /**
     * Constructs a <code>TRANSACTION_MODE</code> exception with the specified
     * minor code and completion status.
     * @param minor the minor code
     * @param completed the completion status
     */
    public TRANSACTION_MODE(int minor, CompletionStatus completed) {
        this("", minor, completed);
    }

    /**
     * Constructs a <code>TRANSACTION_MODE</code> exception with the specified
     * description message, minor code, and completion status.
     * @param s the String containing a description message
     * @param minor the minor code
     * @param completed the completion status
     */
    public TRANSACTION_MODE(String s, int minor, CompletionStatus completed) {
        super(s, minor, completed);
    }
}
