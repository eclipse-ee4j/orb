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

package com.sun.corba.ee.spi.legacy.connection;

import com.sun.corba.ee.spi.transport.SocketInfo;

/**
 * This exception is raised by <code>ORBSocketFactory.createSocket</code>. It informs the ORB that it should call
 * <code>ORBSocketFactory.getEndPointInfo</code> again with the given <code>socketInfo</code> object as an argument
 * (i.e., a cookie).
 *
 */

public class GetEndPointInfoAgainException extends Exception {
    private SocketInfo socketInfo;

    public GetEndPointInfoAgainException(SocketInfo socketInfo) {
        this.socketInfo = socketInfo;
    }

    public SocketInfo getEndPointInfo() {
        return socketInfo;
    }
}
