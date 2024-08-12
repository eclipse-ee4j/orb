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

import com.sun.corba.ee.spi.servicecontext.ServiceContextDefaults;
import com.sun.corba.ee.spi.servicecontext.ServiceContexts;
import com.sun.corba.ee.spi.ior.iiop.GIOPVersion;
import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.orb.ObjectKeyCacheEntry;
import com.sun.corba.ee.impl.encoding.CDRInputObject;
import com.sun.corba.ee.impl.encoding.CDROutputObject;
import com.sun.corba.ee.spi.misc.ORBConstants;

import com.sun.corba.ee.spi.trace.Transport;

import com.sun.corba.ee.spi.logging.ORBUtilSystemException;

/**
 * This implements the GIOP 1.2 Request header.
 *
 * @author Ram Jeyaraman 05/14/2000
 * @version 1.0
 */

@Transport
public final class RequestMessage_1_2 extends Message_1_2 implements RequestMessage {

    private static final ORBUtilSystemException wrapper = ORBUtilSystemException.self;

    // Instance variables

    private ORB orb = null;
    private byte response_flags = (byte) 0;
    private byte reserved[] = null;
    private TargetAddress target = null;
    private String operation = null;
    private ServiceContexts service_contexts = null;
    private ObjectKeyCacheEntry entry = null;

    // Constructors

    RequestMessage_1_2(ORB orb) {
        this.orb = orb;
        this.service_contexts = ServiceContextDefaults.makeServiceContexts(orb);
    }

    RequestMessage_1_2(ORB orb, int _request_id, byte _response_flags, byte[] _reserved, TargetAddress _target, String _operation,
            ServiceContexts _service_contexts) {
        super(Message.GIOPBigMagic, GIOPVersion.V1_2, FLAG_NO_FRAG_BIG_ENDIAN, Message.GIOPRequest, 0);
        this.orb = orb;
        request_id = _request_id;
        response_flags = _response_flags;
        reserved = _reserved;
        target = _target;
        operation = _operation;
        service_contexts = _service_contexts;
    }

    // Accessor methods (RequestMessage interface)

    public int getRequestId() {
        return this.request_id;
    }

    public boolean isResponseExpected() {
        /*
         * case 1: LSBit[1] == 1 not a oneway call (DII flag INV_NO_RESPONSE is false) // Ox03 LSBit[0] must be 1. case 2:
         * LSBit[1] == 0 if (LSB[0] == 0) // Ox00 oneway call else if (LSB[0] == 1) // 0x01 oneway call; but server may provide
         * a location forward response or system exception response.
         */

        if ((this.response_flags & RESPONSE_EXPECTED_BIT) == RESPONSE_EXPECTED_BIT) {
            return true;
        }

        return false;
    }

    public byte[] getReserved() {
        return this.reserved;
    }

    public ObjectKeyCacheEntry getObjectKeyCacheEntry() {
        if (this.entry == null) {
            // this will raise a MARSHAL exception upon errors.
            this.entry = MessageBase.extractObjectKeyCacheEntry(target, orb);
        }

        return this.entry;
    }

    public String getOperation() {
        return this.operation;
    }

    @SuppressWarnings({ "deprecation" })
    public org.omg.CORBA.Principal getPrincipal() {
        // REVISIT Should we throw an exception or return null ?
        return null;
    }

    public ServiceContexts getServiceContexts() {
        return this.service_contexts;
    }

    public void setServiceContexts(ServiceContexts sc) {
        this.service_contexts = sc;
    }

    // IO methods

    @Transport
    public void read(org.omg.CORBA.portable.InputStream istream) {
        super.read(istream);
        this.request_id = istream.read_ulong();
        this.response_flags = istream.read_octet();
        this.reserved = new byte[3];
        for (int _o0 = 0; _o0 < (3); ++_o0) {
            this.reserved[_o0] = istream.read_octet();
        }
        this.target = TargetAddressHelper.read(istream);
        getObjectKeyCacheEntry(); // this does AddressingDisposition check
        this.operation = istream.read_string();
        this.service_contexts = ServiceContextDefaults.makeServiceContexts((org.omg.CORBA_2_3.portable.InputStream) istream);

        // CORBA formal 00-11-0 15.4.2.2 GIOP 1.2 body must be
        // aligned on an 8 octet boundary.
        // Ensures that the first read operation called from the stub code,
        // during body deconstruction, would skip the header padding, that was
        // inserted to ensure that the body was aligned on an 8-octet boundary.
        ((CDRInputObject) istream).setHeaderPadding(true);

    }

    @Transport
    public void write(org.omg.CORBA.portable.OutputStream ostream) {
        super.write(ostream);
        ostream.write_ulong(this.request_id);
        ostream.write_octet(this.response_flags);
        nullCheck(this.reserved);
        if (this.reserved.length != (3)) {
            throw wrapper.badReservedLength();
        }
        for (int _i0 = 0; _i0 < (3); ++_i0) {
            ostream.write_octet(this.reserved[_i0]);
        }
        nullCheck(this.target);
        TargetAddressHelper.write(ostream, this.target);
        ostream.write_string(this.operation);
        service_contexts.write((org.omg.CORBA_2_3.portable.OutputStream) ostream, GIOPVersion.V1_2);

        // CORBA formal 00-11-0 15.4.2.2 GIOP 1.2 body must be
        // aligned on an 8 octet boundary.
        // Ensures that the first write operation called from the stub code,
        // during body construction, would insert a header padding, such that
        // the body is aligned on an 8-octet boundary.
        ((CDROutputObject) ostream).setHeaderPadding(true);
    }

    public void callback(MessageHandler handler) throws java.io.IOException {
        handler.handleInput(this);
    }

    @Override
    public boolean supportsFragments() {
        return true;
    }
} // class RequestMessage_1_2
