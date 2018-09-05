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
 * This exception is thrown if OTS call-back interfaces
 * have already been registered with the ORB.
 * @see TSIdentification
 * @see NotAvailable
 */
public final class AlreadyIdentified
    extends org.omg.CORBA.UserException {
    //  constructor
    public AlreadyIdentified() {
        super();
    }
}

