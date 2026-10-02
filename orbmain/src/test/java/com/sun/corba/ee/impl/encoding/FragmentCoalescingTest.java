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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.sun.corba.ee.impl.protocol.giopmsgheaders.Message;

import java.io.ByteArrayOutputStream;
import java.util.List;

import org.junit.Test;

/**
 * A streamed message's fragments are held and written in groups of up to {@link BufferManagerWriteStream#HELD_BYTES}
 * instead of one write each. The connection fake splits every write back into messages by their GIOP headers, so the
 * fragments these tests see are the ones on the wire.
 */
public class FragmentCoalescingTest extends EncodingTestBase {

    private static final int FRAGMENT = 1024;

    @Test
    public void many_fragments_go_out_in_writes_of_up_to_sixteen_kilobytes() {
        useFragmentsOf(FRAGMENT);
        byte[] data = bytes(40_000);

        getOutputObject().write_octet_array(data, 0, data.length);
        List<byte[]> fragments = finishAndGetFragments();

        int perWrite = BufferManagerWriteStream.HELD_BYTES / FRAGMENT;
        assertTrue("the message must have been fragmented", fragments.size() > perWrite);
        assertEquals((fragments.size() + perWrite - 1) / perWrite, getWriteCount());
        assertArrayEquals(data, payloadOf(fragments));
    }

    @Test
    public void a_message_of_a_few_fragments_is_one_write() {
        useFragmentsOf(FRAGMENT);
        byte[] data = bytes(2_500);

        getOutputObject().write_octet_array(data, 0, data.length);
        List<byte[]> fragments = finishAndGetFragments();

        assertEquals(3, fragments.size());
        assertEquals(1, getWriteCount());
        assertArrayEquals(data, payloadOf(fragments));
    }

    @Test
    public void a_message_in_one_fragment_is_one_write() {
        useFragmentsOf(FRAGMENT);
        byte[] data = bytes(100);

        getOutputObject().write_octet_array(data, 0, data.length);
        List<byte[]> fragments = finishAndGetFragments();

        assertEquals(1, fragments.size());
        assertEquals(1, getWriteCount());
        assertArrayEquals(data, payloadOf(fragments));
    }

    @Test
    public void fragments_larger_than_half_the_holding_buffer_are_written_one_by_one() {
        useFragmentsOf(BufferManagerWriteStream.HELD_BYTES);
        byte[] data = bytes(100_000);

        getOutputObject().write_octet_array(data, 0, data.length);
        List<byte[]> fragments = finishAndGetFragments();

        assertTrue(fragments.size() > 1);
        assertEquals(fragments.size(), getWriteCount());
        assertArrayEquals(data, payloadOf(fragments));
    }

    @Test
    public void held_fragments_do_not_count_as_sent() {
        useFragmentsOf(FRAGMENT);
        byte[] data = bytes(3_000);

        getOutputObject().write_octet_array(data, 0, data.length);

        assertEquals(0, getWriteCount());
        assertFalse(getOutputObject().getBufferManager().sentFragment());

        byte[] more = bytes(20_000);
        getOutputObject().write_octet_array(more, 0, more.length);

        assertTrue(getWriteCount() > 0);
        assertTrue(getOutputObject().getBufferManager().sentFragment());
    }

    @Test
    public void closing_an_unfinished_message_drops_the_held_fragments() {
        useFragmentsOf(FRAGMENT);
        byte[] data = bytes(3_000);

        getOutputObject().write_octet_array(data, 0, data.length);
        getOutputObject().getBufferManager().close();

        assertEquals(0, getWriteCount());
        assertFalse(getOutputObject().getBufferManager().sentFragment());
    }

    private void useFragmentsOf(int size) {
        useV1_2();
        setFragmentSize(size);
        setBufferSize(size);
    }

    private static byte[] bytes(int length) {
        byte[] data = new byte[length];
        for (int i = 0; i < length; i++) {
            data[i] = (byte) (i * 31 + i / 251);
        }
        return data;
    }

    /** The bytes after each GIOP header, run together. */
    private static byte[] payloadOf(List<byte[]> fragments) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (byte[] f : fragments) {
            out.write(f, Message.GIOPMessageHeaderLength, f.length - Message.GIOPMessageHeaderLength);
        }
        return out.toByteArray();
    }
}
