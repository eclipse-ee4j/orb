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

final class ImplicitActivationPolicyImpl extends org.omg.CORBA.LocalObject implements ImplicitActivationPolicy {

    public ImplicitActivationPolicyImpl(ImplicitActivationPolicyValue value) {
        this.value = value;
    }

    public ImplicitActivationPolicyValue value() {
        return value;
    }

    public int policy_type() {
        return IMPLICIT_ACTIVATION_POLICY_ID.value;
    }

    public Policy copy() {
        return new ImplicitActivationPolicyImpl(value);
    }

    public void destroy() {
        value = null;
    }

    private ImplicitActivationPolicyValue value;

    public String toString() {
        return "ImplicitActivationPolicy["
                + ((value.value() == ImplicitActivationPolicyValue._IMPLICIT_ACTIVATION) ? "IMPLICIT_ACTIVATION" : "NO_IMPLICIT_ACTIVATION" + "]");
    }
}
