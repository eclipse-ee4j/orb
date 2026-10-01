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
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.Test;
import org.omg.CORBA.DATA_CONVERSION;

import com.sun.corba.ee.impl.protocol.giopmsgheaders.Message;

/**
 * GIOP 1.2 wstrings in UTF-16, which the streams now copy as code units
 * rather than running through a CharsetEncoder or CharsetDecoder. Every
 * expectation here is what the encoder and decoder produce, so these tests
 * pin the wire format and the error behaviour to what they were.
 */
public class CDRWStringTest extends EncodingTestBase {

    @Test
    public void surrogate_filter_covers_every_code_unit_in_every_lane_and_tail() {
        char[] chars = new char[13];
        for (int value = 0; value <= Character.MAX_VALUE; value++) {
            for (int index = 1; index < 12; index++) {
                chars[index] = (char) value;
                assertEquals(value >= 0xD800,
                        CDROutputStream_1_0.mayContainSurrogate(chars, 1, 12));
                chars[index] = 0;
            }
        }
        chars[0] = chars[12] = '\uFFFF';
        for (int length = 0; length <= 11; length++) {
            assertEquals(false, CDROutputStream_1_0.mayContainSurrogate(chars, 1, 1 + length));
        }
    }

    private static final String LATIN = "Grüße, ça va? naïve café";
    private static final String CJK = "漢字とかな、한국어";
    private static final String WITH_PAIR = "a😀b";   // U+1F600 as a surrogate pair
    private static final String WITH_ZWNBSP = "x﻿y";   // U+FEFF inside the text stays text

    // ---- writing -------------------------------------------------------------

    @Test
    public void writes_latin1_text_exactly_as_the_encoder_did() {
        expectWritten(LATIN);
    }

    @Test
    public void writes_cjk_text_exactly_as_the_encoder_did() {
        expectWritten(CJK);
    }

    @Test
    public void writes_a_surrogate_pair_exactly_as_the_encoder_did() {
        expectWritten(WITH_PAIR);
    }

    @Test
    public void writes_an_inner_byte_order_mark_as_text() {
        expectWritten(WITH_ZWNBSP);
    }

    @Test(expected = DATA_CONVERSION.class)
    public void rejects_a_lone_high_surrogate_at_the_end() {
        useV1_2();
        getOutputObject().write_wstring("abc\uD83D");
    }

    @Test(expected = DATA_CONVERSION.class)
    public void rejects_a_lone_high_surrogate_before_another_char() {
        useV1_2();
        getOutputObject().write_wstring("a\uD83Db");
    }

    @Test(expected = DATA_CONVERSION.class)
    public void rejects_a_lone_low_surrogate() {
        useV1_2();
        getOutputObject().write_wstring("a\uDE00b");
    }

    @Test
    public void accepts_a_surrogate_pair_split_between_two_scratch_chunks() {
        useV1_2();
        setFragmentSize(1 << 20);
        setBufferSize(1 << 20);
        // The chars are copied out of the String 8192 at a time.
        String value = "x".repeat(8191) + "😀" + "y";
        getOutputObject().write_wstring(value);
        assertEquals(value, new String(payloadOf(finishAndGetFragments()), 2, 2 * value.length(),
                StandardCharsets.UTF_16BE));
    }

    @Test
    public void a_long_string_is_split_across_fragments_at_every_byte_offset_the_encoder_would_use() {
        useV1_2();
        // The test ORB uses 250 byte fragments; 250 is not even, so some code
        // units straddle a fragment boundary.
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            sb.append((char) ('a' + i % 26)).append(i % 7 == 0 ? "€" : "");
        }
        String value = sb.toString();

        getOutputObject().write_wstring(value);
        List<byte[]> fragments = finishAndGetFragments();

