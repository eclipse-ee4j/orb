/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.omg.CORBA.TypeCodePackage;

/**
 * Provides the <code>TypeCode</code> operations <code>member_name()</code>, <code>member_type()</code>, and
 * <code>member_label</code>. These methods raise <code>Bounds</code> when the index parameter is greater than or equal
 * to the number of members constituting the type.
 *
 * @version 1.7, 03/18/98
 * @since JDK1.2
 */

// @SuppressWarnings({"serial"})
public final class Bounds extends org.omg.CORBA.UserException {

    /**
     * Constructs a <code>Bounds</code> exception with no reason message.
     */
    public Bounds() {
        super();
    }

    /**
     * Constructs a <code>Bounds</code> exception with the specified reason message.
     *
     * @param reason the String containing a reason message
     */
    public Bounds(String reason) {
        super(reason);
    }
}
