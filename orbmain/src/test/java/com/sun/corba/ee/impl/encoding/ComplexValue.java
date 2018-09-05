/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.encoding;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ComplexValue implements Serializable {
    static final String REPID = "RMI:com.sun.corba.ee.impl.encoding.ComplexValue:526A075F52D4A68C:31E83A657AE82D48";
    int anInt;
    List<Value1> arrayList = new ArrayList<Value1>(1);
    Value1 value;

    ComplexValue(char aChar, int anInt) {
        this.anInt = anInt;
        arrayList.add(new Value1(aChar, anInt));
        char nextChar = Character.toUpperCase(aChar);
        value = new Value1(nextChar, anInt+1);
    }

    ComplexValue() {
    }
}
