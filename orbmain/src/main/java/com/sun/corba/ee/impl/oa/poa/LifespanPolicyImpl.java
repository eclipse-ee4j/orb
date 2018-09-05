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

import org.omg.CORBA.*;
import org.omg.PortableServer.*;

final class LifespanPolicyImpl
    extends org.omg.CORBA.LocalObject implements LifespanPolicy {

    public LifespanPolicyImpl(LifespanPolicyValue value) {
        this.value = value;
    }

    public LifespanPolicyValue value() {
        return value;
    }

    public int policy_type()
    {
        return LIFESPAN_POLICY_ID.value ;
    }

    public Policy copy() {
        return new LifespanPolicyImpl(value);
    }

    public void destroy() {
        value = null;
    }

    private LifespanPolicyValue value;

    public String toString()
    {
        return "LifespanPolicy[" +
            ((value.value() == LifespanPolicyValue._TRANSIENT) ?
                "TRANSIENT" : "PERSISTENT" + "]") ;
    }
}
