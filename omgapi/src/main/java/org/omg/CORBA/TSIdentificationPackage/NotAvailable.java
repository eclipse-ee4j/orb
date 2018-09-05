/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.omg.CORBA.TSIdentificationPackage;
/**
 * A user-defined exception thrown if the ORB is unavailable
 * to register the call-back interfaces identified by the
 * OTS during initialization.
 * @see TSIdentification
 * @see AlreadyIdentified
 */
public final class NotAvailable
    extends org.omg.CORBA.UserException {

    //  constructor
/**
 * Constructs a <code>NotAvailable</code> exception.
 */
    public NotAvailable() {
        super();
    }
}

