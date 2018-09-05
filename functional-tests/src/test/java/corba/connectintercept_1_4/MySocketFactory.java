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
// Created       : by Everett Anderson.
// Last Modified : 2004 May 12 (Wed) 11:42:45 by Harold Carr.
//

package corba.connectintercept_1_4;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;

import org.omg.CORBA.ORB;
import org.omg.IOP.TaggedComponent;

import com.sun.corba.ee.spi.ior.IOR;
import com.sun.corba.ee.spi.ior.iiop.IIOPProfileTemplate ;
import com.sun.corba.ee.spi.ior.iiop.IIOPAddress ;
import com.sun.corba.ee.spi.legacy.connection.GetEndPointInfoAgainException;
import com.sun.corba.ee.spi.legacy.connection.ORBSocketFactory;
import com.sun.corba.ee.spi.transport.SocketInfo;

import com.sun.corba.ee.impl.misc.ORBUtility;

public class MySocketFactory
    implements
        ORBSocketFactory
{
    private Hashtable iorSocketInfo = new Hashtable();

    public static int numCallsGetEndPointInfo = 0;
    public static int numCallsCreateSocket = 0;

    //
    // Constructor used during ORB initialization.
    //

    public MySocketFactory()
    {
        System.out.println("MySocketFactory()");
    }

    //
    // Server side.
    //

    public ServerSocket createServerSocket(String type, int port)
        throws
            IOException 
    {
        System.out.println("createServerSocket(" + type + ", " + port + ")");
        createSocketMessage("ServerSocket", type, "localhost", port);

        if (type.equals(ORBSocketFactory.IIOP_CLEAR_TEXT)) {
            ServerSocketChannel serverSocketChannel =
                ServerSocketChannel.open();
            ServerSocket serverSocket = serverSocketChannel.socket();
            serverSocket.bind(new InetSocketAddress(port));
            return serverSocket;
        } else {
            return new ServerSocket(port);
        }
    }

    //
    // Client side.
    //

    public SocketInfo getEndPointInfo(ORB orb, 
                                        IOR ior,
                                        SocketInfo socketInfo)
    {
        numCallsGetEndPointInfo++;

        System.out.println("MySocketFactory.getEndPointInfo: entering.");

        if (socketInfo != null) {
            if (socketInfo instanceof SocketInfoImpl) {
                System.out.println(
                    "MySocketFactory.getEndPointInfo: found cookie: "
                    + ((SocketInfoImpl)socketInfo).getCookie());
            } else {
                throw new RuntimeException("Wrong type of cookie");
            }
        }

        // Get the clear text host/port form the profile.

        IIOPProfileTemplate temp = (IIOPProfileTemplate)ior.getProfile().getTaggedProfileTemplate() ;
        IIOPAddress primary = temp.getPrimaryAddress() ;
        String host = primary.getHost().toLowerCase();
        int    port = primary.getPort();

        if (/*false*/ socketInfo == null) {
            // The first time it is called on each invocation
            // we give bad info so we can raise an exception
            // in createSocket and end up here again to test
            // the get info loop.
            socketInfo = 
                new SocketInfoImpl(Common.DummyType,
                                     Common.DummyHost,
                                     Common.DummyPort,
                                     "dummy cookie");
            printSocketInfoReturn(host, port, socketInfo);
            return socketInfo;
        }

        // The only time we should ever get called with our cookie
        // is when we "pass ourselves" the above dummy info.
        // Check this is the case and ignore the dummy info.

        if (socketInfo != null) {
            if (!socketInfo.getType().equals(Common.DummyType) ||
                !socketInfo.getHost().equals(Common.DummyHost) ||
                socketInfo.getPort() != Common.DummyPort)
            {
                throw new RuntimeException("MySocketFactory.getEndPointInfo: should never happen.");
            }
        }

        // Another loop for the test.
        // If the component has alternate type/port info (assume same host)
        // then set up a list to iterate down on each call, starting
        // over when list is empty.
        // The cycle is (default, 1, 2, 3)*;
        Vector portList = (Vector) iorSocketInfo.get(ior);
        if (portList == null) {
            TaggedComponent taggedComponents[] =
                ior.getProfile().getTaggedProfileTemplate().getIOPComponents(
                    (com.sun.corba.ee.spi.orb.ORB)orb, Common.ListenPortsComponentID);
            if (taggedComponents.length > 0) {
                String componentData = 
                    new String(taggedComponents[0].component_data);
                System.out.println("componentData: " + componentData);
                iorSocketInfo.put(ior, parseComponentData(componentData));
            }
            socketInfo = 
                new SocketInfoImpl(ORBSocketFactory.IIOP_CLEAR_TEXT,
                                     host,
                                     port,
                                     "clear text cookie");
        } else {
            TypePortPair nextPair = (TypePortPair)portList.remove(0);
            if (portList.size() == 0) {
                // When there is not more remembered info remove
                // it from the cache so it will start over.
                iorSocketInfo.remove(ior);
            }
            socketInfo =
                new SocketInfoImpl(nextPair.getType(),
                                     host,
                                     nextPair.getPort(),
                                     "component cookie");
        }
        printSocketInfoReturn(host, port, socketInfo);
        return socketInfo;
    }

    private void printSocketInfoReturn(String host,
                                    int port,
                                    SocketInfo socketInfo)
    {
        System.out.println("getEndPointInfo(" + host + ", " + port + ")" +
                           " = " + socketInfo);
    }

    public Socket createSocket(SocketInfo socketInfo)
        throws
            IOException,
            GetEndPointInfoAgainException
    {
        numCallsCreateSocket++;

        String type = socketInfo.getType();
        String host = socketInfo.getHost();
        int    port = socketInfo.getPort();
        System.out.println("createSocket(" + type + ", " + host + ", " + port +")");
        if (type.equals(Common.DummyType) &&
            host.equals(Common.DummyHost) &&
            port == Common.DummyPort) 
        {
            // This is to test the "get info" loop.
            throw new GetEndPointInfoAgainException(socketInfo);
        }

        createSocketMessage("ClientSocket", type, host, port);

        if (type.equals(ORBSocketFactory.IIOP_CLEAR_TEXT)) {
            InetSocketAddress address = 
                new InetSocketAddress(host, port);
            SocketChannel socketChannel = ORBUtility.openSocketChannel(address);
            Socket socket = socketChannel.socket();
            try {
                socket.setTcpNoDelay(true);
            } catch (Exception e) {
            }
            return socket;
        } else {
            return new Socket(host, port);
        }
    }

    private void createSocketMessage(String clientOrServer, 
                                     String type,
                                     String host,
                                     int port)
    {
        System.out.println("  creating " + clientOrServer + 
                           " " + type + " " + host + " " + port);
    }

    private Vector parseComponentData(String componentData)
    {
        // REVISIT:
        // Workaround for:
        // componentData: ^@^@^@^@^@^@^@*MyType1:48154,MyType2:48155,MyType3:48156^@ 
        // when getting forwarded IOR from orbd.
        if (!componentData.startsWith(Common.MyType1)) {
            componentData = new String(componentData.getBytes(),
                                       8,
                                       componentData.length() - 9);
        }


        Vector typePortPairs = new Vector();
        StringTokenizer pairs = 
            new StringTokenizer(componentData, ",");
        while (pairs.hasMoreTokens()) {
            String current = pairs.nextToken();
            StringTokenizer pair = new StringTokenizer(current, ":");
            String type = null;
            int port = -1;
            if  (pair.hasMoreTokens()) {
                type = pair.nextToken();
                if (pair.hasMoreTokens()) {
                    try {
                        port = Integer.parseInt(pair.nextToken());
                    } catch (NumberFormatException e) {
                    }
                }
            }
            if (type == null || port == -1) {
                throw new RuntimeException("Improper ORBListenSocket format: "
                                           + componentData);
            }
            typePortPairs.add(new TypePortPair(type, port));
        }
        return typePortPairs;
    }
}

class SocketInfoImpl
    extends
        com.sun.corba.ee.impl.legacy.connection.EndPointInfoImpl
{
    String cookie;

    SocketInfoImpl(String type, String host, int port, String cookie)
    {
        super(type, port, host);
        this.cookie = cookie;
    }

    String getCookie()
    {
        return cookie;
    }

    @Override
    public String toString()
    {
        return 
            "(SocketInfoImpl " + type 
            + " " + hostname + " " + port 
            + " " + cookie
            + ")";
    }
}

class TypePortPair
{
    private String type;
    private int    port;
    TypePortPair (String type, int port)
    {
        this.type = type;
        this.port = port;
    }
    public String getType  () { return type; }
    public int    getPort  () { return port; }
    @Override
    public String toString () { return type + ":" + port; }
}

                

// End of file.
