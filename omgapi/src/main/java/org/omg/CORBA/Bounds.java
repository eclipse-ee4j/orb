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
 * A user exception thrown when a parameter is not within the legal bounds for the object that a method is trying to
 * access.
 *
 * @see <A href="../../../../guide/idl/jidlExceptions.html">documentation on Java&nbsp;IDL exceptions</A>
 */

// @SuppressWarnings({"serial"})
public final class Bounds extends org.omg.CORBA.UserException {

    /**
     * Constructs an <code>Bounds</code> with no specified detail message.
     */
    public Bounds() {
        super();
    }

    /**
     * Constructs an <code>Bounds</code> with the specified detail message.
     *
     * @param reason the detail message.
     */
    public Bounds(String reason) {
        super(reason);
    }
}
