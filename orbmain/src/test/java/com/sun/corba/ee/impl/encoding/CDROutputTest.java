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

import com.sun.corba.ee.impl.corba.AnyImpl;
import com.sun.corba.ee.impl.corba.TypeCodeImpl;
import com.sun.corba.ee.impl.io.ValueUtility;
import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.orb.ORBVersionFactory;
import org.junit.Test;
import org.omg.CORBA.BAD_PARAM;
import org.omg.CORBA.MARSHAL;
import org.omg.CORBA.TCKind;
import org.omg.CORBA.TypeCode;
import org.omg.CORBA_2_3.portable.InputStream;

import java.io.IOException;
import java.io.Serializable;

import static org.junit.Assert.assertEquals;

public class CDROutputTest extends EncodingTestBase {

    @Test
    public void whenCDROutputObjectCreated_canWriteBoolean() throws IOException {
        getOutputObject().write_boolean(false);
        getOutputObject().write_boolean(true);

        expectByteArray(new byte[] { 0, 1 });
    }

    @Test
    public void canWriteLatin1Char() throws IOException {
        setCharEncoding(ISO_8859_1);
        getOutputObject().write_char('x');

        expectByteArray('x');
    }

    @Test
    public void caWriteUTF8Char() throws IOException {
        setCharEncoding(UTF_8);
        getOutputObject().write_char('x');

        expectByteArray('x');
    }

    @Test(expected = MARSHAL.class)
    public void whenCDROutputObjectCreated_cannotWriteUTF16CharIn_1_0() throws IOException {
        useV1_0();
        setWCharEncoding(UTF_16);

        getOutputObject().write_wchar('\u3456');
    }

    @Test
    public void canWriteUTF16CharIn_1_0WithLegacyORB() throws IOException {
        useV1_0();
        setOrbVersion(ORBVersionFactory.getOLD());
        setWCharEncoding(UTF_16);

        getOutputObject().write_wchar('\u3456');
        expectByteArray(0x34, 0x56);
    }

    @Test
    public void canWriteUTF16CharIn_1_1() throws IOException {
        useV1_1();
        setWCharEncoding(UTF_16);

        getOutputObject().write_wchar('\u3456');
        expectByteArray(0x34, 0x56);
    }

    @Test
    public void canWriteUTF16CharIn_1_2() throws IOException {
        useV1_2();
        setWCharEncoding(UTF_16);

        getOutputObject().write_wchar('\u3456');
        expectByteArray(0x04, FE, FF, 0x34, 0x56);
    }

    @Test
    public void canWriteIntegers() {
        getOutputObject().write_octet((byte) 4);
        getOutputObject().write_short((short) -14);
        getOutputObject().write_ushort((short) 3);
        getOutputObject().write_ulong(66179);
        getOutputObject().write_long(-655);
        getOutputObject().write_longlong(1099520016647L);
        getOutputObject().write_ulonglong(1099511628034L);

        expectByteArray(0x04, PAD, FF, (byte) 0xf2, 0x00, 0x03, // short
                PAD, PAD, 0, 1, 2, (byte) 0x83, // long1
                FF, FF, (byte) 0xfd, 0x71, // long2
                PAD, PAD, PAD, PAD, 0, 0, 1, 0, 0, (byte) 0x80, 1, 7, // longlong1
                0, 0, 1, 0, 0, 0, 1, 2); // ulonglong2
    }

    @Test
    public void canWritefloats() {
        getOutputObject().write_float(1);
        getOutputObject().write_double(0.25);
        expectByteArray(0x3f, 0x80, 0, 0, 0x3f, 0xd0, 0, 0, 0, 0, 0, 0);
    }

    @Test
    public void can_write_float_arrays() {
        getOutputObject().write_float_array(new float[] { 1, .0625f }, 0, 2);

        expectByteArray(0x3f, 0x80, 0, 0, 0x3d, 0x80, 0, 0);
    }

    @Test
    public void can_write_double_arrays() {
        getOutputObject().write_double_array(new double[] { 0.25, 2 }, 0, 2);

        expectByteArray(PAD, PAD, PAD, PAD, 0x3f, 0xd0, 0, 0, 0, 0, 0, 0, 0x40, 0, 0, 0, 0, 0, 0, 0);
    }