        assertTrue("the string must have been fragmented", fragments.size() > 1);
        assertArrayEquals(wstringBytes(value), payloadOf(fragments));
    }

    @Test
    public void a_code_unit_straddling_a_fragment_boundary_is_split_where_the_encoder_split_it() {
        useV1_2();
        // An odd buffer size leaves one octet free at the end of a buffer, so
        // a code unit has to be written half in one fragment, half in the next.
        setFragmentSize(251);
        setBufferSize(251);
        String value = "é€".repeat(300);

        getOutputObject().write_wstring(value);
        List<byte[]> fragments = finishAndGetFragments();

        assertTrue("the string must have been fragmented", fragments.size() > 1);
        assertEquals("a fragment must end in the middle of a code unit", 1,
                (fragments.get(0).length - Message.GIOPMessageHeaderLength - 4 - 2) % 2);
        assertArrayEquals(wstringBytes(value), payloadOf(fragments));
    }

    // ---- reading -------------------------------------------------------------

    @Test
    public void reads_big_endian_with_a_byte_order_mark() {
        useV1_2();
        setMessageBody(withLength(bytes(0xFE, 0xFF, 0, 'h', 0, 'i')));
        assertEquals("hi", getInputObject().read_wstring());
    }

    @Test
    public void reads_little_endian_with_a_byte_order_mark() {
        useV1_2();
        setMessageBody(withLength(bytes(0xFF, 0xFE, 'h', 0, 'i', 0)));
        assertEquals("hi", getInputObject().read_wstring());
    }

    @Test
    public void reads_the_stream_byte_order_without_a_byte_order_mark() {
        useV1_2();
        setMessageBody(withLength(bytes(0, 'h', 0, 'i')));
        assertEquals("hi", getInputObject().read_wstring());
    }

    @Test
    public void reads_big_endian_without_a_byte_order_mark_even_in_a_little_endian_stream() {
        // What the decoder has always done: the default for unmarked UTF-16
        // does not follow the stream's byte order.
        useV1_2();
        useLittleEndian();
        setMessageBody(4, 0, 0, 0, 0, 'h', 0, 'i');
        assertEquals("hi", getInputObject().read_wstring());
    }

    @Test
    public void two_octets_are_never_taken_for_a_byte_order_mark() {
        // UTF16BTCConverter only looks for a mark when there are four octets.
        useV1_2();
        setMessageBody(withLength(bytes(0xFE, 0xFF)));
        assertEquals("﻿", getInputObject().read_wstring());
    }

    @Test
    public void reads_cjk_text() {
        useV1_2();
        setMessageBody(withLength(wstringBytes(CJK)));
        assertEquals(CJK, getInputObject().read_wstring());
    }

    @Test
    public void reads_a_surrogate_pair() {
        useV1_2();
        setMessageBody(withLength(wstringBytes(WITH_PAIR)));
        assertEquals(WITH_PAIR, getInputObject().read_wstring());
    }

    @Test
    public void reads_private_use_chars_that_pass_the_filter_but_are_ordinary() {
        useV1_2();
        String value = "aＡz";
        setMessageBody(withLength(wstringBytes(value)));
        assertEquals(value, getInputObject().read_wstring());
    }

    @Test(expected = DATA_CONVERSION.class)
    public void rejects_a_lone_surrogate_the_way_the_decoder_did() {
        useV1_2();
        setMessageBody(withLength(bytes(0xFE, 0xFF, 0, 'a', 0xDE, 0x00, 0, 'b')));
        getInputObject().read_wstring();
    }

    @Test
    public void reads_a_code_unit_split_between_fragments() {
        useV1_2();
        expectMoreFragments();
        setMessageBody(0, 0, 0, 6, 0xFE, 0xFF, 0, 'h', 0);
        addFragment('i');
        assertEquals("hi", getInputObject().read_wstring());
    }

    @Test
    public void reads_a_byte_order_mark_split_between_fragments() {
        useV1_2();
        expectMoreFragments();
        setMessageBody(0, 0, 0, 6, 0xFE);
        addFragment(0xFF, 0, 'h', 0, 'i');
        assertEquals("hi", getInputObject().read_wstring());
    }

    // ---- helpers -------------------------------------------------------------

    /** What the JDK's UTF-16 encoder writes: FE FF, then big endian code units. */
    private static byte[] wstringBytes(String value) {
        return value.getBytes(StandardCharsets.UTF_16);
    }

    private void expectWritten(String value) {
        useV1_2();
        getOutputObject().write_wstring(value);
        expectByteArray(withLength(wstringBytes(value)));
    }

    private static byte[] withLength(byte[] body) {
        byte[] result = new byte[4 + body.length];
        int n = body.length;
        result[0] = (byte) (n >>> 24);
        result[1] = (byte) (n >>> 16);
        result[2] = (byte) (n >>> 8);
        result[3] = (byte) n;
        System.arraycopy(body, 0, result, 4, n);
        return result;
    }

    private static byte[] bytes(int... values) {
        byte[] result = new byte[values.length];
        for (int i = 0; i < values.length; i++) {
            result[i] = (byte) values[i];
        }
        return result;
    }

    /**
     * The wstring's octets as they were written: the first buffer after its
     * GIOP header and the four byte length, the rest after the GIOP header
     * (the test connection writes fragments without a request id).
     */
    private static byte[] payloadOf(List<byte[]> fragments) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (int i = 0; i < fragments.size(); i++) {
            byte[] f = fragments.get(i);
            int skip = Message.GIOPMessageHeaderLength + (i == 0 ? 4 : 0);
            out.write(f, skip, f.length - skip);
        }
        return out.toByteArray();
    }
}
