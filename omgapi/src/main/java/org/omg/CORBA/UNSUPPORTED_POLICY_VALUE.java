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
 * A <tt>PolicyErrorCode</tt> which would be filled if the value requested for the <tt>Policy</tt> is of a valid type
 * and within the valid range for that type, but this valid value is not currently supported.
 *
 * @author rip-dev
 * @version 1.14 07/27/07
 */
public interface UNSUPPORTED_POLICY_VALUE {
    /**
     * The Error code for PolicyError exception.
     */
    final short value = (short) (4L);
};
