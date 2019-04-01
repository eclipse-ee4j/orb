/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.dynamicany;

import org.omg.CORBA.TypeCode;
import org.omg.CORBA.Any;

import com.sun.corba.ee.spi.orb.ORB;
import org.omg.DynamicAny.DynValue;

public class DynValueImpl extends DynValueCommonImpl implements DynValue {
    private static final long serialVersionUID = 4860224542389276556L;
    //
    // Constructors
    //

    private DynValueImpl() {
        this(null, (Any) null, false);
    }

    protected DynValueImpl(ORB orb, Any any, boolean copyValue) {
        super(orb, any, copyValue);
    }

    protected DynValueImpl(ORB orb, TypeCode typeCode) {
        super(orb, typeCode);
    }
}