    @Test
    public void whenUsingV1_0_canWriteCharString() {
        useV1_0();
        getOutputObject().write_string("this works");

        expectByteArray(0, 0, 0, 11, 't', 'h', 'i', 's', ' ', 'w', 'o', 'r', 'k', 's', 0);
    }

    @Test(expected = BAD_PARAM.class)
    public void whenNullStringWritten_throwException() {
        getOutputObject().write_string(null);
    }

    @Test(expected = BAD_PARAM.class)
    public void whenNullWStringWritten_throwExceptionIn1_0() {
        useV1_0();
        getOutputObject().write_wstring(null);
    }

    @Test(expected = BAD_PARAM.class)
    public void whenNullWStringWritten_throwExceptionIn1_1() {
        useV1_1();
        getOutputObject().write_wstring(null);
    }

    @Test(expected = BAD_PARAM.class)
    public void whenNullWStringWritten_throwExceptionIn1_2() {
        useV1_2();
        getOutputObject().write_wstring(null);
    }

    @Test(expected = MARSHAL.class)
    public void whenWriteWStringToForeignOrb_throwException() {
        useV1_0();
        getOutputObject().write_wstring("This should fail");
    }

    @Test
    public void whenUsingV1_0WithLegacyOrb_canReadWCharString() {
        useV1_0();
        setOrbVersion(ORBVersionFactory.getOLD());
        getOutputObject().write_wstring("This works");

        expectByteArray(0, 0, 0, 11, 0, 'T', 0, 'h', 0, 'i', 0, 's', 0, ' ', 0, 'w', 0, 'o', 0, 'r', 0, 'k', 0, 's', 0, 0);
    }

    @Test
    public void whenUsingV1_1_canWriteCharAndWCharStrings() {
        useV1_1();
        getOutputObject().write_string("this works");
        getOutputObject().write_wstring("This, too!");
        expectByteArray(0, 0, 0, 11, 't', 'h', 'i', 's', ' ', 'w', 'o', 'r', 'k', 's', 0, PAD, 0, 0, 0, 11, 0, 'T', 0, 'h', 0, 'i', 0, 's', 0, ',', 0, ' ', 0,
                't', 0, 'o', 0, 'o', 0, '!', 0, 0);
    }

    @Test
    public void whenUsingV1_2_canWriteCharAndWCharStrings() {
        useV1_2();
        getOutputObject().write_string("this works");
        getOutputObject().write_wstring("This, too!");
        expectByteArray(0, 0, 0, 11, 't', 'h', 'i', 's', ' ', 'w', 'o', 'r', 'k', 's', 0, PAD, 0, 0, 0, 22, FE, FF, 0, 'T', 0, 'h', 0, 'i', 0, 's', 0, ',', 0,
                ' ', 0, 't', 0, 'o', 0, 'o', 0, '!');
    }

    @Test
    public void canWriteBooleanArray() {
        getOutputObject().write_boolean_array(new boolean[] { true, true, false, true, false }, 0, 4);

        expectByteArray(1, 1, 0, 1);
    }

    @Test
    public void canWriteOctetArray() {
        getOutputObject().write_octet_array(new byte[] { 2, -3, 6, 2 }, 0, 4);

        expectByteArray(2, -3, 6, 2);
    }

    @Test
    public void writingEmptyOctetArray_doesNotDoEightByteAlign() {
        getOutputObject().setHeaderPadding(true);
        getOutputObject().write_octet_array(new byte[] {}, 0, 0);

        expectByteArray(new byte[0]);
    }

    @Test(expected = BAD_PARAM.class)
    public void whenWritingNullArray_anExceptionIsThrown() {
        getOutputObject().write_octet_array(null, 0, 4);
    }

    @Test
    public void canWriteShortArray() {
        getOutputObject().write_short_array(new short[] { -3, 1, 515, -1 }, 1, 3);

        expectByteArray(0, 1, 2, 3, -1, -1);
    }

    @Test
    public void canWriteUShortArray() {
        getOutputObject().write_short_array(new short[] { -3, 1, 515, -1 }, 0, 2);

        expectByteArray(FF, 0xfd, 0, 1);
    }

    @Test
    public void canWriteLongArray() {
        getOutputObject().write_long_array(new int[] { 66051, -738 }, 0, 2);

        expectByteArray(0, 1, 2, 3, -1, -1, -3, 30);
    }

