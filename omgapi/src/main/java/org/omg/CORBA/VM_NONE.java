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
 * Defines the code used to represent the one of the values of a value type 
 * in a typecode.
 *
 * @author OMG
 * @see org.omg.CORBA.TypeCode
 * @version 1.12 07/27/07
 * @since   JDK1.2
 */

 
public interface VM_NONE {
    /**
     * The value representing the value type in a typecode as per 
     * CORBA 2.3 spec.
     */
    final short value = (short) (0L);
}
