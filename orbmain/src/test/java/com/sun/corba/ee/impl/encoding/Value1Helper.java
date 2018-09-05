/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.encoding;

import com.sun.org.omg.CORBA.portable.ValueHelper;
import org.omg.CORBA.TypeCode;
import org.omg.CORBA.TypeCodePackage.BadKind;
import org.omg.CORBA.portable.OutputStream;

import java.io.Serializable;

import static com.meterware.simplestub.Stub.createStrictStub;

abstract class Value1Helper implements ValueHelper {
    Value1Type typeCode = createStrictStub(Value1Type.class);

    void setModifier(short modifier) {
        typeCode.modifier = modifier;
    }

    @Override
    public TypeCode get_type() {
        return typeCode;
    }

    @Override
    public void write_value(OutputStream os, Serializable value) {
        Value1 value1 = (Value1) value;
        os.write_wchar(value1.aChar);
        os.write_long(value1.anInt);
    }

    @Override
    public String get_id() {
        return Value1.REPID;
    }
}


abstract class Value1Type extends TypeCode {
    short modifier;

    @Override
    public short type_modifier() throws BadKind {
        return modifier;
    }
}
