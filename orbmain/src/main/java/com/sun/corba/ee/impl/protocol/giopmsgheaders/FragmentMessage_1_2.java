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

import com.sun.corba.ee.spi.ior.iiop.GIOPVersion;

/**
 * This implements the GIOP 1.2 Fragment header.
 *
 * @author Ram Jeyaraman 05/14/2000
 * @version 1.0
 */

public final class FragmentMessage_1_2 extends Message_1_2 implements FragmentMessage {

    // Constructors

    FragmentMessage_1_2() {
    }

    // This is currently never called.
    FragmentMessage_1_2(int _request_id) {
        super(Message.GIOPBigMagic, GIOPVersion.V1_2, FLAG_NO_FRAG_BIG_ENDIAN, Message.GIOPFragment, 0);
        this.message_type = GIOPFragment;
        request_id = _request_id;
    }

    FragmentMessage_1_2(Message_1_1 msg12) {
        this.magic = msg12.magic;
        this.GIOP_version = msg12.GIOP_version;
        this.flags = msg12.flags;
        this.message_type = GIOPFragment;
        this.message_size = 0;

        switch (msg12.message_type) {
        case GIOPRequest:
            this.request_id = ((RequestMessage) msg12).getRequestId();
            break;
        case GIOPReply:
            this.request_id = ((ReplyMessage) msg12).getRequestId();
            break;
        case GIOPLocateRequest:
            this.request_id = ((LocateRequestMessage) msg12).getRequestId();
            break;
        case GIOPLocateReply:
            this.request_id = ((LocateReplyMessage) msg12).getRequestId();
            break;
        case GIOPFragment:
            this.request_id = ((FragmentMessage) msg12).getRequestId();
            break;
        }
    }

    // Accessor methods

    public int getRequestId() {
        return this.request_id;
    }

    public int getHeaderLength() {
        return GIOPMessageHeaderLength + 4;
    }

    // IO methods

    /*
     * This will never be called, since we do not currently read the request_id from an CDRInputStream. Instead we use the
     * readGIOP_1_2_requestId to read the requestId from a byte buffer.
     */
    public void read(org.omg.CORBA.portable.InputStream istream) {
        super.read(istream);
        this.request_id = istream.read_ulong();
    }

    public void write(org.omg.CORBA.portable.OutputStream ostream) {
        super.write(ostream);
        ostream.write_ulong(this.request_id);
    }

    public void callback(MessageHandler handler) throws java.io.IOException {
        handler.handleInput(this);
    }

    @Override
    public boolean supportsFragments() {
        return true;
    }
} // class FragmentMessage_1_2
