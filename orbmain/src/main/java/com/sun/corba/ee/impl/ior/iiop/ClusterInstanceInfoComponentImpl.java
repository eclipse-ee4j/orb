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

import com.sun.corba.ee.spi.folb.ClusterInstanceInfo;

import com.sun.corba.ee.spi.ior.TaggedComponentBase;
import com.sun.corba.ee.spi.ior.iiop.ClusterInstanceInfoComponent;

import com.sun.corba.ee.spi.misc.ORBConstants;

public class ClusterInstanceInfoComponentImpl extends TaggedComponentBase implements ClusterInstanceInfoComponent {

    private final ClusterInstanceInfo clusterInstanceInfoValue;

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ClusterInstanceInfoComponentImpl)) {
            return false;
        }

        ClusterInstanceInfoComponentImpl other = (ClusterInstanceInfoComponentImpl) obj;

        return clusterInstanceInfoValue.equals(other.clusterInstanceInfoValue);
    }

    @Override
    public int hashCode() {
        return clusterInstanceInfoValue.hashCode();
    }

    @Override
    public String toString() {
        return "ClusterInstanceInfoComponentImpl[clusterInstanceInfoValue=" + clusterInstanceInfoValue + "]";
    }

    public ClusterInstanceInfoComponentImpl(ClusterInstanceInfo theClusterInstanceInfoValue) {
        clusterInstanceInfoValue = theClusterInstanceInfoValue;
    }

    public ClusterInstanceInfo getClusterInstanceInfo() {
        return clusterInstanceInfoValue;
    }

    public void writeContents(OutputStream os) {
        clusterInstanceInfoValue.write(os);
    }

    public int getId() {
        return ORBConstants.FOLB_MEMBER_ADDRESSES_TAGGED_COMPONENT_ID;
    }
}
