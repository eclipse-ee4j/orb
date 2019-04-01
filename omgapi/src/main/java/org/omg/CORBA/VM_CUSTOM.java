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
 * Defines the code used to represent a custom marshalled value type in a typecode. This is one of the possible results
 * of the <code>type_modifier</code> method on the <code>TypeCode</code> interface.
 *
 * @see org.omg.CORBA.TypeCode
 * @version 1.12 07/27/07
 */
public interface VM_CUSTOM {
    /**
     * The value representing a custom marshalled value type in a typecode.
     */
    final short value = (short) (1L);
}
