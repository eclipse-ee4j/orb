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
 * This implements the GIOP 1.1 CancelRequest header.
 *
 * @author Ram Jeyaraman 05/14/2000
 * @version 1.0
 */

public final class CancelRequestMessage_1_1 extends Message_1_1
        implements CancelRequestMessage {

    // Instance variables

    private int request_id = (int) 0;

    // Constructors

    CancelRequestMessage_1_1() {}

    CancelRequestMessage_1_1(int _request_id) {
        super(Message.GIOPBigMagic, GIOPVersion.V1_1, FLAG_NO_FRAG_BIG_ENDIAN,
            Message.GIOPCancelRequest, CANCEL_REQ_MSG_SIZE);
        request_id = _request_id;
    }

    // Accessor methods

    public int getRequestId() {
        return this.request_id;
    }

    // IO methods

    public void read(org.omg.CORBA.portable.InputStream istream) {
        super.read(istream);    
        this.request_id = istream.read_ulong();
    }

    public void write(org.omg.CORBA.portable.OutputStream ostream) {
        super.write(ostream);
        ostream.write_ulong(this.request_id);
    }

    public void callback(MessageHandler handler)
        throws java.io.IOException
    {
        handler.handleInput(this);
    }
} // class CancelRequestMessage_1_1