    @Test
    public void canWriteULongArray() {
        getOutputObject().write_ulong_array(new int[] { 66051, -738 }, 0, 2);

        expectByteArray(0, 1, 2, 3, -1, -1, -3, 30);
    }

    @Test
    public void canWriteLongLongArray() {
        getOutputObject().write_longlong_array(new long[] { 1099511628039L, -532 }, 0, 2);

        expectByteArray(PAD, PAD, PAD, PAD, 0, 0, 1, 0, 0, 0, 1, 7, -1, -1, -1, -1, -1, -1, -3, -20);
    }

    @Test
    public void canWriteULongLongArray() {
        getOutputObject().write_ulonglong_array(new long[] { 1099511628039L, -532 }, 0, 2);

        expectByteArray(PAD, PAD, PAD, PAD, 0, 0, 1, 0, 0, 0, 1, 7, -1, -1, -1, -1, -1, -1, -3, -20);
    }

    @Test
    public void canWriteCharArray() {
        getOutputObject().write_char_array(new char[] { 'b', 'u', 'c', 'k', 'l', 'e', 'u', 'p' }, 0, 8);

        expectByteArray('b', 'u', 'c', 'k', 'l', 'e', 'u', 'p');
    }

    @Test
    public void canWriteWCharArray() {
        getOutputObject().write_wchar_array(new char[] { 'b', 'u', 't' }, 0, 3);

        expectByteArray(4, FE, FF, 0, 'b', 4, FE, FF, 0, 'u', 4, FE, FF, 0, 't');
    }

    @Test
    public void canWriteTypeCode_withNoBody() {
        getOutputObject().write_TypeCode(new TypeCodeImpl((ORB) getOutputObject().orb(), TCKind._tk_float));

        expectByteArray(0, 0, 0, 6);
    }

    @Test
    public void canWriteStringTypeCode() {
        getOutputObject().write_TypeCode(new TypeCodeImpl((ORB) getOutputObject().orb(), TCKind._tk_string, 256));

        expectByteArray(0, 0, 0, 18, 0, 0, 1, 0);
    }

    @Test
    public void canWriteSerializableTypeCode() {
        String KNOWN_TYPE_CODE = "0000001D000000CE0000000000000067524D493A636F6D2E73756E2E636F7262612E65652E696D706C"
                + "2E656E636F64696E672E4344524F7574707574546573745C553030323453657269616C697A65644461"
                + "74613A323837394346383133394444433741463A393031343243313746444330444632410000000000"
                + "3C636F6D2E73756E2E636F7262612E65652E696D706C2E656E636F64696E672E4344524F7574707574"
                + "546573742453657269616C697A65644461746100000000000000000000000001000000066142797465" + "0000000000000A0000";
        TypeCode typeCode = AnyImpl.createTypeCodeForClass(SerializedData.class, getOrb());
        getOutputObject().write_TypeCode(typeCode);

        expectByteArray(hexStringToByteArray(KNOWN_TYPE_CODE));
    }

    static class SerializedData implements Serializable {
        byte aByte;
    }

    private byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    @Test
    public void canWriteObjRefTypeCode() {
        getOutputObject().write_TypeCode(new TypeCodeImpl((ORB) getOutputObject().orb(), TCKind._tk_objref, "aa", "bb"));

        expectByteArray(0, 0, 0, 14, 0, 0, 0, 19, 0, 0, 0, 0, 0, 0, 0, 3, 'a', 'a', 0, 0, 0, 0, 0, 3, 'b', 'b', 0);
    }

    @Test
    public void WhenOutputStreamClosed_releaseBuffer() throws IOException {
        getOutputObject().write_ulong(123);
        getOutputObject().close();

        assertEquals(1, getNumBuffersReleased());
    }

    @Test
    public void WhenOutputStreamClosedFirst_sharedBuffersAreOnlyReleasedOnce() throws IOException {
        getOutputObject().write_ulong(123);
        CDRInputObject inputObject = getOutputObject().createInputObject(getOrb());
        getOutputObject().getMessageMediator().setInputObject(inputObject);
        inputObject.setMessageMediator(getOutputObject().getMessageMediator());
        assertEquals(123, inputObject.read_ulong());
        getOutputObject().close();
        inputObject.close();

        assertEquals(1, getNumBuffersReleased());
    }

