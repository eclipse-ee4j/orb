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

package com.sun.corba.ee.impl.transport;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

public class SocketChannelReaderTest extends TransportTestBase {

    private static final byte[] DATA_TO_BE_READ = new byte[]{0, 1, 2, 3, 4, 5, 6};
    private SocketChannelReader reader;

    @Before
    public void setUpReaderTest() {
        reader = new SocketChannelReader(getOrb());
    }

    @Test
    public void whenCurrentBufferNull_allocateBufferAndRead() throws IOException {
        enqueData(DATA_TO_BE_READ);
        ByteBuffer buffer = reader.read(getSocketChannel(), null, 0);
        assertBufferContents(buffer, DATA_TO_BE_READ);
    }

    private void enqueData(byte[] dataToBeRead) {
        getSocketChannel().enqueData(dataToBeRead);
    }

    @Test
    public void whenCurrentBufferHasPartialData_readToAppendData() throws IOException {
        ByteBuffer oldBuffer = ByteBuffer.allocate(100);
        populateBuffer(oldBuffer, DATA_TO_BE_READ, 0, 3);
        enqueData(DATA_TO_BE_READ, 3, DATA_TO_BE_READ.length - 3);
        ByteBuffer buffer = reader.read(getSocketChannel(), oldBuffer, 0);
        assertBufferContents(buffer, DATA_TO_BE_READ);
    }

    private void populateBuffer(ByteBuffer buffer, byte[] bytes, int offset, int length) {
        buffer.put(bytes, offset, length);
        buffer.flip();
    }

    private void enqueData(byte[] dataToBeRead, int offset, int length) {
        enqueData(getSubarray(dataToBeRead, offset, length));
    }

    private byte[] getSubarray(byte[] dataToBeRead, int offset, int length) {
        byte[] data = new byte[Math.min(length, dataToBeRead.length-offset)];
        System.arraycopy(dataToBeRead, offset, data, 0, data.length);
        return data;
    }

    private void assertBufferContents(ByteBuffer buffer, byte... bytes) {
        buffer.flip();
        assertPopulatedBufferContents(buffer, bytes);
    }

    private void assertPopulatedBufferContents(ByteBuffer buffer, byte[] bytes) {
        byte[] actual = new byte[buffer.limit()];
        buffer.get(actual);
        assertEqualData(bytes, actual);
    }

    private void assertEqualData( byte[] expected, byte[] actual) {
        if (!Arrays.equals(expected, actual))
            fail( "expected " + Arrays.toString(expected) + " but was " + Arrays.toString(actual));
    }

    @Test
    public void whenCurrentBufferIsFull_readToAppendData() throws IOException {
        ByteBuffer oldBuffer = ByteBuffer.allocate(3);
        populateBuffer(oldBuffer, DATA_TO_BE_READ, 0, 3);
        enqueData(DATA_TO_BE_READ, 3, DATA_TO_BE_READ.length - 3);
        ByteBuffer buffer = reader.read(getSocketChannel(), oldBuffer, 0);
        assertBufferContents(buffer, DATA_TO_BE_READ);
    }

    @Test
    public void whenCurrentBufferTooSmallForIncomingData_reallocateAndAppend() throws IOException {
        ByteBuffer oldBuffer = ByteBuffer.allocate(5);
        populateBuffer(oldBuffer, DATA_TO_BE_READ, 0, 3);
        enqueData(DATA_TO_BE_READ, 3, DATA_TO_BE_READ.length - 3);
        ByteBuffer buffer = reader.read(getSocketChannel(), oldBuffer, DATA_TO_BE_READ.length);
        assertBufferContents(buffer, DATA_TO_BE_READ);
    }

    @Test
    public void whenMoreDataAvailableThanNeeded_ignoreIt() throws IOException {
        ByteBuffer oldBuffer = ByteBuffer.allocate(10);
        oldBuffer.flip();
        enqueData(DATA_TO_BE_READ);
        getSocketChannel().setNumBytesToRead(3, 3);
        ByteBuffer buffer = reader.read(getSocketChannel(), oldBuffer, 2);
        assertBufferContents(buffer, getSubarray(DATA_TO_BE_READ, 0, 3));
    }

    @Test(expected = EOFException.class)
    public void whenEOFDetectedThrowException() throws IOException {
        getSocketChannel().setEndOfInput();
        ByteBuffer oldBuffer = ByteBuffer.allocate(5);
        reader.read(getSocketChannel(), oldBuffer, 0);
    }

    @Test
    public void whenNoDataRemains_returnNull() throws IOException {
        ByteBuffer oldBuffer = ByteBuffer.allocate(10);
        populateBuffer(oldBuffer, DATA_TO_BE_READ, 0, DATA_TO_BE_READ.length);
        ByteBuffer buffer = reader.read(getSocketChannel(), oldBuffer, 10);
        assertNull(buffer);
        assertPopulatedBufferContents(oldBuffer, DATA_TO_BE_READ);
    }

    @Test
    public void whenAtCapacityAndNoDataRemains_returnNullAndPreserveOldBuffer() throws IOException {
        ByteBuffer oldBuffer = ByteBuffer.wrap(DATA_TO_BE_READ);
        ByteBuffer buffer = reader.read(getSocketChannel(), oldBuffer, 10);
        assertNull(buffer);
        assertPopulatedBufferContents(oldBuffer, DATA_TO_BE_READ);
    }
}
