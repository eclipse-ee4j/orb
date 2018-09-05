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
* Contains the value used to indicate a policy value that is
* incorrect for a valid policy type in a call to the
* <code>create_policy</code> method defined in the ORB class.
*
* @version 1.15 07/27/07
*/
public interface BAD_POLICY_VALUE {
    /** 
    * The value used to represent a bad policy value error 
    * in a <code>PolicyError</code> exception.
    * @see org.omg.CORBA.PolicyError
    */
    final short value = (short) (3L);
};
