/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.glassfish.corba.testutils;

import java.util.List;

public class HexBuffer {
    public static final int BYTES_PER_LINE = 32;
    public static final int SPACES_PER_LINE = 2 * BYTES_PER_LINE + BYTES_PER_LINE / 4;

    public static void dumpBuffer(byte[] bytes) {
        for (int i = 0; i < bytes.length; i += BYTES_PER_LINE)
            dumpLine(i, subBuffer(bytes, i, i + BYTES_PER_LINE));
    }

    private static void dumpLine(int start, byte[] bytes) {
        StringBuilder sb = new StringBuilder(String.format("%4d: ", start));
        int width = 0;
        for (int i = 0; i < bytes.length;) {
            sb.append(String.format("%02X", bytes[i]));
            width += 2;
            if ((++i % 4) == 0) {
                sb.append(' ');
                width++;
            }
        }
        while (width++ < SPACES_PER_LINE)
            sb.append(' ');
        sb.append(' ');
        for (byte aByte : bytes) {
            sb.append(aByte < ' ' ? ' ' : (char) aByte);
        }
        System.out.println(sb.toString());
    }

    private static byte[] subBuffer(byte[] input, int start, int limit) {
        int end = Math.min(limit, input.length);
        byte[] result = new byte[end - start];
        System.arraycopy(input, start, result, 0, result.length);
        return result;
    }

    public static void dumpBuffers(List<byte[]> list) {
        for (byte[] buffer : list) {
            dumpBuffer(buffer);
            System.out.println();
        }
    }
}
