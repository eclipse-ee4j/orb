/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.encoding;

import com.sun.corba.ee.spi.ior.iiop.GIOPVersion;
import com.sun.corba.ee.spi.misc.ORBConstants;
import com.sun.corba.ee.spi.trace.CdrWrite;
import org.glassfish.pfl.tf.spi.annotation.InfoMethod;

@CdrWrite
public class CDROutputStream_1_2 extends CDROutputStream_1_1 {
    // There's a situation with chunking with fragmentation
    // in which the alignment for a primitive value is needed
    // to fill fragment N, but the primitive won't fit so
    // must go into fragment N + 1. The behavior is the same
    // as that for specialChunks.
    //
    // Unfortunately, given the current code, we can't reuse
    // specialChunk. If you wrap each of the following
    // write calls with handleSpecialChunkBegin/End, you
    // will lose your state because the primitive calls will
    // change the variables, etc.
    //
    // All of the CDR code should be rewritten moving chunking
    // to a different level, perhaps in the buffer managers.
    // We want to move to a compositional model rather than
    // using inheritance.
    //
    // Note that in the grow case, chunks are _NOT_ closed
    // at grow points, now.
    //
    // **** NOTE ****
    // Since we will not support valuetypes with GIOP 1.1, that
    // also means we do not support chunking there.
    //
    protected boolean primitiveAcrossFragmentedChunk = false;

    // Used in chunking. Here's how this works:
    //
    // When chunking and writing an array of primitives, a string, or a
    // wstring, _AND_ it won't fit in the buffer do the following. (As
    // you can see, this is a very "special" chunk.)
    //
    // 1. Write the length of the chunk including the array length
    // 2. Set specialChunk to true
    // 3 applies to ALL chunking:
    // 3. In grow, if we need to fragment and specialChunk is false
    // a) call end_block
    // b) fragment
    // Now back to the array only case:
    // [write the data]
    // 4. if specialChunk is true
    // a) Close the chunk
    // b) Set specialChunk to false

    protected boolean specialChunk = false;

    // Indicates whether the header should be padded. In GIOP 1.2 and above, the
    // body must be aligned on a 8-octet boundary, and so the header needs to be
    // padded appropriately. However, if there is no body to a request or reply
    // message, there is no need to pad the header, in the unfragmented case.
    private boolean headerPadding;

    @InfoMethod
    private void specialChunkCase() {
    }

    @CdrWrite
    @Override
    protected void handleSpecialChunkBegin(int requiredSize) {
        // If we're chunking and the item won't fit in the buffer
        if (inBlock && requiredSize + byteBuffer.position() > byteBuffer.limit()) {
            specialChunkCase();

            // Duplicating some code from end_block. Compute
            // and write the total chunk length.

            int oldSize = byteBuffer.position();
            byteBuffer.position(blockSizeIndex - 4);

            // write_long(oldSize - blockSizeIndex);
            writeLongWithoutAlign((oldSize - blockSizeIndex) + requiredSize);
            byteBuffer.position(oldSize);

            // Set the special flag so we don't end the chunk when
            // we fragment
            specialChunk = true;
        }
    }

    @CdrWrite
    @Override
    protected void handleSpecialChunkEnd() {
        // If we're in a chunk and the item spanned fragments
        if (inBlock && specialChunk) {
            specialChunkCase();

            // This is unnecessary, but I just want to show that
            // we're done with the current chunk. (the end_block
            // call is inappropriate here)
            inBlock = false;
            blockSizeIndex = -1;
            blockSizePosition = -1;

            // Start a new chunk since we fragmented during the item.
            // Thus, no one can go back to add more to the chunk length
            start_block();

            // Now turn off the flag so we go back to the normal
            // behavior of closing a chunk when we fragment and
            // reopening afterwards.
            specialChunk = false;
        }
    }

    // Called after writing primitives
    @CdrWrite
    private void checkPrimitiveAcrossFragmentedChunk() {
        if (primitiveAcrossFragmentedChunk) {
            primitiveAcrossFragmentedChunk = false;

            inBlock = false;

            // It would be nice to have a StreamPosition
            // abstraction if we could avoid allocation
            // overhead.
            blockSizeIndex = -1;
            blockSizePosition = -1;

            // Start a new chunk
            start_block();
        }
    }

    @Override
    public void write_octet(byte x) {
        super.write_octet(x);
        checkPrimitiveAcrossFragmentedChunk();
    }

    @Override
    public void write_short(short x) {
        super.write_short(x);
        checkPrimitiveAcrossFragmentedChunk();
    }

    @Override
    public void write_long(int x) {
        super.write_long(x);
        checkPrimitiveAcrossFragmentedChunk();
    }

    @Override
    public void write_longlong(long x) {
        super.write_longlong(x);
        checkPrimitiveAcrossFragmentedChunk();
    }

    // Called by RequestMessage_1_2 or ReplyMessage_1_2 classes only.
    @Override
    void setHeaderPadding(boolean headerPadding) {
        this.headerPadding = headerPadding;
    }

