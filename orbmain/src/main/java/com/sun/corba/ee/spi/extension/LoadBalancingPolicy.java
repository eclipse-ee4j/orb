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
public class LoadBalancingPolicy extends LocalObject implements Policy {
    private static final ORBUtilSystemException wrapper = ORBUtilSystemException.self;

    private final int value;

    public LoadBalancingPolicy(int value) {
        if (value < ORBConstants.FIRST_LOAD_BALANCING_VALUE || value > ORBConstants.LAST_LOAD_BALANCING_VALUE) {
            throw wrapper.invalidLoadBalancingPolicyValue(value, ORBConstants.FIRST_LOAD_BALANCING_VALUE, ORBConstants.LAST_LOAD_BALANCING_VALUE);
        }
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public int policy_type() {
        return ORBConstants.LOAD_BALANCING_POLICY;
    }

    public org.omg.CORBA.Policy copy() {
        return this;
    }

    public void destroy() {
        // NO-OP
    }

    @Override
    public String toString() {
        return "LoadBalancingPolicy[" + value + "]";
    }
}
