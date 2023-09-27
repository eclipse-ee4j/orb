/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates.
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

import com.sun.corba.ee.spi.ior.iiop.GIOPVersion;

public class CDROutputStream_1_1 extends CDROutputStream_1_0 {
    // This is used to keep indirections working across fragments. When added
    // to the current bbwi.position(), the result is the current position
    // in the byte stream without any fragment headers.
    //
    // It is equal to the following:
    //
    // n = number of buffers (0 is original buffer, 1 is first fragment, etc)
    //
    // n == 0, fragmentOffset = 0
    //
    // n > 0, fragmentOffset
    // = sum i=[1,n] { bbwi_i-1_.size - buffer i header length }
    //
    protected int fragmentOffset = 0;

    @Override
    protected void alignAndReserve(int align, int n) {

        // Notice that in 1.1, we won't end a fragment with
        // alignment padding. We also won't guarantee that
        // our fragments end on evenly divisible 8 byte
        // boundaries. There may be alignment
        // necessary with the header of the next fragment
        // since the header isn't aligned on an 8 byte
        // boundary, so we have to calculate it twice.

        int alignment = computeAlignment(align);

        if (byteBuffer.position() + n + alignment > byteBuffer.limit()) {
            grow(align, n);

            // Must recompute the alignment after a grow.
            // In the case of fragmentation, the alignment
            // calculation may no longer be correct.

            // People shouldn't be able to set their fragment
            // sizes so small that the fragment header plus
            // this alignment fills the entire buffer.
            alignment = computeAlignment(align);
        }

        byteBuffer.position(byteBuffer.position() + alignment);
    }

    @Override
    protected void grow(int align, int n) {
        // Save the current size for possible post-fragmentation calculation
        int oldSize = byteBuffer.position();

        byteBuffer = bufferManagerWrite.overflow(byteBuffer, n);

        // At this point, if we fragmented, we should have a ByteBuffer
        // with the fragment header already marshalled. The size and length fields
        // should be updated accordingly, and the fragmented flag should be set.
        if (bufferManagerWrite.isFragmentOnOverflow()) {

            // Update fragmentOffset so indirections work properly.
            // At this point, oldSize is the entire length of the
            // previous buffer. bbwi.position() is the length of the
            // fragment header of this buffer.
            fragmentOffset += (oldSize - byteBuffer.position());
        }
    }

    @Override
    public int get_offset() {
        return byteBuffer.position() + fragmentOffset;
    }

    @Override
    public GIOPVersion getGIOPVersion() {
        return GIOPVersion.V1_1;
    }

    @Override
    public void write_wchar(char x) {
        // In GIOP 1.1, interoperability with wchar is limited
        // to 2 byte fixed width encodings. CORBA formal 99-10-07 15.3.1.6.
        // Note that the following code prohibits UTF-16 with a byte
        // order marker (which would result in 4 bytes).
        CodeSetConversion.CTBConverter converter = getWCharConverter();

        converter.convert(x);

        if (converter.getNumBytes() != 2)
            throw wrapper.badGiop11Ctb();

        alignAndReserve(converter.getAlignment(), converter.getNumBytes());

        parent.write_octet_array(converter.getBytes(), 0, converter.getNumBytes());
    }

    @Override
    public void write_wstring(String value) {
        if (value == null) {
            throw wrapper.nullParam();
        }

        // The length is the number of code points (which are 2 bytes each)
        // including the 2 byte null. See CORBA formal 99-10-07 15.3.2.7.

        int len = value.length() + 1;

        write_long(len);

        CodeSetConversion.CTBConverter converter = getWCharConverter();

        converter.convert(value);

        internalWriteOctetArray(converter.getBytes(), 0, converter.getNumBytes());

        // Write the 2 byte null ending
        write_short((short) 0);
    }
}
