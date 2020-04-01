/*
 * Copyright (c) 2012, 2020, Oracle and/or its affiliates.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.encoding;

import java.io.Serializable;

import com.sun.corba.ee.impl.util.RepositoryId;

class Value1 implements Serializable {
    static final String REPID = RepositoryId.createForJavaType(Value1.class);
    char aChar;
    int anInt;

    Value1(char aChar, int anInt) {
        this.aChar = aChar;
        this.anInt = anInt;
    }

    Value1() {
    }
}
