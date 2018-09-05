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
* A user exception thrown when a policy error occurs.  A <code>PolicyError</code>
* exception may include one of the following policy error reason codes
* defined in the org.omg.CORBA package: BAD_POLICY, BAD_POLICY_TYPE,
* BAD_POLICY_VALUE, UNSUPPORTED_POLICY, UNSUPPORTED_POLICY_VALUE.
*/

// @SuppressWarnings({"serial"})
public final class PolicyError extends org.omg.CORBA.UserException {

    /** 
     * The reason for the <code>PolicyError</code> exception being thrown.
     * @serial
     */
    public short reason;

    /**
     * Constructs a default <code>PolicyError</code> user exception
     * with no reason code and an empty reason detail message.
     */
    public PolicyError() {
        super();
    }

    /**
     * Constructs a <code>PolicyError</code> user exception
     * initialized with the given reason code and an empty reason detail message.
     * @param __reason the reason code.
     */
    public PolicyError(short __reason) {
        super();
        reason = __reason;
    }

    /**
     * Constructs a <code>PolicyError</code> user exception
     * initialized with the given reason detail message and reason code.
     * @param reason_string the reason detail message.
     * @param __reason the reason code.
     */
    public PolicyError(String reason_string, short __reason) {
        super(reason_string);
        reason = __reason;
    }
}
