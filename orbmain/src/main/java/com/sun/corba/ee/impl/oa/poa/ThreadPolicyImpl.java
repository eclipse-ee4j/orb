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

final class ThreadPolicyImpl extends org.omg.CORBA.LocalObject implements ThreadPolicy {

    public ThreadPolicyImpl(ThreadPolicyValue value) {
        this.value = value;
    }

    public ThreadPolicyValue value() {
        return value;
    }

    public int policy_type() {
        return THREAD_POLICY_ID.value;
    }

    public Policy copy() {
        return new ThreadPolicyImpl(value);
    }

    public void destroy() {
        value = null;
    }

    private ThreadPolicyValue value;

    public String toString() {
        return "ThreadPolicy[" + ((value.value() == ThreadPolicyValue._SINGLE_THREAD_MODEL) ? "SINGLE_THREAD_MODEL" : "ORB_CTRL_MODEL" + "]");
    }
}
