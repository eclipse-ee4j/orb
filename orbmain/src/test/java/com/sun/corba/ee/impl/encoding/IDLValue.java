/*
 * Copyright (c) 2018, 2020 Oracle and/or its affiliates.
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
