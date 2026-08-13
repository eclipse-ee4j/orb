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

import com.sun.corba.ee.spi.ior.iiop.GIOPVersion;
import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.orb.ObjectKeyCacheEntry;

/**
 * This implements the GIOP 1.2 LocateRequest header.
 *
 * @author Ram Jeyaraman 05/14/2000
 * @version 1.0
 */

public final class LocateRequestMessage_1_2 extends Message_1_2 implements LocateRequestMessage {

    // Instance variables

    private ORB orb = null;
    private ObjectKeyCacheEntry entry = null;
    private TargetAddress target = null;

    // Constructors

    LocateRequestMessage_1_2(ORB orb) {
        this.orb = orb;
    }

    LocateRequestMessage_1_2(ORB orb, int _request_id, TargetAddress _target) {
        super(Message.GIOPBigMagic, GIOPVersion.V1_2, FLAG_NO_FRAG_BIG_ENDIAN, Message.GIOPLocateRequest, 0);
        this.orb = orb;
        request_id = _request_id;
        target = _target;
    }

    // Accessor methods (LocateRequestMessage interface)

    public int getRequestId() {
        return this.request_id;
    }

    public ObjectKeyCacheEntry getObjectKeyCacheEntry() {
        if (this.entry == null) {
            // this will raise a MARSHAL exception upon errors.
            this.entry = MessageBase.extractObjectKeyCacheEntry(target, orb);
        }

        return this.entry;
    }

    // IO methods

    public void read(org.omg.CORBA.portable.InputStream istream) {
        super.read(istream);
        this.request_id = istream.read_ulong();
        this.target = TargetAddressHelper.read(istream);
        getObjectKeyCacheEntry(); // this does AddressingDisposition check
    }

    public void write(org.omg.CORBA.portable.OutputStream ostream) {
        super.write(ostream);
        ostream.write_ulong(this.request_id);
        nullCheck(this.target);
        TargetAddressHelper.write(ostream, this.target);
    }

    public void callback(MessageHandler handler) throws java.io.IOException {
        handler.handleInput(this);
    }

    @Override
    public boolean supportsFragments() {
        return true;
    }
} // class LocateRequestMessage_1_2
