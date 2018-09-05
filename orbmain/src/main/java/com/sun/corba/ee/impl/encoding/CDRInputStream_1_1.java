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

public class CDRInputStream_1_1 extends CDRInputStream_1_0
{
    // See notes in CDROutputStream
    protected int fragmentOffset = 0;

    @Override
    public GIOPVersion getGIOPVersion() {
        return GIOPVersion.V1_1;
    }

    // Template method
    @Override
    public CDRInputStreamBase dup() {
        CDRInputStreamBase result = super.dup();

        ((CDRInputStream_1_1)result).fragmentOffset = this.fragmentOffset;

        return result;
    }

    @Override
    protected int get_offset() {
        return byteBuffer.position() + fragmentOffset;
    }

    @Override
    protected void alignAndCheck(int align, int n) {


        checkBlockLength(align, n);

        // WARNING: Must compute real alignment after calling
        // checkBlockLength since it may move the position
        int alignment = computeAlignment(byteBuffer.position(), align);

        if (byteBuffer.position() + n + alignment  > byteBuffer.limit()) {

            // Some other ORBs may have found a way to send 1.1
            // fragments which put alignment bytes at the end
            // of a fragment
            if (byteBuffer.position() + alignment == byteBuffer.limit())
            {
                byteBuffer.position(byteBuffer.position() + alignment);
            }

            grow(align, n);

            // We must recalculate the alignment after a possible
            // fragmentation since the new bbwi.position() (after the header)
            // may require a different alignment.

            alignment = computeAlignment(byteBuffer.position(), align);
        }

        byteBuffer.position(byteBuffer.position() + alignment);
    }

    //
    // This can be overridden....
    //
    @Override
    protected void grow(int align, int n) {

        // Save the size of the current buffer for
        // possible fragmentOffset calculation
        int oldSize = byteBuffer.position();

        byteBuffer = bufferManagerRead.underflow(byteBuffer);

        if (bufferManagerRead.isFragmentOnUnderflow()) {
            
            // By this point we should be guaranteed to have
            // a new fragment whose header has already been
            // unmarshalled.  bbwi.position() should point to the
            // end of the header.
            fragmentOffset += (oldSize - byteBuffer.position());

            markAndResetHandler.fragmentationOccured(byteBuffer);
        }
    }

    // Mark/reset ---------------------------------------

    private class FragmentableStreamMemento extends StreamMemento
    {
        private int fragmentOffset_;

        public FragmentableStreamMemento()
        {
            super();

            fragmentOffset_ = fragmentOffset;
        }
    }

    @Override
    public java.lang.Object createStreamMemento() {
        return new FragmentableStreamMemento();
    }

    @Override
    public void restoreInternalState(java.lang.Object streamMemento) 
    {
        super.restoreInternalState(streamMemento);

        fragmentOffset 
            = ((FragmentableStreamMemento)streamMemento).fragmentOffset_;
    }

    // --------------------------------------------------

    @Override
    public char read_wchar() {
        // In GIOP 1.1, interoperability with wchar is limited
        // to 2 byte fixed width encodings.  CORBA formal 99-10-07 15.3.1.6.
        // WARNING:  For UTF-16, this means that there can be no
        // byte order marker, so it must default to big endian!
        alignAndCheck(2, 2);

        // Because of the alignAndCheck, we should be guaranteed
        // 2 bytes of real data.
        char[] result = getConvertedChars(2, getWCharConverter());

        // Did the provided bytes convert to more than one
        // character?  This may come up as more unicode values are
        // assigned, and a single 16 bit Java char isn't enough.
        // Better to use strings for i18n purposes.
        if (getWCharConverter().getNumChars() > 1)
            throw wrapper.btcResultMoreThanOneChar() ;

        return result[0];
    }

    @Override
    public String read_wstring() {
        // In GIOP 1.1, interoperability with wchar is limited
        // to 2 byte fixed width encodings.  CORBA formal 99-10-07 15.3.1.6.
        int len = read_long();

        // Workaround for ORBs which send string lengths of
        // zero to mean empty string.
        //
        if (len == 0)
            return newEmptyString();

        checkForNegativeLength(len);

        // Don't include the two byte null for the
        // following computations.  Remember that since we're limited
        // to a 2 byte fixed width code set, the "length" was the
        // number of such 2 byte code points plus a 2 byte null.
        len = len - 1;

        char[] result = getConvertedChars(len * 2, getWCharConverter());

        // Skip over the 2 byte null
        read_short();

        return new String(result, 0, getWCharConverter().getNumChars());
    }

}
