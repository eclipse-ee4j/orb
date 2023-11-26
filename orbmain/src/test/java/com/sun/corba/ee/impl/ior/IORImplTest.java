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

package com.sun.corba.ee.impl.ior;

import com.sun.corba.ee.impl.encoding.EncodingTestBase;
import com.sun.corba.ee.spi.ior.IOR;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class IORImplTest extends EncodingTestBase {

    @Test
    public void stringifyIncludesTypeId() {
        IOR ior = new IORImpl(getOrb(), "TestType");
        assertEquals("IOR:" + "00000000" + "00000009" + "54657374" + "54797065" + "0000000000000000", ior.stringify());
    }
}
