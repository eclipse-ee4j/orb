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
import com.sun.corba.ee.spi.misc.ORBConstants;

/**
 * Policy used to implement zero IIOP port policy in the POA.
 */
public class ZeroPortPolicy extends LocalObject implements Policy {
    private static ZeroPortPolicy policy = new ZeroPortPolicy(true);

    private boolean flag = true;

    private ZeroPortPolicy(boolean type) {
        this.flag = type;
    }

    public String toString() {
        return "ZeroPortPolicy[" + flag + "]";
    }

    public boolean forceZeroPort() {
        return flag;
    }

    public synchronized static ZeroPortPolicy getPolicy() {
        return policy;
    }

    public int policy_type() {
        return ORBConstants.ZERO_PORT_POLICY;
    }

    public org.omg.CORBA.Policy copy() {
        return this;
    }

    public void destroy() {
        // NO-OP
    }
}
