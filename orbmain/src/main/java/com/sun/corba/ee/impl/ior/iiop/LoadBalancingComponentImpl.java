/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.ior.iiop;

import org.omg.CORBA_2_3.portable.OutputStream;

import com.sun.corba.ee.spi.ior.TaggedComponentBase;
import com.sun.corba.ee.spi.ior.iiop.LoadBalancingComponent;

import com.sun.corba.ee.spi.logging.ORBUtilSystemException;
import com.sun.corba.ee.spi.misc.ORBConstants;

public class LoadBalancingComponentImpl extends TaggedComponentBase implements LoadBalancingComponent {

    private static ORBUtilSystemException wrapper = ORBUtilSystemException.self;

    private int loadBalancingValue;

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof LoadBalancingComponentImpl)) {
            return false;
        }

        LoadBalancingComponentImpl other = (LoadBalancingComponentImpl) obj;

        return loadBalancingValue == other.loadBalancingValue;
    }

    @Override
    public int hashCode() {
        return loadBalancingValue;
    }

    @Override
    public String toString() {
        return "LoadBalancingComponentImpl[loadBalancingValue=" + loadBalancingValue + "]";
    }

    public LoadBalancingComponentImpl() {
        loadBalancingValue = 0;
    }

    public LoadBalancingComponentImpl(int theLoadBalancingValue) {
        if (theLoadBalancingValue < ORBConstants.FIRST_LOAD_BALANCING_VALUE || theLoadBalancingValue > ORBConstants.LAST_LOAD_BALANCING_VALUE) {
            throw wrapper.invalidLoadBalancingComponentValue(theLoadBalancingValue, ORBConstants.FIRST_LOAD_BALANCING_VALUE,
                    ORBConstants.LAST_LOAD_BALANCING_VALUE);
        }
        loadBalancingValue = theLoadBalancingValue;
    }

    public int getLoadBalancingValue() {
        return loadBalancingValue;
    }

    public void writeContents(OutputStream os) {
        os.write_ulong(loadBalancingValue);
    }

    public int getId() {
        return ORBConstants.TAG_LOAD_BALANCING_ID;
    }
}
