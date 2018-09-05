/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.ior;

import com.sun.corba.ee.impl.encoding.EncodingTestBase;
import com.sun.corba.ee.spi.ior.IOR;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class IORImplTest extends EncodingTestBase {

    @Test
    public void stringifyIncludesTypeId() {
        IOR ior = new IORImpl(getOrb(), "TestType");
        assertEquals("IOR:" + "00000000" + "00000009" + "54657374" + "54797065" +"0000000000000000", ior.stringify());
    }
}
