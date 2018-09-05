/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.corba;

public final class Enum1 implements org.omg.CORBA.portable.IDLEntity {
    public static final int _zeroth = 0,
        _first = 1,
        _second = 2,
        _third = 3;
    public static final Enum1 zeroth = new Enum1(_zeroth);
    public static final Enum1 first = new Enum1(_first);
    public static final Enum1 second = new Enum1(_second);
    public static final Enum1 third = new Enum1(_third);
    public int value() {
        return _value;
    }
    public static final Enum1 from_int(int i)  throws  org.omg.CORBA.BAD_PARAM {
        switch (i) {
        case _zeroth:
            return zeroth;
        case _first:
            return first;
        case _second:
            return second;
        case _third:
            return third;
        default:
            throw new org.omg.CORBA.BAD_PARAM();
        }
    }
    private Enum1(int _value){
        this._value = _value;
    }
    private int _value;
}
