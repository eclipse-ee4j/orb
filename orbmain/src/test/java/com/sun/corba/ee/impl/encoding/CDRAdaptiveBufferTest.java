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

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

/**
 * A stream may now start with a buffer smaller than a fragment and grow it.
 * That must not be observable: for any starting size, the fragments sent
 * must be byte for byte those sent when the buffer is a whole fragment from
 * the start, including values whose chunks cross fragment boundaries.
 */
public class CDRAdaptiveBufferTest {

    /** A fresh ORB, connection and output stream per call. */
    static class Fixture extends EncodingTestBase {
        List<byte[]> write(int fragmentSize, int bufferSize) throws Exception {
            setUp();
            useV1_2();
            setFragmentSize(fragmentSize);
            setBufferSize(bufferSize);
            CDROutputObject out = getOutputObject();
            for (int i = 0; i < 12; i++) {
                out.write_octet((byte) i);
                out.write_long(i * 1_000_003);
                out.write_string("chars " + i);
                out.write_wstring("wide " + "é€".repeat(i * 5));
                out.write_double(i / 3.0);
                out.write_octet_array(new byte[i * 37], 0, i * 37);

                ArrayList<Value1> list = new ArrayList<>();
                for (int k = 0; k < i * 3; k++) {
                    list.add(new Value1((char) ('a' + k % 26), k));
                }
                out.write_value(list);
                out.write_value(new ComplexValue('z', i));
            }
            return finishAndGetFragments();
        }
    }

    @Test
    public void every_starting_size_sends_the_same_fragments() throws Exception {
        for (int fragmentSize : new int[] { 256, 1024, 4096 }) {
            List<byte[]> reference = new Fixture().write(fragmentSize, fragmentSize);
            assertTrue("the workload must fragment", reference.size() > 1);

            for (int bufferSize : new int[] { 32, 64, 200, fragmentSize / 2, fragmentSize - 8 }) {
                List<byte[]> actual = new Fixture().write(fragmentSize, bufferSize);
                String where = "fragment size " + fragmentSize + ", buffer size " + bufferSize;
                assertEquals(where, reference.size(), actual.size());
                for (int i = 0; i < reference.size(); i++) {
                    assertArrayEquals(where + ", fragment " + i, reference.get(i), actual.get(i));
                }
            }
        }
    }

    @Test
    public void a_message_that_fits_is_one_fragment_whatever_the_starting_size() throws Exception {
        Fixture small = new Fixture();
        small.setUp();
        small.useV1_2();
        small.setFragmentSize(1 << 16);
        small.setBufferSize(64);
        small.getOutputObject().write_octet_array(new byte[10_000], 0, 10_000);
        assertEquals(1, small.finishAndGetFragments().size());
    }
}
