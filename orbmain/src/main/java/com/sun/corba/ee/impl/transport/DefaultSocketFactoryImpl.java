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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.ServerSocket;
import java.nio.channels.SocketChannel;
import java.nio.channels.ServerSocketChannel;

import com.sun.corba.ee.spi.transport.Acceptor;

import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.transport.ORBSocketFactory;

import com.sun.corba.ee.spi.misc.ORBConstants;
import com.sun.corba.ee.impl.misc.ORBUtility;

public class DefaultSocketFactoryImpl
    implements ORBSocketFactory
{
    private ORB orb;

    public void setORB(ORB orb)
    {
        this.orb = orb;
    }

    public ServerSocket createServerSocket(String type, 
                                           InetSocketAddress inetSocketAddress)
        throws IOException
    {
        ServerSocketChannel serverSocketChannel = null;
        ServerSocket serverSocket = null;

        if (orb.getORBData().acceptorSocketType().equals(ORBConstants.SOCKETCHANNEL)) {
            serverSocketChannel = ServerSocketChannel.open();
            serverSocket = serverSocketChannel.socket();
        } else {
            serverSocket = new ServerSocket();
        }
        serverSocket.bind(inetSocketAddress);
        return serverSocket;
    }

    public Socket createSocket(String type, 
                               InetSocketAddress inetSocketAddress)
        throws IOException
    {
        SocketChannel socketChannel = null;
        Socket socket = null;

        if (orb.getORBData().connectionSocketType().equals(ORBConstants.SOCKETCHANNEL)) {
            socketChannel = ORBUtility.openSocketChannel(inetSocketAddress);
            socket = socketChannel.socket();
        } else {
            socket = new Socket(inetSocketAddress.getHostName(),
                                inetSocketAddress.getPort());
        }

        // Disable Nagle's algorithm (i.e., always send immediately).
        socket.setTcpNoDelay(true);

        return socket;
    }

    public void setAcceptedSocketOptions(Acceptor acceptor,
                                         ServerSocket serverSocket,
                                         Socket socket)
        throws SocketException
    {
        // Disable Nagle's algorithm (i.e., always send immediately).
        socket.setTcpNoDelay(true);
    }
}

// End of file.
