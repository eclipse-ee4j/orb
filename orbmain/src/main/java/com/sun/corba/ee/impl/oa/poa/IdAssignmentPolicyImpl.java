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

final class IdAssignmentPolicyImpl extends org.omg.CORBA.LocalObject implements org.omg.PortableServer.IdAssignmentPolicy {

    public IdAssignmentPolicyImpl(IdAssignmentPolicyValue value) {
        this.value = value;
    }

    public IdAssignmentPolicyValue value() {
        return value;
    }

    public int policy_type() {
        return ID_ASSIGNMENT_POLICY_ID.value;
    }

    public Policy copy() {
        return new IdAssignmentPolicyImpl(value);
    }

    public void destroy() {
        value = null;
    }

    private IdAssignmentPolicyValue value;

    public String toString() {
        return "IdAssignmentPolicy[" + ((value.value() == IdAssignmentPolicyValue._USER_ID) ? "USER_ID" : "SYSTEM_ID" + "]");
    }
}
