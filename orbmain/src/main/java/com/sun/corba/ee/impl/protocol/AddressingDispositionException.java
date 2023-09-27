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
