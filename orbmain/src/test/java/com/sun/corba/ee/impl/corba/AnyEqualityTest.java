/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation
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

package com.sun.corba.ee.impl.corba;

import com.meterware.simplestub.Memento;
import com.meterware.simplestub.SystemPropertySupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;
import org.omg.CORBA.TypeCode;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class AnyEqualityTest {
    List<Memento> mementos = new ArrayList<>();

    private Any any;
    private Any any1;
    private Any any2;

    @Before
    public void setUp() throws Exception {
        mementos.add(SystemPropertySupport.install("org.omg.CORBA.ORBClass", "com.sun.corba.ee.impl.orb.ORBImpl"));
        mementos.add(SystemPropertySupport.install("org.glassfish.gmbal.no.multipleUpperBoundsException", "true"));
        ORB orb = ORB.init((String[]) null, null);
        any = orb.create_any();
        any1 = orb.create_any();
        any2 = orb.create_any();
     }

    @After
    public void tearDown() throws Exception {
        for (Memento memento : mementos) {
            memento.revert();
        }
    }

    @Test
    public void nullAnys_areEqual() throws Exception {
        assertTrue(any1.equal(any2));
    }

    @Test
    public void twoAnysContainingTheSameShort_areEqual() throws Exception {
        short shortData = Short.MAX_VALUE;
        any1.insert_short(shortData);
        any2.insert_short(shortData);

        assertTrue(any1.equal(any2));
    }

    @Test
    public void twoAnysContainingTheSameUnsignedShort_areEqual() throws Exception {
        short uShortData = -1;
        any1.insert_ushort(uShortData);
        any2.insert_ushort(uShortData);

        assertTrue(any1.equal(any2));
    }

    @Test
    public void twoAnysContainingTheSameLong_areEqual() throws Exception {
        int longData = Integer.MAX_VALUE;
        any1.insert_long(longData);
        any2.insert_long(longData);

        assertTrue(any1.equal(any2));
    }

    @Test
    public void twoAnysContainingTheSameUnsignedLong_areEqual() throws Exception {
        int ulongData = -1;
        any1.insert_ulong(ulongData);
        any2.insert_ulong(ulongData);

        assertTrue(any1.equal(any2));
    }

    @Test
    public void twoAnysContainingTheSameLongLong_areEqual() throws Exception {
        long longlongData = Long.MAX_VALUE;
        any1.insert_longlong(longlongData);
        any2.insert_longlong(longlongData);

        assertTrue(any1.equal(any2));
    }

    @Test
    public void twoAnysContainingTheSameUnsignedLongLong_areEqual() throws Exception {
        long ulonglongData = -1L;
        any1.insert_ulonglong(ulonglongData);
        any2.insert_ulonglong(ulonglongData);

        assertTrue(any1.equal(any2));
    }

    @Test
    public void twoAnysContainingTheSameFloat_areEqual() throws Exception {
        float floatData = Float.MAX_VALUE;
        any1.insert_float(floatData);
        any2.insert_float(floatData);

        assertTrue(any1.equal(any2));
    }

    @Test
    public void twoAnysContainingTheSameDouble_areEqual() throws Exception {
        double doubleData = Double.MAX_VALUE;
        any1.insert_double(doubleData);
        any2.insert_double(doubleData);

        assertTrue(any1.equal(any2));
    }

    @Test
    public void twoAnysContainingTheSameChar_areEqual() throws Exception {
        char charData = Character.MAX_VALUE;
        any1.insert_char(charData);
        any2.insert_char(charData);

        assertTrue(any1.equal(any2));
    }

    @Test
    public void twoAnysContainingTheSameOctet_areEqual() throws Exception {
        byte octetData = Byte.MAX_VALUE;
        any1.insert_octet(octetData);
        any2.insert_octet(octetData);

        assertTrue(any1.equal(any2));
    }

    @Test
    public void twoAnysContainingTheSameAny_areEqual() throws Exception {
        byte octetData = Byte.MAX_VALUE;
        any.insert_octet(octetData);

        any1.insert_any(any);
        any2.insert_any(any);

        assertTrue(any1.equal(any2));
    }

    @Test
    public void twoAnysContainingTheSameTypecode_areEqual() throws Exception {
        byte octetData = Byte.MAX_VALUE;
        any.insert_octet(octetData);
        TypeCode typeCodeData = any.type();

        any1.insert_TypeCode(typeCodeData);
        any2.insert_TypeCode(typeCodeData);

        assertTrue(any1.equal(any2));
    }

    @Test
    public void twoAnysContainingTheSameString_areEqual() throws Exception {
        String stringData = "stringData";
        any1.insert_string(stringData);
        any2.insert_string(stringData);

        assertTrue(any1.equal(any2));
    }

    @Test
    public void twoAnysContainingTheSameEnum_areEqual() throws Exception {
        Enum1 enumData = Enum1.zeroth;
        Enum1Helper.insert(any1, enumData);
        Enum1Helper.insert(any2, enumData);

        assertTrue(any1.equal(any2));
    }
}
