/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2019 Payara Services Ltd.
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
     * 
     * @param byteBuffer buffer to get request ID of
     */
    public void unmarshalRequestID(ByteBuffer byteBuffer) {
        byteBuffer.order(isLittleEndian() ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN);
        request_id = byteBuffer.getInt(GIOPMessageHeaderLength);
    }

    @Override
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

    @Override
    public RequestId getCorbaRequestId() {
        return new RequestIdImpl(this.request_id);
    }
}
