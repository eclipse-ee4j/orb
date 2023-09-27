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

package com.sun.corba.ee.impl.encoding;

import com.sun.corba.ee.impl.protocol.RequestCanceledException;
import com.sun.corba.ee.spi.orb.ORBVersionFactory;
import org.junit.Test;
import org.omg.CORBA.*;
import org.omg.CORBA.TypeCodePackage.BadKind;

import java.io.IOException;
import java.nio.ByteOrder;
import java.util.Arrays;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class CDRInputTest extends EncodingTestBase {

    @Test
    public void reportMarkNotSupported() {
        assertFalse(new CDRInputStream_1_0().markSupported());
        assertFalse(new CDRInputStream_1_1().markSupported());
        assertFalse(new CDRInputStream_1_2().markSupported());
    }

    @Test(expected = NO_IMPLEMENT.class)
    public void reportReadNotImplented() throws IOException {
        new CDRInputStream_1_0().read();
    }

    @Test(expected = NO_IMPLEMENT.class)
    public void reportReadContextImplented() throws IOException {
        setMessageBody();
        getInputObject().read_Context();
    }

    @Test
    public void whenCDRInputObjectCreated_canReadBoolean() throws IOException {
        setMessageBody(0, 1);
        assertFalse(getInputObject().read_boolean());
        assertTrue(getInputObject().read_boolean());
    }

    @Test
    public void whenCDRInputObjectCreated_canReadLatin1Char() throws IOException {
        setCharEncoding(ISO_8859_1);
        setMessageBody('x');
        assertEquals('x', getInputObject().read_char());
    }

    @Test
    public void whenCDRInputObjectCreated_canReadUTF8Char() throws IOException {
        setCharEncoding(UTF_8);
        setMessageBody('{');
        assertEquals('{', getInputObject().read_char());
    }

    @Test(expected = MARSHAL.class)
    public void whenCDRInputObjectCreated_cannotReadUTF16CharIn_1_0() throws IOException {
        useV1_0();
        setWCharEncoding(UTF_16);
        setMessageBody(0x04, FE, FF, 0x34, 0x56);
        assertEquals('\u3456', getInputObject().read_wchar());
    }

    @Test
    public void whenCDRInputObjectCreated_canReadUTF16CharIn_1_0WithLegacyORB() throws IOException {
        useV1_0();
        setOrbVersion(ORBVersionFactory.getOLD());
        setWCharEncoding(UTF_16);
        setMessageBody(0x34, 0x56);
        assertEquals('\u3456', getInputObject().read_wchar());
    }

    @Test
    public void whenCDRInputObjectCreated_canReadLittleEndianUTF16CharIn_1_0WithLegacyORB() throws IOException {
        useV1_0();
        useLittleEndian();
        setOrbVersion(ORBVersionFactory.getOLD());
        setWCharEncoding(UTF_16);
        setMessageBody(0x34, 0x56);
        assertEquals('\u5634', getInputObject().read_wchar());
    }

    @Test
    public void whenCDRInputObjectCreated_canReadUTF16CharIn_1_1() throws IOException {
        useV1_1();
        setWCharEncoding(UTF_16);
        setMessageBody(0x34, 0x56);
        assertEquals('\u3456', getInputObject().read_wchar());
    }

    @Test
    public void whenCDRInputObjectCreated_canReadUTF16CharIn_1_2() throws IOException {
        useV1_2();
        setWCharEncoding(UTF_16);
        setMessageBody(0x04, FE, FF, 0x34, 0x56);
        assertEquals('\u3456', getInputObject().read_wchar());
    }

    @Test
    public void whenCDRInputObjectCreated_canReadOctet() throws IOException {
        setMessageBody(25);
        CDRInputObject inputObject = getInputObject();
        assertEquals(25, inputObject.read_octet());
    }

    @Test
    public void can_read_integers() {
        setMessageBody(0x04, pad(), /* short */ FF, (byte) 0xf2, /* short */ 0x00, 0x03, pad(), pad(), /* long1 */ 0, 1, 2, (byte) 0x83,
                /* long2 */ FF, FF, (byte) 0xfd, 0x71, pad(), pad(), pad(), pad(), /* long long */ 0, 0, 1, 0, 0, (byte) 0x80, 1, 7,
                /* long long */ 0, 0, 1, 0, 0, 0, 1, 2);

        assertEquals("Octet value", 4, getInputObject().read_octet());
        assertEquals("Signed short value", -14, getInputObject().read_short());
        assertEquals("Standard unsigned short value", 3, getInputObject().read_ushort());
        assertEquals("Unsigned long value", 66179, getInputObject().read_ulong());
        assertEquals("Long value", -655, getInputObject().read_long());
        assertEquals("Long long value", 1099520016647L, getInputObject().read_longlong());
        assertEquals("Unsigned long long value", 1099511628034L, getInputObject().read_ulonglong());
    }

    @Test
    public void can_read_little_endian_integers() {
        setMessageBody(0x04, pad(), /* short */ 0xf2, FF, /* ushort */ 0x03, 0x00, /* for long */ pad(), pad(), /* long */ 0x83, 2, 1, 0,
                /* ulong */ 0x71, 0xfd, FF, FF, /* for long_long */ pad(), pad(), pad(), pad(), /* long long */ 7, 1, 0x80, 0, 0, 1, 0, 0);
        useLittleEndian();

        assertEquals("Octet value", 4, getInputObject().read_octet());
        assertEquals("Signed short value", -14, getInputObject().read_short());
        assertEquals("Standard unsigned short value", 3, getInputObject().read_ushort());
        assertEquals("Unsigned long value", 66179, getInputObject().read_ulong());
        assertEquals("Long value", -655, getInputObject().read_long());
        assertEquals("Long long value", 1099520016647L, getInputObject().read_longlong());
    }

    @Test
    public void can_read_floats() {
        setMessageBody(0x3f, 0x80, 0, 0, 0x3f, 0xd5, 0x55, 0x55, 0x55, 0x55, 0x55, 0x55);

        assertEquals("Float", 1, getInputObject().read_float(), 0.001);
        assertEquals("Double", 0.33333, getInputObject().read_double(), 0.001);
    }

    @Test
    public void can_read_float_arrays() {
        setMessageBody(0x3f, 0x80, 0, 0, 0x46, 0x40, 0xE4, 0x7E);
        float[] actual = new float[2];
        getInputObject().read_float_array(actual, 0, 2);

        assertEquals("Float 1", 1, actual[0], 0.001);
        assertEquals("Float 2", 12345.12346f, actual[1], 0.001);
    }

    @Test
    public void can_read_double_arrays() {
        setMessageBody(pad(), pad(), pad(), pad(), 0x3f, 0xd5, 0x55, 0x55, 0x55, 0x55, 0x55, 0x55, 0x40, 0, 0, 0, 0, 0, 0, 0);
        double[] actual = new double[2];
        getInputObject().read_double_array(actual, 0, 2);

        assertEquals("Double 1", 0.33333, actual[0], 0.001);
        assertEquals("Double 2", 2, actual[1], 0.001);
    }

    @Test
    public void whenUsingV1_0_canReadCharString() {
        useV1_0();
        setMessageBody(0, 0, 0, 11, 't', 'h', 'i', 's', ' ', 'w', 'o', 'r', 'k', 's', 0);

        assertEquals("String value", "this works", getInputObject().read_string());
    }

    @Test
    public void canReadNullStringFromOldOrb() {
        setOrbVersion(ORBVersionFactory.getOLD());
        setMessageBody(0, 0, 0, 0);
        assertEquals("", getInputObject().read_string());
    }

    @Test
    public void canReadStringFromOldOrb() {
        setOrbVersion(ORBVersionFactory.getOLD());
        setMessageBody(0, 0, 0, 9, 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 0);
        assertEquals("abcdefgh", getInputObject().read_string());
    }

    @Test
    public void canReadStringFromOldOrbAcrossFragment() {
        useV1_1();
        setOrbVersion(ORBVersionFactory.getOLD());
        setMessageBody(0, 0, 0, 9, 'a', 'b', 'c', 'd');
        addFragment('e', 'f', 'g', 'h', 0);
        assertEquals("abcdefgh", getInputObject().read_string());
    }

    @Test
    public void canReadStringFromOldOrbWithTerminatorInNextFragment() {
        useV1_1();
        setOrbVersion(ORBVersionFactory.getOLD());
        setMessageBody(0, 0, 0, 9, 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h');
        addFragment(0, 'x');
        assertEquals("abcdefgh", getInputObject().read_string());
        assertEquals('x', getInputObject().read_char());
    }

    @Test(expected = MARSHAL.class)
    public void whenUsingV1_0_cannotReadWCharString() {
        useV1_0();
        int[] data = { 0, 0, 0, 22, FE, FF, 0, 'T', 0, 'h', 0, 'i', 0, 's', 0, ',', 0, ' ', 0, 't', 0, 'o', 0, 'o', 0, '!' };
        setMessageBody(data);
        assertEquals("Wide string value", "This, too!", getInputObject().read_wstring());
    }

    @Test
    public void whenUsingV1_1_canReadCharAndWCharStrings() {
        useV1_1();
        int[] data = { 0, 0, 0, 11, 't', 'h', 'i', 's', ' ', 'w', 'o', 'r', 'k', 's', 0, pad(), 0, 0, 0, 11, 0, 'T', 0, 'h', 0, 'i', 0, 's',
                0, ',', 0, ' ', 0, 't', 0, 'o', 0, 'o', 0, '!', 0, 0, pad(), pad(), 0, 0, 0, 0 };
        setMessageBody(data);

        assertEquals("String value", "this works", getInputObject().read_string());
        assertEquals("Wide string value", "This, too!", getInputObject().read_wstring());
        assertEquals("position before reading empty string", 54, getInputObject().getPosition());
        assertEquals("Empty string value", "", getInputObject().read_wstring());
    }

    @Test
    public void whenUsingV1_2_canReadCharAndWCharStrings() {
        useV1_2();
        int[] data = { 0, 0, 0, 11, 't', 'h', 'i', 's', ' ', 'w', 'o', 'r', 'k', 's', 0, pad(), 0, 0, 0, 22, FE, FF, 0, 'T', 0, 'h', 0, 'i',
                0, 's', 0, ',', 0, ' ', 0, 't', 0, 'o', 0, 'o', 0, '!', pad(), pad(), 0, 0, 0, 0 };
        setMessageBody(data);

        assertEquals("String value", "this works", getInputObject().read_string());
        assertEquals("Wide string value", "This, too!", getInputObject().read_wstring());
        assertEquals("Empty string value", "", getInputObject().read_wstring());
    }

    @Test
    public void can_read_boolean_array() throws Exception {
        final int[] data = { 1, FF, 0, 7, 0 };
        final boolean[] expected = { true, true, false, true, false };
        setMessageBody(data);
        readAndVerifyBooleanArray(expected);
    }

    private void readAndVerifyBooleanArray(boolean[] expected) {
        boolean[] actual = new boolean[expected.length];

        getInputObject().read_boolean_array(actual, 0, expected.length);

        if (!Arrays.equals(expected, actual)) {
            fail("Expected " + Arrays.toString(expected) + " but found " + Arrays.toString(actual));
        }
    }

    @Test
    public void can_read_octet_array() throws Exception {
        final int[] data = { 0, 1, 2, 3, -1, -1 };
        final byte[] expected = { 0, 1, 2, 3, -1, -1 };
        setMessageBody(data);
        readAndVerifyOctetArray(expected);
    }

    @Test
    public void can_read_octet_array_acrossFragments() throws Exception {
        useV1_2();
        final int[] data = { 0, 1, 2, 3 };
        final byte[] expected = { 0, 1, 2, 3, -1, -1 };
        setMessageBody(data);
        addFragment(-1, -1);
        readAndVerifyOctetArray(expected);
    }

    private void readAndVerifyOctetArray(byte[] expected) {
        byte[] actual = new byte[expected.length];

        getInputObject().read_octet_array(actual, 0, expected.length);
        assertArrayEquals("Octet array", expected, actual);
    }

    @Test
    public void can_read_short_array() throws Exception {
        final int[] data = { 0, 1, 2, 3, -1, -1 };
        final short[] expected = { 1, 515, -1 };
        setMessageBody(data);
        readAndVerifyShortArray(expected);
    }

    private void readAndVerifyShortArray(short[] expected) {
        short[] actual = new short[expected.length];

        getInputObject().read_short_array(actual, 0, expected.length);
        assertArrayEquals(expected, actual);
    }

    @Test
    public void can_read_ushort_array() throws Exception {
        final int[] data = { 0, 1, 2, 3 };
        final short[] expected = { 1, 515 };
        setMessageBody(data);
        readAndVerifyUshortArray(expected);
    }

    private void readAndVerifyUshortArray(short[] expected) {
        short[] actual = new short[expected.length];

        getInputObject().read_ushort_array(actual, 0, expected.length);
        assertArrayEquals(expected, actual);
    }

    @Test
    public void can_read_long_array() throws Exception {
        final int[] data = { 0, 1, 2, 3, -1, -1, -3, 30 };
        final int[] expected = { 66051, -738 };
        setMessageBody(data);
        readAndVerifyLongArray(expected);
    }

    private void readAndVerifyLongArray(int[] expected) {
        int[] actual = new int[expected.length];

        getInputObject().read_long_array(actual, 0, expected.length);
        assertArrayEquals(expected, actual);
    }

    @Test
    public void can_read_ulong_array() throws Exception {
        final int[] data = { 0, 1, 2, 3, -1, -1, -3, 30 };
        final int[] expected = { 66051, -738 };
        setMessageBody(data);
        readAndVerifyULongArray(expected);
    }

    private void readAndVerifyULongArray(int[] expected) {
        int[] actual = new int[expected.length];

        getInputObject().read_ulong_array(actual, 0, expected.length);

        assertArrayEquals(expected, actual);
    }

    @Test
    public void can_read_longlong_array() throws Exception {
        final int[] data = { pad(), pad(), pad(), pad(), 0, 0, 1, 0, 0, 0, 1, 7, -1, -1, -1, -1, -1, -1, -3, -20 };
        final long[] expected = { 1099511628039L, -532 };
        setMessageBody(data);

        readAndVerifyLongLongArray(expected);
    }

    private void readAndVerifyLongLongArray(long[] expected) {
        long[] actual = new long[expected.length];

        getInputObject().read_longlong_array(actual, 0, expected.length);

        assertArrayEquals(expected, actual);
    }

    @Test
    public void can_read_ulonglong_array() throws Exception {
        final int[] data = { pad(), pad(), pad(), pad(), 0, 0, 1, 0, 0, 0, 1, 7, FF, FF, FF, FF, FF, FF, -3, -20 };
        final long[] expected = { 1099511628039L, -532 };
        setMessageBody(data);
        readAndVerifyULongLongArray(expected);
    }

    private void readAndVerifyULongLongArray(long[] expected) {
        long[] actual = new long[expected.length];

        getInputObject().read_ulonglong_array(actual, 0, expected.length);

        if (!Arrays.equals(expected, actual)) {
            fail("Expected " + Arrays.toString(expected) + " but found " + Arrays.toString(actual));
        }
    }

    @Test
    public void can_read_char_array() throws Exception {
        final int[] data = { 'b', 'u', 'c', 'k', 'l', 'e', 'u', 'p' };
        final char[] expected = { 'b', 'u', 'c', 'k', 'l', 'e', 'u', 'p' };
        setMessageBody(data);
        readAndVerifyCharArray(expected);
    }

    private void readAndVerifyCharArray(char[] expected) {
        char[] actual = new char[expected.length];

        getInputObject().read_char_array(actual, 0, expected.length);

        assertArrayEquals(expected, actual);
    }

    @Test
    public void can_read_wchar_array() throws Exception {
        useV1_2();
        final int[] data = { 4, FE, FF, 0, 'b', 4, FE, FF, 0, 'u', 4, FF, FE, 't', 0 };
        final char[] expected = { 'b', 'u', 't' };
        setMessageBody(data);
        readAndVerifyWCharArray(expected);
    }

    private void readAndVerifyWCharArray(char[] expected) {
        char[] actual = new char[expected.length];

        getInputObject().read_wchar_array(actual, 0, expected.length);

        assertArrayEquals(expected, actual);
    }

    @Test(expected = MARSHAL.class)
    public void whenUsingV1_0_throwExceptionOnUnexpectedEndOfData() {
        useV1_0();
        setMessageBody(0, 0);
        getInputObject().read_long();
    }

    @Test
    public void whenUsingV1_2_headerPaddingForces8ByteAlignmentOnce() {
        useV1_2();
        setMessageBody(pad(), pad(), pad(), pad(), 0, 0, 1, 0, FF, FF, FF, FF);

        getInputObject().setHeaderPadding(true);
        assertEquals(256, getInputObject().read_long());
        assertEquals(-1, getInputObject().read_long());
    }

    @Test
    public void whenMarkIsSetInV1_0_restorePreservesByteOrder() {
        useV1_0();
        setMessageBody(1);
        getInputObject().consumeEndian();
        assertEquals(ByteOrder.LITTLE_ENDIAN, getInputObject().getByteOrder());
        getInputObject().mark(0);
        getInputObject().reset();
        assertEquals(ByteOrder.LITTLE_ENDIAN, getInputObject().getByteOrder());
    }

    @Test
    public void whenMarkIsSetInV1_0_restoreAllowsReread() {
        useV1_0();
        setMessageBody(0, 0, 1, 23, 'x');
        getInputObject().mark(0);
        assertEquals(0, getInputObject().read_short());
        getInputObject().reset();
        assertEquals(279, getInputObject().read_long());
    }

    @Test
    public void whenMarkIsSetInV1_2_restoreAllowsReread() {
        useV1_2();
        setMessageBody(0, 0, 1, 23, 'x');
        getInputObject().mark(0);
        assertEquals(0, getInputObject().read_short());
        getInputObject().reset();
        assertEquals(279, getInputObject().read_long());
    }

    @Test
    public void whenUsingV1_2_continueReadingOnToFragment() {
        useV1_2();
        setMessageBody(0, 0, 1, 23);
        addFragment(0, 7);
        getInputObject().read_long();
        assertEquals(7, getInputObject().read_short());
    }

    @Test
    public void whenUsingV1_2_skipPaddingBeforeReadingNextFragment() {
        useV1_2();
        setMessageBody(0, 23, pad(), pad());
        addFragment(0, 0, 0, 7);
        getInputObject().read_short();
        assertEquals(7, getInputObject().read_long());
    }

    @Test
    public void whenUsingV1_1_skipOptionalPaddingBeforeReadingNextFragment() {
        useV1_1();
        setMessageBody(0, 23, pad(), pad());
        addFragment(0, 0, 0, 7);
        getInputObject().read_short();
        assertEquals(7, getInputObject().read_long());
    }

    @Test
    public void whenUsingV1_1_alignToStartOfNextFragment() {
        useV1_1();
        setMessageBody(0, 23);
        addFragment(0, 0, 0, 7);
        getInputObject().read_short();
        assertEquals(7, getInputObject().read_long());
    }

    @Test(expected = RequestCanceledException.class)
    public void whenUsingV1_2_throwExceptionIfCanceled() {
        useV1_2();
        setMessageBody(0, 23, pad(), pad());
        getInputObject().read_short();
        getInputObject().cancelProcessing(0);
        getInputObject().read_long();
    }

    @Test(expected = MARSHAL.class)
    public void whenUsingV1_2_throwExceptionOnReadPastEnd() {
        useV1_2();
        setMessageBody(0, 23, pad(), pad());
        getInputObject().read_short();
        getInputObject().read_long();
    }

    @Test(expected = COMM_FAILURE.class)
    public void whenUsingV1_2_throwExceptionOnTimeout() {
        useV1_2();
        expectMoreFragments();
        setMessageBody(0, 23, pad(), pad());
        getInputObject().read_short();
        getInputObject().read_long();
    }

    @Test
    public void whenUsingV1_2_interruptedThreadDoesNotCauseTimeout() {
        useV1_2();
        setMessageBody(0, 23, pad(), pad());
        expectMoreFragments();

        whileWaitingForFragmentsDo(new AsynchronousAction() {
            int iteration = 0;

            public void exec() {
                if (iteration++ == 0)
                    Thread.currentThread().interrupt();
                else
                    addFragment(0, 0, 0, 7);
            }
        });

        getInputObject().read_short();
        getInputObject().read_long();
    }

    @Test(expected = RequestCanceledException.class)
    public void whenUsingV1_2_throwExceptionWhenCanceledDuringWait() {
        useV1_2();
        setMessageBody(0, 23, pad(), pad());
        expectMoreFragments();

        whileWaitingForFragmentsDo(new AsynchronousAction() {
            public void exec() {
                addFragment(0, 0, 0, 7);
                getInputObject().cancelProcessing(0);
            }
        });
        getInputObject().read_short();
        getInputObject().read_long();
    }

    @Test
    public void whenTypeCodeHasNoBody_readKindOnly() {
        setMessageBody(0, 0, 0, 6);
        TypeCode typeCode = getInputObject().read_TypeCode();
        assertEquals(TCKind.tk_float, typeCode.kind());
    }

    @Test
    public void whenTypeCodeIsString_readLength() throws BadKind {
        setMessageBody(0, 0, 0, 18, 0, 0, 1, 0);
        TypeCode typeCode = getInputObject().read_TypeCode();
        assertEquals(TCKind.tk_string, typeCode.kind());
        assertEquals(256, typeCode.length());
    }

    @Test
    public void whenTypeCodeIsFixed_readDigitsAndScale() throws BadKind {
        setMessageBody(0, 0, 0, 28, 0, 10, 0, 6);
        TypeCode typeCode = getInputObject().read_TypeCode();
        assertEquals(TCKind.tk_fixed, typeCode.kind());
        assertEquals(10, typeCode.fixed_digits());
        assertEquals(6, typeCode.fixed_scale());
    }

    @Test
    public void whenValueIsZero_returnNull() {
        setMessageBody(0, 0, 0, 0);
        assertNull(getInputObject().read_value());
    }

    @Test
    public void whenCloneEncapsInputStream_readFromClone() {
        setMessageBody();
        EncapsInputStream inputStream1 = createEncapsulatedInputStream(0, 0, 1, 5);
        EncapsInputStream inputStream2 = new EncapsInputStream(inputStream1);
        assertEquals(261, inputStream2.read_long());
    }

}
