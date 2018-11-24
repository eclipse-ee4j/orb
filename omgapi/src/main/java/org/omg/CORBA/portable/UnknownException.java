/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 1998-1999 IBM Corp. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.omg.CORBA.portable;

/**
 * The org.omg.CORBA.portable.UnknownException is used for reporting unknown exceptions between ties and ORBs and
 * between ORBs and stubs. It provides a Java representation of an UNKNOWN system exception that has an
 * UnknownExceptionInfo service context. If the CORBA system exception org.omg.CORBA.portable.UnknownException is
 * thrown, then the stub does one of the following: (1) Translates it to org.omg.CORBA.UNKNOWN. (2) Translates it to the
 * nested exception that the UnknownException contains. (3) Passes it on directly to the user.
 */
public class UnknownException extends org.omg.CORBA.SystemException {
    /**
     * A throwable--the original exception that was wrapped in a CORBA UnknownException.
     */
    public Throwable originalEx;

    /**
     * Constructs an UnknownException object.
     *
     * @param ex a Throwable object--to be wrapped in this exception.
     */
    public UnknownException(Throwable ex) {
        super("", 0, org.omg.CORBA.CompletionStatus.COMPLETED_MAYBE);
        originalEx = ex;
    }
}
