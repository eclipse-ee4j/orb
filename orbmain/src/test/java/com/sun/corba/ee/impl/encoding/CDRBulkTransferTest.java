/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Covers the cases that distinguish the bulk primitive transfers and the
 * single byte string path from the element at a time versions they replaced.
 *
 * <p>The straightforward cases are already covered by {@link CDRInputTest} and
 * {@link CDROutputTest} and act as the regression net. What is added here is
 * what the batching loop introduced and the old loop could not get wrong:
 * arrays interrupted by a fragment boundary, a destination offset, a byte
 * order that is not the platform's, and the boundary between the string fast
 * path and the CharsetDecoder fallback.
 */
public class CDRBulkTransferTest extends EncodingTestBase {

    // ---- primitive arrays across a fragment boundary ----------------------

    @Test
    public void can_read_long_array_acrossFragments() {
        useV1_2();
        setMessageBody(0, 0, 0, 1,
                       0, 0, 0, 2);
        addFragment(0, 0, 0, 3,
                    0, 0, 0, 4);

        int[] actual = new int[4];
        getInputObject().read_long_array(actual, 0, 4);
        assertArrayEquals(new int[] { 1, 2, 3, 4 }, actual);
    }

    @Test
    public void can_read_short_array_acrossFragments() {
        useV1_2();
        setMessageBody(0, 1, 0, 2);
        addFragment(0, 3, 0, 4);

        short[] actual = new short[4];
        getInputObject().read_short_array(actual, 0, 4);
        assertArrayEquals(new short[] { 1, 2, 3, 4 }, actual);
    }

    @Test
    public void can_read_double_array_acrossFragments() {
        useV1_2();
        // The GIOP body starts at stream offset 4, so an 8 byte type needs
        // four bytes of alignment padding before it.
        setMessageBody(pad(), pad(), pad(), pad(),
                       0x3f, 0xd0, 0, 0, 0, 0, 0, 0);
        addFragment(0x40, 0, 0, 0, 0, 0, 0, 0);

        double[] actual = new double[2];
        getInputObject().read_double_array(actual, 0, 2);
        assertEquals(0.25, actual[0], 0.0);
        assertEquals(2.0, actual[1], 0.0);
    }

    // ---- offsets and lengths ----------------------------------------------

    @Test
    public void can_read_long_array_intoAnOffset() {
        setMessageBody(0, 0, 0, 7,
                       0, 0, 0, 8);

        int[] actual = new int[5];
        getInputObject().read_long_array(actual, 2, 2);
        assertArrayEquals(new int[] { 0, 0, 7, 8, 0 }, actual);
    }

    @Test
    public void reading_a_zero_length_array_consumes_nothing() {
        setMessageBody(0, 0, 0, 9);

        getInputObject().read_long_array(new int[0], 0, 0);
        assertEquals("the stream must not have advanced", 9, getInputObject().read_long());
    }

    // ---- byte order --------------------------------------------------------

    @Test
    public void can_read_long_array_littleEndian() {
        useLittleEndian();
        setMessageBody(1, 0, 0, 0,
                       2, 1, 0, 0);

        int[] actual = new int[2];
        getInputObject().read_long_array(actual, 0, 2);
        assertArrayEquals("the bulk view must honour the stream's byte order",
                new int[] { 1, 0x102 }, actual);
    }

    @Test
    public void can_read_short_array_littleEndian() {
        useLittleEndian();
        setMessageBody(1, 0, 0x34, 0x12);

        short[] actual = new short[2];
        getInputObject().read_short_array(actual, 0, 2);
        assertArrayEquals(new short[] { 1, 0x1234 }, actual);
    }

    // ---- char arrays --------------------------------------------------------

    @Test
    public void can_read_char_array() {
        setCharEncoding(ISO_8859_1);
        setMessageBody('h', 'e', 'l', 'l', 'o');

        char[] actual = new char[5];
        getInputObject().read_char_array(actual, 0, 5);
        assertArrayEquals(new char[] { 'h', 'e', 'l', 'l', 'o' }, actual);
    }

    @Test
    public void can_read_char_array_intoAnOffset() {
        setCharEncoding(ISO_8859_1);
        setMessageBody('a', 'b');

        char[] actual = new char[4];
        getInputObject().read_char_array(actual, 1, 2);
        assertArrayEquals(new char[] { 0, 'a', 'b', 0 }, actual);
    }

    @Test
    public void can_read_char_array_acrossFragments() {
        useV1_2();
        setCharEncoding(ISO_8859_1);
        setMessageBody('a', 'b', 'c', 'd');
        addFragment('e', 'f');

        char[] actual = new char[6];
        getInputObject().read_char_array(actual, 0, 6);
        assertArrayEquals(new char[] { 'a', 'b', 'c', 'd', 'e', 'f' }, actual);
    }

    // ---- strings -------------------------------------------------------------

    @Test
    public void iso8859_1_stringUsesEveryByteValue() {
        // 0xE8 is 'e with grave' in Latin-1. It is also the case that would
        // break if the fast path were extended to US-ASCII, where the same
        // byte is malformed input and must raise a CORBA exception rather
        // than quietly become U+FFFD.
        setCharEncoding(ISO_8859_1);
        setMessageBody(0, 0, 0, 6, 'c', 'a', 'f', 'f', 0xE8, 0);

        assertEquals("caffè", getInputObject().read_string());
    }

    @Test
    public void utf8_stringStillGoesThroughTheDecoder() {
        // UTF-8 is not eligible for the fast path, so this exercises the
        // fallback and proves multi byte sequences are unaffected.
        setCharEncoding(UTF_8);
        setMessageBody(0, 0, 0, 7, 'c', 'a', 'f', 'f', 0xC3, 0xA8, 0);

        assertEquals("caffè", getInputObject().read_string());
    }

    @Test
    public void can_read_empty_string() {
        setCharEncoding(ISO_8859_1);
        setMessageBody(0, 0, 0, 1, 0);

        assertEquals("", getInputObject().read_string());
    }

    @Test
    public void can_read_string_thenContinueReading() {
        setCharEncoding(ISO_8859_1);
        setMessageBody(0, 0, 0, 3, 'h', 'i', 0,
                       pad(),
                       0, 0, 0, 42);

        assertEquals("hi", getInputObject().read_string());
        assertEquals("the null terminator must have been consumed, leaving"
                        + " only the alignment padding before the next value",
                42, getInputObject().read_long());
    }

    // ---- writing --------------------------------------------------------------

    @Test
    public void can_write_long_array_fromAnOffset() {
        getOutputObject().write_long_array(new int[] { 9, 9, 1, 2 }, 2, 2);

        expectByteArray(0, 0, 0, 1,
                        0, 0, 0, 2);
    }

    @Test
    public void can_write_short_array() {
        getOutputObject().write_short_array(new short[] { 1, 0x1234 }, 0, 2);

        expectByteArray(0, 1, 0x12, 0x34);
    }

    @Test
    public void can_write_longlong_array() {
        getOutputObject().write_longlong_array(new long[] { 1, 2 }, 0, 2);

        expectByteArray(PAD, PAD, PAD, PAD,
                        0, 0, 0, 0, 0, 0, 0, 1,
                        0, 0, 0, 0, 0, 0, 0, 2);
    }

    @Test
    public void writing_a_zero_length_array_writes_nothing() {
        getOutputObject().write_long_array(new int[0], 0, 0);
        getOutputObject().write_long(5);

        expectByteArray(0, 0, 0, 5);
    }
}
