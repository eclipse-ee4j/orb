/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.legacy.connection;

import com.sun.corba.ee.spi.legacy.connection.GetEndPointInfoAgainException;
import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.transport.ContactInfo;
import com.sun.corba.ee.spi.transport.SocketInfo;

import com.sun.corba.ee.impl.transport.ConnectionImpl;
import com.sun.corba.ee.spi.trace.Transport;
import java.net.Socket;

/**
 * @author Harold Carr
 */
@Transport
public class SocketFactoryConnectionImpl extends ConnectionImpl {
    @Transport
    private void connectionCreated(Socket socket) {
    }

    // Socket-factory client constructor.
    public SocketFactoryConnectionImpl(ORB orb, ContactInfo contactInfo, boolean useSelectThreadToWait, boolean useWorkerThread) {
        super(orb, useSelectThreadToWait, useWorkerThread);

        // REVISIT - probably need a contact info for both
        // client and server for removing connections from cache?
        this.contactInfo = contactInfo;

        SocketInfo socketInfo =
                // REVISIT - case - needs interface method
                ((SocketFactoryContactInfoImpl) contactInfo).socketInfo;
        try {
            defineSocket(useSelectThreadToWait, orb.getORBData().getLegacySocketFactory().createSocket(socketInfo));
            connectionCreated(socket);
        } catch (GetEndPointInfoAgainException ex) {
            throw wrapper.connectFailure(ex, socketInfo.getType(), socketInfo.getHost(), Integer.toString(socketInfo.getPort()));
        } catch (Exception ex) {
            throw wrapper.connectFailure(ex, socketInfo.getType(), socketInfo.getHost(), Integer.toString(socketInfo.getPort()));
        }
        setState(OPENING);
    }

    public String toString() {
        synchronized (stateEvent) {
            return "SocketFactoryConnectionImpl[" + " " + (socketChannel == null ? socket.toString() : socketChannel.toString()) + " "
                    + getStateString(getState()) + " " + shouldUseSelectThreadToWait() + " " + shouldUseWorkerThreadForEvent() + "]";
        }
    }
}

// End of file.
