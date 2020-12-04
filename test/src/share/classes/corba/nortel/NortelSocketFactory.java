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

package corba.nortel ;

import com.sun.corba.ee.impl.transport.DefaultSocketFactoryImpl;

import java.io.IOException;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import java.lang.String;

public class NortelSocketFactory extends DefaultSocketFactoryImpl {
    private static Socket savedSocket = null ;
    private static boolean transportDown = false ;
    public static boolean useNio = true ;
    public static boolean verbose = false ;

    private static void msg( String str ) {
        if (verbose) {
            System.out.println( "+++NortelSocketFactory: " + str ) ;
        }
    }

    public ServerSocket createServerSocket(String type, InetSocketAddress in) throws IOException {
        if (transportDown) {
            msg( "Simulating transport failure..." ) ;
            throw new IOException( "Transport simulated down" ) ;
        }

        msg("In method createServerSocket, type:" + type + ", InetSocketAddress:" + in );
        ServerSocket serverSocket = new ServerSocket();
        serverSocket.bind(in);

        return serverSocket;
    }

    public Socket createSocket(String type, InetSocketAddress in) throws IOException {
        msg("In method createSocket, type:" + type + ", InetSocketAddress:" + in );
        if (transportDown) {
            msg( "Simulating transport failure..." ) ;
            throw new IOException( "Transport simulated down" ) ;
        }

        Socket socket = null;
        if (useNio) {
            socket = super.createSocket(type, in); 
        } else {
            socket = new Socket(in.getHostName(), in.getPort());
            socket.setTcpNoDelay(true);
        }
        
        savedSocket = socket;
        return socket;
    }

    public static void disconnectSocket(){
        msg( "Disconnecting socket" ) ;
        try  {
            savedSocket.close();
        } catch (Exception e) {

            msg("Exception " + e);
        }
    }

    // Simulate the failure of the destination: ensure that all connection attempts fail
    public static void simulateConnectionDown() {
        transportDown = true ;
    }

    public static void simulateConnectionUp() {
        transportDown = false ;
    }
}

