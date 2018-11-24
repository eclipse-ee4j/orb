/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.spi.transport;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.ServerSocket;

import com.sun.corba.ee.spi.orb.ORB;

/**
 * @author Harold Carr
 */
public interface ORBSocketFactory {
    public void setORB(ORB orb);

    public ServerSocket createServerSocket(String type, InetSocketAddress inetSocketAddress) throws IOException;

    public Socket createSocket(String type, InetSocketAddress inetSocketAddress) throws IOException;

    public void setAcceptedSocketOptions(Acceptor acceptor, ServerSocket serverSocket, Socket socket) throws SocketException;

}

// End of file.
