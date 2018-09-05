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

import java.io.Serializable;

class Value1 implements Serializable {
    static final String REPID = "RMI:com.sun.corba.ee.impl.encoding.Value1:3E1F37A79F0D0984:F72C4A0542764A7B";
    char aChar;
    int anInt;

    Value1(char aChar, int anInt) {
        this.aChar = aChar;
        this.anInt = anInt;
    }

    Value1() {
    }
}
