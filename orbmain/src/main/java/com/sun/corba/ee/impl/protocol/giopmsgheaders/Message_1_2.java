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
import java.nio.ByteOrder;

import com.sun.corba.ee.spi.ior.iiop.GIOPVersion;
import com.sun.corba.ee.spi.protocol.RequestId;

import com.sun.corba.ee.spi.misc.ORBConstants;
import com.sun.corba.ee.impl.protocol.RequestIdImpl;

public class Message_1_2 extends Message_1_1 {
    protected int request_id = (int) 0;

    Message_1_2() {
    }

    Message_1_2(int _magic, GIOPVersion _GIOP_version, byte _flags, byte _message_type, int _message_size) {

        super(_magic, _GIOP_version, _flags, _message_type, _message_size);
    }

    /**
     * The byteBuffer is presumed to have contents of the message already read in. It must have 12 bytes of space at the
     * beginning for the GIOP header, but the header doesn't have to be copied in.
     */
    public void unmarshalRequestID(ByteBuffer byteBuffer) {
        byteBuffer.order(isLittleEndian() ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN);
        request_id = byteBuffer.getInt(GIOPMessageHeaderLength);
    }

    public void write(org.omg.CORBA.portable.OutputStream ostream) {
        if (getEncodingVersion() == ORBConstants.CDR_ENC_VERSION) {
            super.write(ostream);
            return;
        }
        GIOPVersion gv = GIOP_version; // save
        GIOP_version = GIOPVersion.getInstance(GIOPVersion.V13_XX.getMajor(), getEncodingVersion());
        super.write(ostream);
        GIOP_version = gv; // restore
    }

    public RequestId getCorbaRequestId() {
        return new RequestIdImpl(this.request_id);
    }
}
