/*
 * Copyright (c) 2018, 2020, Oracle and/or its affiliates.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.encoding;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Stack;

import com.sun.corba.ee.impl.protocol.giopmsgheaders.Message;
import org.glassfish.corba.testutils.HexBuffer;

public class ValueTestBase extends EncodingTestBase {
    protected static final int USE_CODEBASE = 0x01;
    protected static final int ONE_REPID_ID = 0x02;
    protected static final int USE_CHUNKING = 0x08;
    private static final int BASE_VALUE_TAG = 0x7fffff00;

    private DataByteOutputStream out = new DataByteOutputStream();
    private final Stack<DataByteOutputStream> chunkStack = new Stack<DataByteOutputStream>();

    protected void writeValueTag(int flags) throws IOException {
        writeInt(BASE_VALUE_TAG | flags);
    }

    protected byte[] getGeneratedBody() {
        return out.toByteArray();
    }

    protected void writeByte(int aByte) throws IOException {
        out.write(aByte);
    }

    protected void startCustomMarshalingFormat(boolean defaultWriteObjectCalled) throws IOException {
        out.write(getFormatVersion());
        out.write(defaultWriteObjectCalled ? 1 : 0);
    }

    protected int getCurrentLocation() {
        return out.pos();
    }

    protected void writeCodebase(String location) throws IOException {
        writeString(location);
    }

    protected void writeNull() throws IOException {
        writeInt(0);
    }

    protected void dumpBuffer() {
        HexBuffer.dumpBuffer(getGeneratedBody());
    }

    protected void writeWchar_1_0(char aChar) throws IOException {
        out.write((aChar >> 8));
        out.write(aChar);
    }

    protected void writeWchar_1_1(char aChar) throws IOException {
        align(2);
        out.write((aChar >> 8));
        out.write(aChar);
    }

    protected void writeWchar_1_2(char aChar) throws IOException {
        out.write(4);
        writeBigEndianMarker();
        out.write((aChar >> 8));
        out.write(aChar);
    }

    protected void writeEndTag(int chunkLevel) throws IOException {
        writeInt(chunkLevel);
    }

    /** When starting a new chunk, align and reserve space for the chunk length. **/
    protected void startChunk() throws IOException {
        align(4);
        chunkStack.push(out);
        out = new DataByteOutputStream(out.pos() + 4);
    }

    protected void endChunk() throws IOException {
        byte[] chunkData = out.toByteArray();
        out = chunkStack.pop();
        writeInt(chunkData.length);
        out.write(chunkData);
    }

    protected void writeStringValue_1_2(String value) throws IOException {
        writeInt(2 + 2*value.length());
        writeBigEndianMarker();
        for (char aChar : value.toCharArray()) {
            out.write(0);
            out.write(aChar);
        }
    }

    private void writeBigEndianMarker() throws IOException {
        out.write(FE);
        out.write(FF);
    }

    protected void writeRepId(String id) throws IOException {
        writeString(id);
    }

    // Rep ID to define optional data in serial version 2 jidl ptc 03-01-17 1.4.10
    protected void writeCustomRepId(String id) throws IOException {
        writeString("org.omg.custom." + id);
    }

    protected void writeString(String string) throws IOException {
        writeInt(string.length() + 1);
        for (char aChar : string.toCharArray())
            out.write(aChar);
        out.write(0);
    }

    protected void writeShort(short value) throws IOException {
        align(2);
        out.writeShort(value);
    }

    protected void writeInt(int value) throws IOException {
        align(4);
        out.writeInt(value);
    }

    protected void writeLong(long value) throws IOException {
        align(8);
        out.writeLong(value);
    }

    protected void writeFloat(float value) throws IOException {
        align(4);
        out.writeFloat(value);
    }

    protected void writeDouble(double value) throws IOException {
        align(8);
        out.writeDouble(value);
    }

    private void align(int size) throws IOException {
        while ((out.pos() % size) != 0)
            out.write(0);
    }

    protected void writeIndirectionTo(int location) throws IOException {
        writeInt(-1);
        writeInt(location - out.pos());
    }

    static class DataByteOutputStream extends DataOutputStream {
        private int streamStart;

        DataByteOutputStream() {
            this(Message.GIOPMessageHeaderLength);
        }

        DataByteOutputStream(int streamStart) {
            super(new ByteArrayOutputStream());
            this.streamStart = streamStart;
        }

        private byte[] toByteArray() {
            return ((ByteArrayOutputStream) out).toByteArray();
        }

        private int pos() {
            return streamStart + size();
        }
    }
}
