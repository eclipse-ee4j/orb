/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.encoding;

import org.omg.CORBA.portable.IDLEntity;

public class IDLValue implements IDLEntity {
    static final String REPID = "RMI:com.sun.corba.ee.impl.encoding.IDLValue:BB212B05444A560F:000000000ABCDEF0";
    static final long serialVersionUID = 0xABCDEF0;

    byte aByte;
    int anInt;

    public IDLValue() {
    }

    public IDLValue(byte aByte) {
        this.aByte = aByte;
        this.anInt = 0x10 * aByte;
    }
}
