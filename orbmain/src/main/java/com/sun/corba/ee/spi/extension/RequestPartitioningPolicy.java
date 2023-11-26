/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Eclipse Distribution License
 * v. 1.0 which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License v. 2.0 are satisfied: GNU General Public License v2.0
 * w/Classpath exception which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR BSD-3-Clause OR GPL-2.0 WITH
 * Classpath-exception-2.0
 */

package com.sun.corba.ee.spi.extension;

import org.omg.CORBA.Policy;
import org.omg.CORBA.LocalObject;

import com.sun.corba.ee.spi.logging.ORBUtilSystemException;
import com.sun.corba.ee.spi.misc.ORBConstants;

/**
 * Policy used to support the request partitioning feature and to specify the partition to use.
 */
public class RequestPartitioningPolicy extends LocalObject implements Policy {
    private static ORBUtilSystemException wrapper = ORBUtilSystemException.self;

    public final static int DEFAULT_VALUE = 0;
    private final int value;

    public RequestPartitioningPolicy(int value) {
        if (value < ORBConstants.REQUEST_PARTITIONING_MIN_THREAD_POOL_ID || value > ORBConstants.REQUEST_PARTITIONING_MAX_THREAD_POOL_ID) {
            throw wrapper.invalidRequestPartitioningPolicyValue(value, ORBConstants.REQUEST_PARTITIONING_MIN_THREAD_POOL_ID,
                    ORBConstants.REQUEST_PARTITIONING_MAX_THREAD_POOL_ID);
        }
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public int policy_type() {
        return ORBConstants.REQUEST_PARTITIONING_POLICY;
    }

    public org.omg.CORBA.Policy copy() {
        return this;
    }

    public void destroy() {
        // NO-OP
    }

    @Override
    public String toString() {
        return "RequestPartitioningPolicy[" + value + "]";
    }
}
