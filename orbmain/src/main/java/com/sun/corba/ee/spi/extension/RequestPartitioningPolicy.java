/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
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
