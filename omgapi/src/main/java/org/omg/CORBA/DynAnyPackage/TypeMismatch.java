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
 * TypeMismatch is thrown by dynamic any accessor methods when type of the actual contents do not match what is trying
 * to be accessed.
 */
// @SuppressWarnings({"serial"})
public final class TypeMismatch extends org.omg.CORBA.UserException {

    /**
     * Constructs a <code>TypeMismatch</code> object.
     */
    public TypeMismatch() {
        super();
    }

    /**
     * Constructs a <code>TypeMismatch</code> object.
     *
     * @param reason a <code>String</code> giving more information regarding the exception.
     */
    public TypeMismatch(String reason) {
        super(reason);
    }
}
