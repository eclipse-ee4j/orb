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
 * This exception indicates that even though the operation that was invoked exists (it has an IDL definition), no
 * implementation for that operation exists. <tt>NO_IMPLEMENT</tt> can, for example, be raised by an ORB if a client
 * asks for an object's type definition from the interface repository, but no interface repository is provided by the
 * ORB.
 * <P>
 * It contains a minor code, which gives more detailed information about what caused the exception, and a completion
 * status. It may also contain a string describing the exception.
 * <P>
 * See the section <A href="../../../../guide/idl/jidlExceptions.html#minorcodemeanings">Minor Code Meanings</A> to see
 * the minor codes for this exception.
 *
 * @version 1.17, 09/09/97
 * @since JDK1.2
 */

public final class NO_IMPLEMENT extends SystemException {
    /**
     * Constructs a <code>NO_IMPLEMENT</code> exception with a default minor code of 0, a completion state of
     * CompletionStatus.COMPLETED_NO, and a null description.
     */
    public NO_IMPLEMENT() {
        this("");
    }

    /**
     * Constructs a <code>NO_IMPLEMENT</code> exception with the specified description message, a minor code of 0, and a
     * completion state of COMPLETED_NO.
     *
     * @param s the String containing a description of the exception
     */
    public NO_IMPLEMENT(String s) {
        this(s, 0, CompletionStatus.COMPLETED_NO);
    }

    /**
     * Constructs a <code>NO_IMPLEMENT</code> exception with the specified minor code and completion status.
     *
     * @param minor an <code>int</code> specifying the minor code
     * @param completed a <code>CompletionStatus</code> instance indicating the completion status
     */
    public NO_IMPLEMENT(int minor, CompletionStatus completed) {
        this("", minor, completed);
    }

    /**
     * Constructs a <code>NO_IMPLEMENT</code> exception with the specified description message, minor code, and completion
     * status.
     *
     * @param s the String containing a description message
     * @param minor an <code>int</code> specifying the minor code
     * @param completed a <code>CompletionStatus</code> instance indicating the completion status
     */
    public NO_IMPLEMENT(String s, int minor, CompletionStatus completed) {
        super(s, minor, completed);
    }
}
