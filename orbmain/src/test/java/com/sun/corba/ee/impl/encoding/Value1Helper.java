/*
 * Copyright (c) 2012, 2020 Oracle and/or its affiliates. All rights reserved.
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
