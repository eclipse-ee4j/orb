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
 * The vendor minor code ID reserved for OMG. Minor codes for the standard exceptions are prefaced by the VMCID assigned
 * to OMG, defined as the constant OMGVMCID, which, like all VMCIDs, occupies the high order 20 bits.
 */

public interface OMGVMCID {

    /**
     * The vendor minor code ID reserved for OMG. This value is or'd with the high order 20 bits of the minor code to
     * produce the minor value in a system exception.
     */
    static final int value = 0x4f4d0000;
}
