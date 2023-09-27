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

package com.sun.corba.ee.impl.protocol.giopmsgheaders;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.sun.corba.ee.spi.ior.iiop.GIOPVersion;
import com.sun.corba.ee.spi.protocol.RequestId;

/**
 * This is the base interface for different message type interfaces.
 *
 * @author Ram Jeyaraman 05/14/2000
 * @version 1.0
 */

public interface Message {

    // Generic constants

    static final int defaultBufferSize = 1024;
    static final int GIOPBigMagic = 0x47494F50;
    static final int GIOPMessageHeaderLength = 12;

    // Other useful constants

    static final byte LITTLE_ENDIAN_BIT = 0x01;
    static final byte MORE_FRAGMENTS_BIT = 0x02;
    static final byte FLAG_NO_FRAG_BIG_ENDIAN = 0x00;
    static final byte TRAILING_TWO_BIT_BYTE_MASK = 0x3;
    static final byte THREAD_POOL_TO_USE_MASK = 0x3F;

    // Message types

    static final byte GIOPRequest = 0;
    static final byte GIOPReply = 1;
    static final byte GIOPCancelRequest = 2;
    static final byte GIOPLocateRequest = 3;
    static final byte GIOPLocateReply = 4;
    static final byte GIOPCloseConnection = 5;
    static final byte GIOPMessageError = 6;
    static final byte GIOPFragment = 7; // 1.1 & 1.2:

    /**
     * Returns whether the Message supports message fragmenting.
     *
     * @return <code>true</code> if Message supports fragmenting or is a message fragment. Otherwise <code>false</code> it
     * does not support message fragments.
     */
    boolean supportsFragments();

    // Accessor methods

    GIOPVersion getGIOPVersion();

    byte getEncodingVersion();

    boolean isLittleEndian();

    boolean moreFragmentsToFollow();

    int getType();

    int getSize();

    int getThreadPoolToUse();

    // Mutator methods

    void read(org.omg.CORBA.portable.InputStream istream);

    void write(org.omg.CORBA.portable.OutputStream ostream);

    void setSize(ByteBuffer byteBuffer, int size);

    FragmentMessage createFragmentMessage();

    void callback(MessageHandler handler) throws IOException;

    void setEncodingVersion(byte version);

    /**
     * Return a Message's CorbaRequestId. Messages which do not support a request id in the 4 bytes following the 12 byte
     * GIOP message header shall return an undefined CorbaRequestId.
     * 
     * @return a Message's CorbaRequestId.
     */
    RequestId getCorbaRequestId();
}