    @Override
    @CdrWrite
    protected void alignAndReserve(int align, int n) {

        // headerPadding bit is set by the write operation of RequestMessage_1_2
        // or ReplyMessage_1_2 classes. When set, the very first body write
        // operation (from the stub code) would trigger an alignAndReserve
        // method call, that would in turn add the appropriate header padding,
        // such that the body is aligned on a 8-octet boundary. The padding
        // is required for GIOP versions 1.2 and above, only if body is present.
        if (headerPadding == true) {
            headerPadding = false;
            alignOnBoundary(ORBConstants.GIOP_12_MSG_BODY_ALIGNMENT);
        }

        // In GIOP 1.2, we always end fragments at our
        // fragment size, which is an "evenly divisible
        // 8 byte boundary" (aka divisible by 16). A fragment can
        // end with appropriate alignment padding, but no padding
        // is needed with respect to the next GIOP fragment
        // header since it ends on an 8 byte boundary.

        byteBuffer.position(byteBuffer.position() + computeAlignment(align));

        if (byteBuffer.position() + n > byteBuffer.limit())
            grow(align, n);
    }

    @InfoMethod
    private void outOfSequenceWrite() {
    }

    @InfoMethod
    private void handlingFragmentCase() {
    }

    @Override
    @CdrWrite
    protected void grow(int align, int n) {

        // Save the current size for possible post-fragmentation calculation
        int oldSize = byteBuffer.position();

        // See notes where specialChunk is defined, as well as the
        // above notes for primitiveAcrossFragmentedChunk.
        //
        // If we're writing a primitive and chunking, we need to update
        // the chunk length to include the length of the primitive (unless
        // this complexity is handled by specialChunk).
        //
        // Note that this is wasted processing in the grow case, but that
        // we don't actually close the chunk in that case.
        boolean handleChunk = (inBlock && !specialChunk);
        if (handleChunk) {
            int oldIndex = byteBuffer.position();

            byteBuffer.position(blockSizeIndex - 4);

            writeLongWithoutAlign((oldIndex - blockSizeIndex) + n);

            byteBuffer.position(oldIndex);
        }

        byteBuffer = bufferManagerWrite.overflow(byteBuffer, n);

        // At this point, if we fragmented, we should have a ByteBuffer
        // with the fragment header already marshaled. The buflen and position
        // should be updated accordingly.
        if (bufferManagerWrite.isFragmentOnOverflow()) {

            // Update fragmentOffset so indirections work properly.
            // At this point, oldSize is the entire length of the
            // previous buffer. bbwi.position() is the length of the
            // fragment header of this buffer.
            fragmentOffset += (oldSize - byteBuffer.position());

            // We just fragmented, and need to signal that we should
            // start a new chunk after writing the primitive.
            if (handleChunk)
                primitiveAcrossFragmentedChunk = true;
        }
    }

    @Override
    public GIOPVersion getGIOPVersion() {
        return GIOPVersion.V1_2;
    }

    @Override
    public void write_wchar(char x) {
        // In GIOP 1.2, a wchar is encoded as an unsigned octet length
        // followed by the octets of the converted wchar. This is good,
        // but it causes problems with our chunking code. We don't
        // want that octet to get put in a different chunk at the end
        // of the previous fragment.
        //
        // Ensure that this won't happen by overriding write_wchar_array
        // and doing our own handleSpecialChunkBegin/End here.
        CodeSetConversion.CTBConverter converter = getWCharConverter();

        converter.convert(x);

        handleSpecialChunkBegin(1 + converter.getNumBytes());

        write_octet((byte) converter.getNumBytes());

        byte[] result = converter.getBytes();

        // Write the bytes without messing with chunking
        // See CDROutputStream_1_0
        internalWriteOctetArray(result, 0, converter.getNumBytes());

        handleSpecialChunkEnd();
    }

    @Override
    public void write_wchar_array(char[] value, int offset, int length) {
        if (value == null) {
            throw wrapper.nullParam();
        }

        CodeSetConversion.CTBConverter converter = getWCharConverter();

        // Unfortunately, because of chunking, we have to convert the
        // entire char[] to a byte[] array first so we can know how
        // many bytes we're writing ahead of time. You can't split
        // an array of primitives into multiple chunks.
        int totalNumBytes = 0;

        // Remember that every wchar starts with an octet telling
        // its length. The buffer size is an upper bound estimate.
        int maxLength = (int) Math.ceil(converter.getMaxBytesPerChar() * length);
        byte[] buffer = new byte[maxLength + length];

        for (int i = 0; i < length; i++) {
            // Convert one wchar
            converter.convert(value[offset + i]);

            // Make sure to add the octet length
            buffer[totalNumBytes++] = (byte) converter.getNumBytes();

            // Copy it into our buffer
            System.arraycopy(converter.getBytes(), 0, buffer, totalNumBytes, converter.getNumBytes());

            totalNumBytes += converter.getNumBytes();
        }

        // Now that we know the total length, we can deal with chunking.
        // Note that we don't have to worry about alignment since they're
        // just octets.
        handleSpecialChunkBegin(totalNumBytes);

        // Must use totalNumBytes rather than buffer.length since the
        // buffer.length is only the upper bound estimate.
        internalWriteOctetArray(buffer, 0, totalNumBytes);

        handleSpecialChunkEnd();
    }

    @Override
    public void write_wstring(String value) {
        if (value == null) {
            throw wrapper.nullParam();
        }

        // In GIOP 1.2, wstrings are not terminated by a null. The
        // length is the number of octets in the converted format.
        // A zero length string is represented with the 4 byte length
        // value of 0.
        if (value.length() == 0) {
            write_long(0);
            return;
        }

        CodeSetConversion.CTBConverter converter = getWCharConverter();

        converter.convert(value);

        handleSpecialChunkBegin(computeAlignment(4) + 4 + converter.getNumBytes());

        write_long(converter.getNumBytes());

        // Write the octet array without tampering with chunking
        internalWriteOctetArray(converter.getBytes(), 0, converter.getNumBytes());

        handleSpecialChunkEnd();
    }
}