    @Test
    public void WhenInputStreamClosedFirst_sharedBuffersAreOnlyReleasedOnce() throws IOException {
        getOutputObject().write_ulong(123);
        CDRInputObject inputObject = getOutputObject().createInputObject(getOrb());
        getOutputObject().getMessageMediator().setInputObject(inputObject);
        inputObject.setMessageMediator(getOutputObject().getMessageMediator());
        getOutputObject().getMessageMediator().setOutputObject(getOutputObject());
        assertEquals(123, inputObject.read_ulong());
        inputObject.close();
        getOutputObject().close();

        assertEquals(1, getNumBuffersReleased());
    }

    @Test
    public void WhenEncapsOutputStreamClosedFirst_sharedBuffersAreOnlyReleasedOnce() throws IOException {
        EncapsOutputStream os = new EncapsOutputStream(getOrb());
        os.write_ulong(123);
        InputStream is = (InputStream) (os.create_input_stream());
        assertEquals(123, is.read_ulong());
        os.close();
        is.close();

        assertEquals(1, getNumBuffersReleased());
    }

    @Test
    public void WhenEncapsInputStreamClosedFirst_sharedBuffersAreOnlyReleasedOnce() throws IOException {
        EncapsOutputStream os = new EncapsOutputStream(getOrb());
        os.write_ulong(123);
        InputStream is = (InputStream) (os.create_input_stream());
        assertEquals(123, is.read_ulong());
        is.close();
        os.close();

        assertEquals(1, getNumBuffersReleased());
    }

    @Test
    public void WhenTypeCodeOutputStreamClosedFirst_sharedBuffersAreOnlyReleasedOnce() throws IOException {
        TypeCodeOutputStream os = new TypeCodeOutputStream(getOrb());
        os.write_ulong(123);
        InputStream is = (InputStream) (os.create_input_stream());
        assertEquals(123, is.read_ulong());
        os.close();
        is.close();

        assertEquals(1, getNumBuffersReleased());
    }

    @Test
    public void WhenTypeCodeInputStreamClosedFirst_sharedBuffersAreOnlyReleasedOnce() throws IOException {
        TypeCodeOutputStream os = new TypeCodeOutputStream(getOrb());
        os.write_ulong(123);
        InputStream is = (InputStream) (os.create_input_stream());
        assertEquals(123, is.read_ulong());
        is.close();
        os.close();

        assertEquals(1, getNumBuffersReleased());
    }

