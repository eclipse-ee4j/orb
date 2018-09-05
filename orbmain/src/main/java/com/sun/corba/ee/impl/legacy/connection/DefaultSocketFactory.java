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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import org.omg.CORBA.ORB;

import com.sun.corba.ee.spi.ior.IOR;
import com.sun.corba.ee.spi.ior.iiop.IIOPProfileTemplate ;
import com.sun.corba.ee.spi.ior.iiop.IIOPAddress ;
import com.sun.corba.ee.spi.legacy.connection.GetEndPointInfoAgainException;
import com.sun.corba.ee.spi.legacy.connection.ORBSocketFactory;
import com.sun.corba.ee.spi.transport.SocketInfo;

import com.sun.corba.ee.spi.logging.ORBUtilSystemException;
import com.sun.corba.ee.spi.misc.ORBConstants;
import com.sun.corba.ee.impl.misc.ORBUtility;

public class DefaultSocketFactory 
    implements 
        ORBSocketFactory
{
    private com.sun.corba.ee.spi.orb.ORB orb;
    private static final ORBUtilSystemException wrapper =
        ORBUtilSystemException.self ;

    public DefaultSocketFactory()
    {
    }

    public void setORB(com.sun.corba.ee.spi.orb.ORB orb)
    {
        this.orb = orb;
    }

    public ServerSocket createServerSocket(String type, int port)
        throws
            IOException
    {
        if (! type.equals(ORBSocketFactory.IIOP_CLEAR_TEXT)) {
            throw wrapper.defaultCreateServerSocketGivenNonIiopClearText( type ) ;
        }

        ServerSocket serverSocket;

        if (orb.getORBData().acceptorSocketType().equals(ORBConstants.SOCKETCHANNEL)) {
            ServerSocketChannel serverSocketChannel =
                ServerSocketChannel.open();
            serverSocket = serverSocketChannel.socket();
        } else {
            serverSocket = new ServerSocket();
        }
        serverSocket.bind(new InetSocketAddress(port));
        return serverSocket;
    }

    public SocketInfo getEndPointInfo(ORB orb,
                                        IOR ior,
                                        SocketInfo socketInfo)
    {
        IIOPProfileTemplate temp = 
            (IIOPProfileTemplate)ior.getProfile().getTaggedProfileTemplate() ;
        IIOPAddress primary = temp.getPrimaryAddress() ;

        return new EndPointInfoImpl(ORBSocketFactory.IIOP_CLEAR_TEXT,
                                    primary.getPort(),
                                    primary.getHost().toLowerCase());
    }

    public Socket createSocket(SocketInfo socketInfo)
        throws
            IOException,
            GetEndPointInfoAgainException
    {
        Socket socket;

        if (orb.getORBData().acceptorSocketType().equals(ORBConstants.SOCKETCHANNEL)) {
            InetSocketAddress address = 
                new InetSocketAddress(socketInfo.getHost(), 
                                      socketInfo.getPort());
            SocketChannel socketChannel = ORBUtility.openSocketChannel(address);
            socket = socketChannel.socket();
        } else {
            socket = new Socket(socketInfo.getHost(), 
                                socketInfo.getPort());
        }

        // REVISIT - this is done in SocketOrChannelConnectionImpl
        try {
            socket.setTcpNoDelay(true);
        } catch (Exception e) {
            wrapper.couldNotSetTcpNoDelay( e ) ;
        }
        return socket;
    }
}

// End of file.

