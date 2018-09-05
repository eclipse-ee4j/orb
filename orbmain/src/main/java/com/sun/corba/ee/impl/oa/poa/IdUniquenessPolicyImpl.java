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

final class IdUniquenessPolicyImpl
    extends org.omg.CORBA.LocalObject implements IdUniquenessPolicy {

    public IdUniquenessPolicyImpl(IdUniquenessPolicyValue value) {
        this.value = value;
    }

    public IdUniquenessPolicyValue value() {
        return value;
    }
 
    public int policy_type()
    {
        return ID_UNIQUENESS_POLICY_ID.value ;
    }

    public Policy copy() {
        return new IdUniquenessPolicyImpl(value);
    }

    public void destroy() {
        value = null;
    }

    private IdUniquenessPolicyValue value;

    public String toString()
    {
        return "IdUniquenessPolicy[" +
            ((value.value() == IdUniquenessPolicyValue._UNIQUE_ID) ?
                "UNIQUE_ID" : "MULTIPLE_ID" + "]") ;
    }
}
