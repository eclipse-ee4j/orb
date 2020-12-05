/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Eclipse Distribution License
 * v. 1.0 which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License v. 2.0 are satisfied: GNU General Public License v2.0
 * w/Classpath exception which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR BSD-3-Clause OR GPL-2.0 WITH
 * Classpath-exception-2.0
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
