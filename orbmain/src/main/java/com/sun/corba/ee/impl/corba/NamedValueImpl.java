/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 1998-1999 IBM Corp. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.corba;

import org.omg.CORBA.NamedValue;
import org.omg.CORBA.Any;

import com.sun.corba.ee.spi.orb.ORB;

public class NamedValueImpl extends NamedValue {
    private String _name;
    private Any _value;
    private int _flags;
    private ORB _orb;

    public NamedValueImpl(ORB orb) {
        // Note: This orb could be an instanceof ORBSingleton or ORB
        _orb = orb;
        _value = new AnyImpl(_orb);
    }

    public NamedValueImpl(ORB orb, String name, Any value, int flags) {
        // Note: This orb could be an instanceof ORBSingleton or ORB
        _orb = orb;
        _name = name;
        _value = value;
        _flags = flags;
    }

    public String name() {
        return _name;
    }

    public Any value() {
        return _value;
    }

    public int flags() {
        return _flags;
    }
}
