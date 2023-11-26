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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.SelectableChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.security.AccessController;
import java.security.PrivilegedAction;

import com.sun.corba.ee.spi.transport.Selector;

import com.sun.corba.ee.spi.orb.ORB;

import com.sun.corba.ee.impl.misc.ORBUtility;
import com.sun.corba.ee.spi.trace.Transport;
import org.glassfish.pfl.tf.spi.annotation.InfoMethod;

/**
 * @author Harold Carr
 */
@Transport
public class AcceptorImpl extends AcceptorBase {
    protected ServerSocketChannel serverSocketChannel;
    protected ServerSocket serverSocket;

    private Class<?> lastExceptionClassSeen = null;

    public AcceptorImpl(ORB orb, int port, String name, String type) {
        super(orb, port, name, type);
    }

    @Transport
    public synchronized boolean initialize() {
        if (initialized) {
            return false;
        }
        InetSocketAddress inetSocketAddress = null;
        String host = "all interfaces";
        try {
            if (orb.getORBData().getListenOnAllInterfaces()) {
                inetSocketAddress = new InetSocketAddress(port);
            } else {
                host = orb.getORBData().getORBServerHost();
                inetSocketAddress = new InetSocketAddress(host, port);
            }
            serverSocket = orb.getORBData().getSocketFactory().createServerSocket(type, inetSocketAddress);
            internalInitialize();
            if (orb.getORBData().showInfoMessages()) {
                wrapper.infoCreateListenerSucceeded(host, Integer.toString(port));
            }
        } catch (Throwable t) {
            throw wrapper.createListenerFailed(t, host, port);
        }
        initialized = true;
        return true;
    }

    protected void internalInitialize() throws Exception {
        // Determine the listening port (for the IOR).
        // This is important when using emphemeral ports (i.e.,
        // when the port value to the constructor is 0).

        port = serverSocket.getLocalPort();

        // Register with transport (also sets up monitoring).

        orb.getCorbaTransportManager().getInboundConnectionCache(this);

        // Finish configuation.

        serverSocketChannel = serverSocket.getChannel();

        if (serverSocketChannel != null) {
            setUseSelectThreadToWait(orb.getORBData().acceptorSocketUseSelectThreadToWait());
            serverSocketChannel.configureBlocking(!orb.getORBData().acceptorSocketUseSelectThreadToWait());
        } else {
            // Configure to use listener and reader threads.
            setUseSelectThreadToWait(false);
        }
        setUseWorkerThreadForEvent(orb.getORBData().acceptorSocketUseWorkerThreadForEvent());

    }

    @InfoMethod
    private void usingServerSocket(ServerSocket ss) {
    }

    @InfoMethod
    private void usingServerSocketChannel(ServerSocketChannel ssc) {
    }

    @Transport
    public Socket getAcceptedSocket() {
        SocketChannel socketChannel = null;
        Socket socket = null;

        try {
            if (serverSocketChannel == null) {
                socket = serverSocket.accept();
                usingServerSocket(serverSocket);
            } else {
                socketChannel = serverSocketChannel.accept();
                socket = socketChannel.socket();
                usingServerSocketChannel(serverSocketChannel);
            }

            orb.getORBData().getSocketFactory().setAcceptedSocketOptions(this, serverSocket, socket);

            // Clear the last exception after a successful accept, in case
            // we get sporadic bursts of related failures.
            lastExceptionClassSeen = null;
        } catch (IOException e) {
            // Log the exception at WARNING level, unless the same exception
            // occurs repeatedly. In that case, only log the first exception
            // as a warning. Log all exceptions with the same class after the
            // first of that class at FINE level. We want to avoid flooding the
            // log when the same error occurs repeatedly (e.g. we are using an
            // SSLSocketChannel and there is a certificate problem that causes
            // ALL accepts to fail).
            if (e.getClass() == lastExceptionClassSeen) {
                wrapper.ioexceptionInAcceptFine(e);
            } else {
                lastExceptionClassSeen = e.getClass();
                wrapper.ioexceptionInAccept(e);
            }

            orb.getTransportManager().getSelector(0).unregisterForEvent(this);
            // REVISIT - need to close - recreate - then register new one.
            orb.getTransportManager().getSelector(0).registerForEvent(this);
            // NOTE: if register cycling we do not want to shut down ORB
            // since local beans will still work. Instead one will see
            // a growing log file to alert admin of problem.
        }

        return socket;
    }

    @InfoMethod
    private void closeException(IOException exc) {
    }

    @Transport
    public void close() {
        try {
            Selector selector = orb.getTransportManager().getSelector(0);
            selector.unregisterForEvent(this);
            if (serverSocketChannel != null) {
                serverSocketChannel.close();
            }
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            closeException(e);
        } finally {
        }
    }

    // EventHandler methods
    //

    public SelectableChannel getChannel() {
        return serverSocketChannel;
    }

    ////////////////////////////////////////////////////
    //
    // Work methods.
    //

    protected void accept() {
        processSocket(getAcceptedSocket());
    }

    @Transport
    public void doWork() {
        try {
            if (selectionKey.isAcceptable()) {
                AccessController.doPrivileged(new PrivilegedAction<Object>() {
                    public java.lang.Object run() {
                        accept();
                        return null;
                    }
                });
            } else {
                selectionKeyNotAcceptable();
            }
        } catch (SecurityException se) {
            securityException(se);
            String permissionStr = ORBUtility.getClassSecurityInfo(getClass());
            wrapper.securityExceptionInAccept(se, permissionStr);
        } catch (Exception ex) {
            otherException(ex);
            wrapper.exceptionInAccept(ex, ex.toString());
        } catch (Throwable t) {
            otherException(t);
        } finally {

            // IMPORTANT: To avoid bug (4953599), we force the
            // Thread that does the NIO select to also do the
            // enable/disable of Ops using SelectionKey.interestOps().
            // Otherwise, the SelectionKey.interestOps() may block
            // indefinitely.
            // NOTE: If "acceptorSocketUseWorkerThreadForEvent" is
            // set to to false in ParserTable.java, then this method,
            // doWork(), will get executed by the same thread
            // (SelectorThread) that does the NIO select.
            // If "acceptorSocketUseWorkerThreadForEvent" is set
            // to true, a WorkerThread will execute this method,
            // doWork(). Hence, the registering of the enabling of
            // the SelectionKey's interestOps is done here instead
            // of calling SelectionKey.interestOps(<interest op>).

            Selector selector = orb.getTransportManager().getSelector(0);
            selector.registerInterestOps(this);
        }
    }

    ////////////////////////////////////////////////////
    //
    // SocketOrChannelAcceptor
    //

    public ServerSocket getServerSocket() {
        return serverSocket;
    }

    @InfoMethod
    private void selectionKeyNotAcceptable() {
    }

    @InfoMethod
    private void securityException(SecurityException se) {
    }

    @InfoMethod
    private void otherException(Throwable t) {
    }
    // END Legacy support
}

// End of file.
