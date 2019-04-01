/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.protocol;

import com.sun.corba.ee.impl.protocol.giopmsgheaders.KeyAddr;

/**
 * This exception is thrown while reading GIOP 1.2 Request, LocateRequest to indicate that a TargetAddress disposition
 * is unacceptable. If this exception is caught explicitly, this need to be rethrown. This is eventually handled within
 * RequestPRocessor and an appropriate reply is sent back to the client.
 *
 * GIOP 1.2 allows three dispositions : KeyAddr (ObjectKey), ProfileAddr (ior profile), IORAddressingInfo (IOR). If the
 * ORB does not support the disposition contained in the GIOP Request / LocateRequest 1.2 message, then it sends a Reply
 * / LocateReply indicating the correct disposition, which the client ORB shall use to transparently retry the request
 * with the correct disposition.
 *
 */
public class AddressingDispositionException extends RuntimeException {

    private short expectedAddrDisp = KeyAddr.value;

    public AddressingDispositionException(short expectedAddrDisp) {
        this.expectedAddrDisp = expectedAddrDisp;
    }

    public short expectedAddrDisp() {
        return this.expectedAddrDisp;
    }
}
