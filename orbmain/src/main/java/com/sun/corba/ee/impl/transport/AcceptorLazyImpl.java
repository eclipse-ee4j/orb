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

package com.sun.corba.ee.impl.transport;

import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.trace.Transport;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.SelectableChannel;

/** A version of an Acceptor that does not own the ServerSocket.
 * Instead, SelectableChannels obtained from the ServerSocket are
 * given to the processSocket method 
 *
 * @author ken
 */
@Transport
public class AcceptorLazyImpl extends AcceptorBase {

    public AcceptorLazyImpl( ORB orb, int port, String name, String type ) {
        super( orb, port, name, type ) ;
    }

    @Override
    public boolean isLazy() {
        return true ;
    }

    public Socket getAcceptedSocket() {
        throw wrapper.notSupportedOnLazyAcceptor() ;
    }

    public SelectableChannel getChannel() {
        throw wrapper.notSupportedOnLazyAcceptor() ;
    }

    @Transport
    public synchronized boolean initialize() {
        if (initialized) {
            return false;
        }

        orb.getCorbaTransportManager().getInboundConnectionCache(this);

        initialized = true ;

        return true ;
    }

    public void close() {
        // NO-OP in this case
    }

    public ServerSocket getServerSocket() {
        throw wrapper.notSupportedOnLazyAcceptor() ;
    }

    public void doWork() {
        throw wrapper.notSupportedOnLazyAcceptor() ;
    }

    @Override
    public boolean shouldRegisterAcceptEvent() {
        return false;
    }
}
