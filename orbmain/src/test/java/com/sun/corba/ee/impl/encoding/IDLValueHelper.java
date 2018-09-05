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

import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;

import java.io.Serializable;

public class IDLValueHelper {
    public static Serializable read(InputStream is) {
        byte b = is.read_octet();
        return new IDLValue(b);
    }

    public static void write(OutputStream os, IDLValue value) {
        os.write_octet(value.aByte);
    }

}
