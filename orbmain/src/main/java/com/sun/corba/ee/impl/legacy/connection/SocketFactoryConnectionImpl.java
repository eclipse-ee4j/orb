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

package com.sun.corba.ee.impl.legacy.connection;




import com.sun.corba.ee.impl.transport.ConnectionImpl;
import com.sun.corba.ee.spi.legacy.connection.GetEndPointInfoAgainException;
import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.trace.Transport;
import com.sun.corba.ee.spi.transport.ContactInfo;
import com.sun.corba.ee.spi.transport.SocketInfo;

import java.net.Socket;

/**
 * @author Harold Carr
 */
@Transport
public class SocketFactoryConnectionImpl
    extends
        ConnectionImpl
{
    @Transport
    private void connectionCreated( Socket socket ) { }

    // Socket-factory client constructor.
    public SocketFactoryConnectionImpl(ORB orb,
                                       ContactInfo contactInfo,
                                       boolean useSelectThreadToWait,
                                       boolean useWorkerThread)
    {
        super(orb, useSelectThreadToWait, useWorkerThread);

        // REVISIT - probably need a contact info for both
        // client and server for removing connections from cache?
        this.contactInfo = contactInfo;

        SocketInfo socketInfo =
            // REVISIT - case - needs interface method
            ((SocketFactoryContactInfoImpl)contactInfo).socketInfo;
        try {
            defineSocket(useSelectThreadToWait, orb.getORBData().getLegacySocketFactory().createSocket(socketInfo));
            connectionCreated( socket ) ;
        } catch (GetEndPointInfoAgainException ex) {
            throw wrapper.connectFailure(
                ex, socketInfo.getType(), socketInfo.getHost(),
                Integer.toString(socketInfo.getPort())) ;
        } catch (Exception ex) {
            throw wrapper.connectFailure(
                ex, socketInfo.getType(), socketInfo.getHost(),
                Integer.toString(socketInfo.getPort())) ;
        }
        setState(OPENING);
    }

    public String toString()
    {
        synchronized ( stateEvent ){
            return 
                "SocketFactoryConnectionImpl[" + " "
                + (socketChannel == null ?
                   socket.toString() : socketChannel.toString()) + " "
                + getStateString(getState()) + " "
                + shouldUseSelectThreadToWait() + " "
                + shouldUseWorkerThreadForEvent()
                + "]" ;
        }
    }
}

// End of file.
