/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.omg.CORBA.ORBPackage;

/**
 * InconsistentTypeCode is thrown when an attempt is made to create a dynamic any with a type code that does not match
 * the particular subclass of <code>DynAny</code>.
 */
// @SuppressWarnings({"serial"})
public final class InconsistentTypeCode extends org.omg.CORBA.UserException {
    /**
     * Constructs an <code>InconsistentTypeCode</code> user exception with no reason message.
     */
    public InconsistentTypeCode() {
        super();
    }

    /**
     * Constructs an <code>InconsistentTypeCode</code> user exception with the specified reason message.
     *
     * @param reason The String containing a reason message
     */
    public InconsistentTypeCode(String reason) {
        super(reason);
    }
}
