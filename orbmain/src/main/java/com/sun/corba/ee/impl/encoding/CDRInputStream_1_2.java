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
import com.sun.corba.ee.spi.trace.CdrRead;

@CdrRead
public class CDRInputStream_1_2 extends CDRInputStream_1_1 {
    // Indicates whether the header is padded. In GIOP 1.2 and above,
    // the body must be aligned on an 8-octet boundary, and so the header is
    // padded appropriately. However, if there is no body to a request or reply
    // message, there is no header padding, in the unfragmented case.
    protected boolean headerPadding;

    // used to remember headerPadding flag when mark() and restore() are used.
    protected boolean restoreHeaderPadding;

    // Called by RequestMessage_1_2 or ReplyMessage_1_2 classes only.
    @Override
    void setHeaderPadding(boolean headerPadding) {
        this.headerPadding = headerPadding;
    }

    // the mark and reset methods have been overridden to remember the
    // headerPadding flag.

    @Override
    public void mark(int readlimit) {
        super.mark(readlimit);
        restoreHeaderPadding = headerPadding;
    }

    @Override
    public void reset() {
        super.reset();
        headerPadding = restoreHeaderPadding;
        restoreHeaderPadding = false;
    }

    // Template method
    // This method has been overriden to ensure that the duplicated stream
    // inherits the headerPadding flag, in case of GIOP 1.2 and above, streams.
    @Override
    public CDRInputStreamBase dup() {
        CDRInputStreamBase result = super.dup();
        ((CDRInputStream_1_2) result).headerPadding = this.headerPadding;
        return result;
    }

    @CdrRead
    @Override
    protected void alignAndCheck(int align, int n) {
        // headerPadding bit is set by read method of the RequestMessage_1_2
        // or ReplyMessage_1_2 classes. When set, the very first body read
        // operation (from the stub code) would trigger an alignAndCheck
        // method call, that would in turn skip the header padding that was
        // inserted during the earlier write operation by the sender. The
        // padding ensures that the body is aligned on an 8-octet boundary,
        // for GIOP versions 1.2 and beyond.
        if (headerPadding == true) {
            headerPadding = false;
            alignOnBoundary(ORBConstants.GIOP_12_MSG_BODY_ALIGNMENT);
        }

        checkBlockLength(align, n);

        // WARNING: Must compute real alignment after calling
        // checkBlockLength since it may move the position

        // In GIOP 1.2, a fragment may end with some alignment
        // padding (which leads to all fragments ending perfectly
        // on evenly divisible 8 byte boundaries). A new fragment
        // never requires alignment with the header since it ends
        // on an 8 byte boundary.
        // NOTE: Change underlying ByteBuffer's position only if
        // alignIncr is less than or equal to underlying
        // ByteBuffer's limit.
        int savedPosition = byteBuffer.position();
        int alignIncr = computeAlignment(savedPosition, align);
        int bytesNeeded = alignIncr + n;
        if (savedPosition + alignIncr <= byteBuffer.limit()) {
            byteBuffer.position(savedPosition + alignIncr);
        }

        if (savedPosition + bytesNeeded > byteBuffer.limit()) {
            grow(1, n);
        }
    }

    @Override
    public GIOPVersion getGIOPVersion() {
        return GIOPVersion.V1_2;
    }

    @Override
    public char read_wchar() {
        // In GIOP 1.2, a wchar is encoded as an unsigned octet length
        // followed by the octets of the converted wchar.
        int numBytes = read_octet();

        char[] result = getConvertedChars(numBytes, getWCharConverter());

        // Did the provided bytes convert to more than one
        // character? This may come up as more unicode values are
        // assigned, and a single 16 bit Java char isn't enough.
        // Better to use strings for i18n purposes.
        if (getWCharConverter().getNumChars() > 1)
            throw wrapper.btcResultMoreThanOneChar();

        return result[0];
    }

    @Override
    public String read_wstring() {
        // In GIOP 1.2, wstrings are not terminated by a null. The
        // length is the number of octets in the converted format.
        // A zero length string is represented with the 4 byte length
        // value of 0.

        int len = read_long();

        if (len == 0)
            return newEmptyString();

        checkForNegativeLength(len);

        return new String(getConvertedChars(len, getWCharConverter()), 0, getWCharConverter().getNumChars());
    }
}
