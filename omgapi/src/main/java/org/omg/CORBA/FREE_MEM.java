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
 * Exception thrown when the ORB failed in an attempt to free dynamic memory, for example because of heap corruption or
 * memory segments being locked.
 * <P>
 * It contains a minor code, which gives more detailed information about what caused the exception, and a completion
 * status. It may also contain a string describing the exception.
 *
 * @see <A href="../../../../guide/idl/jidlExceptions.html">documentation on Java&nbsp;IDL exceptions</A>
 * @version 1.16, 09/09/97
 * @since JDK1.2
 */

public final class FREE_MEM extends SystemException {
    /**
     * Constructs a <code>FREE_MEM</code> exception with a default minor code of 0 and a completion state of COMPLETED_NO.
     */
    public FREE_MEM() {
        this("");
    }

    /**
     * Constructs a <code>FREE_MEM</code> exception with the specified detail message, a minor code of 0, and a completion
     * state of COMPLETED_NO.
     *
     * @param s the String containing a detail message
     */
    public FREE_MEM(String s) {
        this(s, 0, CompletionStatus.COMPLETED_NO);
    }

    /**
     * Constructs a <code>FREE_MEM</code> exception with the specified minor code and completion status.
     *
     * @param minor the minor code
     * @param completed the completion status
     */
    public FREE_MEM(int minor, CompletionStatus completed) {
        this("", minor, completed);
    }

    /**
     * Constructs a <code>FREE_MEM</code> exception with the specified detail message, minor code, and completion status. A
     * detail message is a String that describes this particular exception.
     *
     * @param s the String containing a detail message
     * @param minor the minor code
     * @param completed the completion status
     */
    public FREE_MEM(String s, int minor, CompletionStatus completed) {
        super(s, minor, completed);
    }
}
