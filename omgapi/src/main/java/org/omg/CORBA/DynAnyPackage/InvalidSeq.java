/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.omg.CORBA.DynAnyPackage;

/**
 * The InvalidSeq exception is thrown by all operations on dynamic
 * anys that take a sequence (Java array) as an argument, when that
 * sequence is invalid.
 */
// @SuppressWarnings({"serial"})
public final class InvalidSeq
    extends org.omg.CORBA.UserException {

    /**
     * Constructs an <code>InvalidSeq</code> object.
     */
    public InvalidSeq() {
        super();
    }

    /**
     * Constructs an <code>InvalidSeq</code> object.
     * @param reason  a <code>String</code> giving more information
     * regarding the exception.
     */
    public InvalidSeq(String reason) {
        super(reason);
    }
}
