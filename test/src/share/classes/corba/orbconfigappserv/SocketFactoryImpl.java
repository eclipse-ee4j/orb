/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

//
// Created       : 2003 Apr 15 (Tue) 16:16:46 by Harold Carr.
// Last Modified : 2004 May 12 (Wed) 11:59:38 by Harold Carr.
//

package corba.orbconfigappserv;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.omg.CORBA.ORB;

import com.sun.corba.ee.spi.ior.IOR;
import com.sun.corba.ee.spi.legacy.connection.GetEndPointInfoAgainException;
import com.sun.corba.ee.spi.legacy.connection.ORBSocketFactory;
import com.sun.corba.ee.spi.transport.SocketInfo;

import com.sun.corba.ee.impl.legacy.connection.DefaultSocketFactory;
import com.sun.corba.ee.impl.legacy.connection.EndPointInfoImpl;

public class SocketFactoryImpl
    implements 
        ORBSocketFactory
{
    ORBSocketFactory socketFactory;

    public SocketFactoryImpl()
    {
        System.out.println("SocketFactoryImpl()");
        socketFactory = new DefaultSocketFactory();
    }

    public ServerSocket createServerSocket(String type, int port)
        throws
            IOException
    {
        System.out.println("createServerSocket: " + type + " " + port);
        return socketFactory.createServerSocket(type, port);
    }

    public SocketInfo getEndPointInfo(ORB orb,
                                        IOR ior,
                                        SocketInfo socketInfo)
    {
        System.out.println("getEndPointInfo");
        return socketFactory.getEndPointInfo(orb, ior, socketInfo);
    }

    public Socket createSocket(SocketInfo socketInfo)
        throws
            IOException,
            GetEndPointInfoAgainException
    {
        System.out.println("createSocket: " + socketInfo);
        return socketFactory.createSocket(socketInfo);
    }
}

// End of file.
