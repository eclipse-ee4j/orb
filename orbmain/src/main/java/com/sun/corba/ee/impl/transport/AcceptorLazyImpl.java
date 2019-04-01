/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.transport;

import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.trace.Transport;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.SelectableChannel;

/**
 * A version of an Acceptor that does not own the ServerSocket. Instead, SelectableChannels obtained from the
 * ServerSocket are given to the processSocket method
 *
 * @author ken
 */
@Transport
public class AcceptorLazyImpl extends AcceptorBase {

    public AcceptorLazyImpl(ORB orb, int port, String name, String type) {
        super(orb, port, name, type);
    }

    @Override
    public boolean isLazy() {
        return true;
    }

    public Socket getAcceptedSocket() {
        throw wrapper.notSupportedOnLazyAcceptor();
    }

    public SelectableChannel getChannel() {
        throw wrapper.notSupportedOnLazyAcceptor();
    }

    @Transport
    public synchronized boolean initialize() {
        if (initialized) {
            return false;
        }

        orb.getCorbaTransportManager().getInboundConnectionCache(this);

        initialized = true;

        return true;
    }

    public void close() {
        // NO-OP in this case
    }

    public ServerSocket getServerSocket() {
        throw wrapper.notSupportedOnLazyAcceptor();
    }

    public void doWork() {
        throw wrapper.notSupportedOnLazyAcceptor();
    }

    @Override
    public boolean shouldRegisterAcceptEvent() {
        return false;
    }
}
