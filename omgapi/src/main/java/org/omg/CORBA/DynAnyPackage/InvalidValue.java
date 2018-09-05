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
 * @author unattributed
 * @version 1.18 07/27/07
 *
 * Dynamic Any insert operations raise the <code>InvalidValue</code>
 * exception if the value inserted is not consistent with the type
 * of the accessed component in the <code>DynAny</code> object.
 */
// @SuppressWarnings({"serial"})
public final class InvalidValue
    extends org.omg.CORBA.UserException {

    /**
     * Constructs an <code>InvalidValue</code> object.
     */
    public InvalidValue() {
        super();
    }

    /**
     * Constructs an <code>InvalidValue</code> object.
     * @param reason  a <code>String</code> giving more information
     * regarding the exception.
     */
    public InvalidValue(String reason) {
        super(reason);
    }
}
