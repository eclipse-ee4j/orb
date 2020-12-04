/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Eclipse Distribution License
 * v. 1.0 which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License v. 2.0 are satisfied: GNU General Public License v2.0
 * w/Classpath exception which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR BSD-3-Clause OR GPL-2.0 WITH
 * Classpath-exception-2.0
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
