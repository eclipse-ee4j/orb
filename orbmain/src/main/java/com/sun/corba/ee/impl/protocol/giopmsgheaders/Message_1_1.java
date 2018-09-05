/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.protocol.giopmsgheaders;

import java.nio.ByteBuffer;

import com.sun.corba.ee.spi.ior.iiop.GIOPVersion;

import com.sun.corba.ee.spi.logging.ORBUtilSystemException ;

/*
 * This implements the GIOP 1.1 & 1.2 Message header.
 *
 * @author Ram Jeyaraman 05/14/2000
 * @version 1.0
 */

public class Message_1_1
        extends com.sun.corba.ee.impl.protocol.giopmsgheaders.MessageBase {

    // Constants
    final static int UPPER_THREE_BYTES_OF_INT_MASK = 0xFF;

    private static ORBUtilSystemException wrapper =
        ORBUtilSystemException.self ;

    // Instance variables
    int magic = 0;
    GIOPVersion GIOP_version = null;
    byte flags = 0;
    byte message_type = 0;
    int message_size = 0;

    // Constructor

    Message_1_1() {
    }
    
    Message_1_1(int _magic, GIOPVersion _GIOP_version, byte _flags,
            byte _message_type, int _message_size) {
        magic = _magic;
        GIOP_version = _GIOP_version;
        flags = _flags;
        message_type = _message_type;
        message_size = _message_size;
    }

    // Accessor methods

    public GIOPVersion getGIOPVersion() {
        return this.GIOP_version;
    }

    public int getType() {
        return this.message_type;
    }

    public int getSize() {
            return this.message_size;
    }

    public boolean isLittleEndian() {
        return ((this.flags & LITTLE_ENDIAN_BIT) == LITTLE_ENDIAN_BIT);
    }

    public boolean moreFragmentsToFollow() {
        return ( (this.flags & MORE_FRAGMENTS_BIT) == MORE_FRAGMENTS_BIT );
    }

    // Mutator methods

    // NOTE: This is a SUN PROPRIETARY EXTENSION
    // Add the poolToUse to the upper 6 bits of byte 6 of the GIOP header.
    // this.flags represents byte 6 here.
    public void setThreadPoolToUse(int poolToUse) {
        // IMPORTANT: Bitwise operations will promote
        //            byte types to int before performing
        //            bitwise operations. And, Java
        //            types are signed.
        int tmpFlags = poolToUse << 2;
        tmpFlags &= UPPER_THREE_BYTES_OF_INT_MASK;
        tmpFlags |= flags;
        flags = (byte)tmpFlags;
    }

    public void setSize(ByteBuffer byteBuffer, int size) {

        this.message_size = size;

        //
        // Patch the size field in the header.
        //

        int patch = size - GIOPMessageHeaderLength;
        if (!isLittleEndian()) {
            byteBuffer.put(8,  (byte)((patch >>> 24) & 0xFF));
            byteBuffer.put(9,  (byte)((patch >>> 16) & 0xFF));
            byteBuffer.put(10, (byte)((patch >>> 8)  & 0xFF));
            byteBuffer.put(11, (byte)((patch >>> 0)  & 0xFF));
        } else {
            byteBuffer.put(8,  (byte)((patch >>> 0)  & 0xFF));
            byteBuffer.put(9,  (byte)((patch >>> 8)  & 0xFF));
            byteBuffer.put(10, (byte)((patch >>> 16) & 0xFF));
            byteBuffer.put(11, (byte)((patch >>> 24) & 0xFF));
        }
    }

    /**
     * Allows us to create a fragment message from any message type.
     */
    public FragmentMessage createFragmentMessage() {

        // check for message type validity

        switch (this.message_type) {
        case GIOPCancelRequest :
        case GIOPCloseConnection :
        case GIOPMessageError :
            throw wrapper.fragmentationDisallowed() ;
        case GIOPLocateRequest :
        case GIOPLocateReply :
            if (this.GIOP_version.equals(GIOPVersion.V1_1)) {
                throw wrapper.fragmentationDisallowed() ;
            }
            break;
        }

        /*
        // A fragmented mesg can be created only if the current mesg' fragment
        // bit is set. Otherwise, raise error
        // too stringent check
        if ( (this.flags & MORE_FRAGMENTS_BIT) != MORE_FRAGMENTS_BIT ) {
                throw wrapper.fragmentationDisallowed( CompletionStatus.COMPLETED_MAYBE);
        }
        */
        if (this.GIOP_version.equals(GIOPVersion.V1_1)) {
            return new FragmentMessage_1_1(this);
        } else if (this.GIOP_version.equals(GIOPVersion.V1_2)) {
            return new FragmentMessage_1_2(this);
        }

        throw wrapper.giopVersionError() ;
    }

    // IO methods

    // This should do nothing even if it is called. The Message Header is read
    // off a java.io.InputStream (not a CDRInputStream) by IIOPConnection
    // in order to choose the correct CDR Version , msg_type, and msg_size.
    // So, we would never need to read the Message Header off a CDRInputStream.
    public void read(org.omg.CORBA.portable.InputStream istream) {
        /*
        this.magic = istream.read_long();
        this.GIOP_version = (new GIOPVersion()).read(istream);
        this.flags = istream.read_octet();
        this.message_type = istream.read_octet();
        this.message_size = istream.read_ulong();
        */
    }

    public void write(org.omg.CORBA.portable.OutputStream ostream) {
        ostream.write_long(this.magic);
        nullCheck(this.GIOP_version);
        this.GIOP_version.write(ostream);
        ostream.write_octet(this.flags);
        ostream.write_octet(this.message_type);
        ostream.write_ulong(this.message_size);
    }
} // class Message_1_1
