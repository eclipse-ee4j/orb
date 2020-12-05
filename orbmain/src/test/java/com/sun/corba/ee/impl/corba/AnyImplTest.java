/*
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

import com.sun.corba.ee.spi.orb.ORB;
import org.junit.Before;
import org.junit.Test;
import org.omg.CORBA.Any;
import org.omg.CORBA.BAD_OPERATION;
import org.omg.CORBA.TCKind;

import java.io.Serializable;

import static com.meterware.simplestub.Stub.createStrictStub;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

public class AnyImplTest {

    private ORBFake orb = createStrictStub(ORBFake.class);
    private Any any;

    @Before
    public void setUp() throws Exception {
        any = new AnyImpl(orb);
    }

    @Test
    public void whenAnyCreated_typeIsNull() {
        assertEquals(TCKind.tk_null, any.type().kind());
    }

    @Test(expected = BAD_OPERATION.class)
    public void whenReadingUninitializedAny_throwException() {
        any.extract_octet();
    }

    @Test(expected = BAD_OPERATION.class)
    public void whenTryingToReadWrongType_throwException() {
        any.insert_octet((byte) 3);
        assertThat(any.extract_double(), equalTo(3.0));
    }

    @Test
    public void whenOctetInserted_canReadBackValue() {
        any.insert_octet((byte) 3);
        assertEquals(TCKind.tk_octet, any.type().kind());
        assertEquals(3, any.extract_octet());
    }

    @Test
    public void whenShortInserted_canReadBackValue() {
        any.insert_short((short) -15);
        assertEquals(TCKind.tk_short, any.type().kind());
        assertEquals(-15, any.extract_short());
    }

    @Test
    public void whenUnsignedShortInserted_canReadBackValue() {
        any.insert_ushort((short) 127);
        assertEquals(TCKind.tk_ushort, any.type().kind());
        assertEquals(127, any.extract_ushort());
    }

    @Test
    public void whenLongInserted_canReadBackValue() {
        any.insert_long(17);
        assertEquals(TCKind.tk_long, any.type().kind());
        assertEquals(17, any.extract_long());
    }

    @Test
    public void whenUnsignedLongInserted_canReadBackValue() {
        any.insert_ulong(170);
        assertEquals(TCKind.tk_ulong, any.type().kind());
        assertEquals(170, any.extract_ulong());
    }

    @Test
    public void whenLongLongInserted_canReadBackValue() {
        any.insert_longlong(Integer.MAX_VALUE);
        assertEquals(TCKind.tk_longlong, any.type().kind());
        assertEquals(Integer.MAX_VALUE, any.extract_longlong());
    }

    @Test
    public void whenUnsignedLongLongInserted_canReadBackValue() {
        any.insert_ulonglong(Integer.MAX_VALUE);
        assertEquals(TCKind.tk_ulonglong, any.type().kind());
        assertEquals(Integer.MAX_VALUE, any.extract_ulonglong());
    }

    @Test
    public void whenBooleanTrueInserted_canReadBackValue() {
        any.insert_boolean(true);
        assertEquals(TCKind.tk_boolean, any.type().kind());
        assertTrue(any.extract_boolean());
    }

    @Test
    public void whenBooleanFalseInserted_canReadBackValue() {
        any.insert_boolean(false);
        assertFalse(any.extract_boolean());
    }

    @Test
    public void whenFloatInserted_canReadBackValue() {
        any.insert_float((float) 21.3);
        assertEquals(TCKind.tk_float, any.type().kind());
        assertEquals(21.3, any.extract_float(), 0.01);
    }

    @Test
    public void whenDoubleInserted_canReadBackValue() {
        any.insert_double(-12.56);
        assertEquals(TCKind.tk_double, any.type().kind());
        assertEquals(-12.56, any.extract_double(), 0.01);
    }

    @Test
    public void whenCharInserted_canReadBackValue() {
        any.insert_char('x');
        assertEquals(TCKind.tk_char, any.type().kind());
        assertEquals('x', any.extract_char());
    }

    @Test
    public void whenWideCharInserted_canReadBackValue() {
        any.insert_wchar('\u0123');
        assertEquals(TCKind.tk_wchar, any.type().kind());
        assertEquals('\u0123', any.extract_wchar());
    }

    @Test
    public void whenStringInserted_canReadBackValue() {
        any.insert_string("This is a test");
        assertEquals(TCKind.tk_string, any.type().kind());
        assertEquals("This is a test", any.extract_string());
    }

    @Test
    public void whenStringInsertedAsValue_canReadBackValue() throws Exception {
        any.insert_Value("This is another test");

        assertThat(any.type().kind(), is(TCKind.tk_value_box));
        assertThat(any.extract_Value(), equalTo((Serializable) "This is another test"));
    }

    abstract static class ORBFake extends ORB {
        protected ORBFake() {
            initializePrimitiveTypeCodeConstants();
        }
    }
}
