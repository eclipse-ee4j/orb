/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.oa.poa;

import org.omg.CORBA.Policy;
import org.omg.PortableServer.SERVANT_RETENTION_POLICY_ID;
import org.omg.PortableServer.ServantRetentionPolicy;
import org.omg.PortableServer.ServantRetentionPolicyValue;

final class ServantRetentionPolicyImpl extends org.omg.CORBA.LocalObject implements ServantRetentionPolicy {

    private static final long serialVersionUID = 469062222833983100L;

    public ServantRetentionPolicyImpl(ServantRetentionPolicyValue value) {
        this.value = value;
    }

    public ServantRetentionPolicyValue value() {
        return value;
    }

    public int policy_type() {
        return SERVANT_RETENTION_POLICY_ID.value;
    }

    public Policy copy() {
        return new ServantRetentionPolicyImpl(value);
    }

    public void destroy() {
        value = null;
    }

    private ServantRetentionPolicyValue value;

    @Override
    public String toString() {
        return "ServantRetentionPolicy[" + ((value.value() == ServantRetentionPolicyValue._RETAIN) ? "RETAIN" : "NON_RETAIN" + "]");
    }
}
