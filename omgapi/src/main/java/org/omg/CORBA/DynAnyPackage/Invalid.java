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
 * Invalid is thrown by dynamic any operations when a bad <code>DynAny</code> or <code>Any</code> is passed as a
 * parameter.
 */
// @SuppressWarnings({"serial"})
public final class Invalid extends org.omg.CORBA.UserException {

    /**
     * Constructs an <code>Invalid</code> object.
     */
    public Invalid() {
        super();
    }

    /**
     * Constructs an <code>Invalid</code> object.
     *
     * @param reason a <code>String</code> giving more information regarding the bad parameter passed to a dynamic any
     * operation.
     */
    public Invalid(String reason) {
        super(reason);
    }
}