    /*
     *
     * @Test(expected = MARSHAL.class) public void whenUsingV1_0_throwExceptionOnUnexpectedEndOfData() { useV1_0();
     * setMessageBody(0, 0); getInputObject().read_long(); }
     *
     * @Test public void whenUsingV1_2_headerPaddingForces8ByteAlignmentOnce() { useV1_2(); setMessageBody(pad(), pad(),
     * pad(), pad(), 0, 0, 1, 0, FF, FF, FF, FF);
     *
     * getInputObject().setHeaderPadding(true); assertEquals(256, getInputObject().read_long()); assertEquals(-1,
     * getInputObject().read_long()); }
     *
     * @Test public void whenMarkIsSetInV1_0_restoreAllowsReread() { useV1_0(); setMessageBody(0, 0, 1, 23, 'x');
     * getInputObject().mark(0); assertEquals(0, getInputObject().read_short()); getInputObject().reset(); assertEquals(279,
     * getInputObject().read_long()); }
     *
     * @Test public void whenMarkIsSetInV1_2_restoreAllowsReread() { useV1_2(); setMessageBody(0, 0, 1, 23, 'x');
     * getInputObject().mark(0); assertEquals(0, getInputObject().read_short()); getInputObject().reset(); assertEquals(279,
     * getInputObject().read_long()); }
     *
     * @Test public void whenUsingV1_2_continueReadingOnToFragment() { useV1_2(); setMessageBody(0, 0, 1, 23);
     * addFragment(0, 7); getInputObject().read_long(); assertEquals(7, getInputObject().read_short()); }
     *
     * @Test public void whenUsingV1_2_skipPaddingBeforeReadingNextFragment() { useV1_2(); setMessageBody(0, 23, pad(),
     * pad()); addFragment(0, 0, 0, 7); getInputObject().read_short(); assertEquals(7, getInputObject().read_long()); }
     *
     * @Test public void whenUsingV1_1_skipOptionalPaddingBeforeReadingNextFragment() { useV1_1(); setMessageBody(0, 23,
     * pad(), pad()); addFragment(0, 0, 0, 7); getInputObject().read_short(); assertEquals(7, getInputObject().read_long());
     * }
     *
     * @Test public void whenUsingV1_1_alignToStartOfNextFragment() { useV1_1(); setMessageBody(0, 23); addFragment(0, 0, 0,
     * 7); getInputObject().read_short(); assertEquals(7, getInputObject().read_long()); }
     *
     * @Test(expected = RequestCanceledException.class) public void whenUsingV1_2_throwExceptionIfCanceled() { useV1_2();
     * setMessageBody(0, 23, pad(), pad()); getInputObject().read_short(); getInputObject().cancelProcessing(0);
     * getInputObject().read_long(); }
     *
     * @Test(expected = MARSHAL.class) public void whenUsingV1_2_throwExceptionOnReadPastEnd() { useV1_2();
     * setMessageBody(0, 23, pad(), pad()); getInputObject().read_short(); getInputObject().read_long(); }
     *
     * @Test(expected = COMM_FAILURE.class) public void whenUsingV1_2_throwExceptionOnTimeout() { useV1_2();
     * expectMoreFragments(); setMessageBody(0, 23, pad(), pad()); getInputObject().read_short();
     * getInputObject().read_long(); }
     *
     * @Test(expected = RequestCanceledException.class) public void whenUsingV1_2_throwExceptionWhenCanceledDuringWait() {
     * useV1_2(); setMessageBody(0, 23, pad(), pad()); expectMoreFragments();
     *
     * whileWaitingForFragmentsDo(new AsynchronousAction() { public void exec() { addFragment(0, 0, 0, 7);
     * getInputObject().cancelProcessing(0); } }); getInputObject().read_short(); getInputObject().read_long(); }
     *
     * @Test public void whenValueIsZero_returnNull() { setMessageBody(0, 0, 0, 0);
     * assertNull(getInputObject().read_value()); }
     *
     * @Test public void whenCloneEncapsInputStream_readFromClone() { setMessageBody(); EncapsInputStream inputStream1 =
     * createEncapsulatedInputStream(0, 0, 1, 5); EncapsInputStream inputStream2 = new EncapsInputStream(inputStream1);
     * assertEquals(261, inputStream2.read_long()); }
     *
     * @Test public void canWriteLittleEndianUTF16CharIn_1_0WithLegacyORB() throws IOException { useV1_0();
     * useLittleEndian(); setOrbVersion(ORBVersionFactory.getOLD()); setWCharEncoding(UTF_16);
     *
     * getOutputObject().write_wchar('\u5634'); expectByteArray(0x34, 0x56); }
     *
     * @Test public void can_read_little_endian_integers() { setMessageBody(0x04, pad(), 0xf2, FF,// short 0x03, 0x00,//
     * ushort pad(), pad(),// for long 0x83, 2, 1, 0,// long 0x71, 0xfd, FF, FF,// ulong pad(), pad(), pad(), pad(),// for
     * long_long 7, 1, 0x80, 0, 0, 1, 0, 0);// long long useLittleEndian();
     *
     * assertEquals("Octet value", 4, getInputObject().read_octet()); assertEquals("Signed short value", -14,
     * getInputObject().read_short()); assertEquals("Standard unsigned short value", 3, getInputObject().read_ushort());
     * assertEquals("Unsigned long value", 66179, getInputObject().read_ulong()); assertEquals("Long value", -655,
     * getInputObject().read_long()); assertEquals("Long long value", 1099520016647L, getInputObject().read_longlong()); }
     *
     * @Test public void canReadStringFromOldOrbAcrossFragment() { useV1_1(); setOrbVersion(ORBVersionFactory.getOLD());
     * setMessageBody(0, 0, 0, 9, 'a', 'b', 'c', 'd'); addFragment('e', 'f', 'g', 'h', 0); assertEquals("abcdefgh",
     * getInputObject().read_string()); }
     *
     * @Test public void can_read_octet_array_acrossFragments() throws Exception { useV1_2(); final int[] data = {0, 1, 2,
     * 3}; final byte[] expected = {0, 1, 2, 3, -1, -1}; setMessageBody(data); addFragment(-1, -1);
     * readAndVerifyOctetArray(expected); }
     *
     * /
     **/
}
