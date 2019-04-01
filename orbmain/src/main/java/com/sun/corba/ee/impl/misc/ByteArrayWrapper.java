/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.misc;

import java.util.Arrays;

public class ByteArrayWrapper {

    private byte[] objKey;

    public ByteArrayWrapper(byte[] objKey) {
        this.objKey = objKey;
    }

    public byte[] getObjKey() {
        return objKey;
    }

    public boolean equals(Object obj) {
        if (obj == null)
            return false;

        if (obj instanceof ByteArrayWrapper) {
            return Arrays.equals(objKey, ((ByteArrayWrapper) obj).getObjKey());
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Arrays.hashCode(objKey);

    }

}
