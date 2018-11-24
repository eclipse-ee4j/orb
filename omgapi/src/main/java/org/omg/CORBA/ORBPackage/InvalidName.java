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
 * The <code>InvalidName</code> exception is raised when <code>ORB.resolve_initial_references</code> is passed a name
 * for which there is no initial reference.
 *
 * @see org.omg.CORBA.ORB#resolve_initial_references(String)
 * @version 1.6, 03/18/98
 * @since JDK1.2
 */

// @SuppressWarnings({"serial"})
final public class InvalidName extends org.omg.CORBA.UserException {
    /**
     * Constructs an <code>InvalidName</code> exception with no reason message.
     */
    public InvalidName() {
        super();
    }

    /**
     * Constructs an <code>InvalidName</code> exception with the specified reason message.
     *
     * @param reason the String containing a reason message
     */
    public InvalidName(String reason) {
        super(reason);
    }
}
