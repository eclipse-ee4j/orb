/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.transport;

import com.sun.corba.ee.spi.transport.TcpTimeouts;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class NioBufferWriter {
    protected TemporarySelector tmpWriteSelector;
    protected final java.lang.Object tmpWriteSelectorLock = new java.lang.Object();

    private SocketChannel socketChannel;
    private TcpTimeouts tcpTimeouts;

    public NioBufferWriter(SocketChannel socketChannel, TcpTimeouts tcpTimeouts) {
        this.socketChannel = socketChannel;
        this.tcpTimeouts = tcpTimeouts;
    }

    void write(ByteBuffer byteBuffer) throws IOException {
        int nbytes = socketChannel.write(byteBuffer);
        if (byteBuffer.hasRemaining()) {
            // Can only occur on non-blocking connections.
            // Using long for backoff_factor to avoid floating point
            // calculations.
            TcpTimeouts.Waiter waiter = tcpTimeouts.waiter();
            SelectionKey sk = null;
            TemporarySelector tmpSelector = null;
            try {
                tmpSelector = getTemporaryWriteSelector(socketChannel);
                sk = tmpSelector.registerChannel(socketChannel, SelectionKey.OP_WRITE);
                while (byteBuffer.hasRemaining() && !waiter.isExpired()) {
                    int nsel = tmpSelector.select(waiter.getTimeForSleep());
                    if (nsel > 0) {
                        tmpSelector.removeSelectedKey(sk);
                        do {
                            // keep writing while bytes can be written
                            nbytes = socketChannel.write(byteBuffer);
                        } while (nbytes > 0 && byteBuffer.hasRemaining());
                    }
                    // selector timed out or no bytes have been written
                    if (nsel == 0 || nbytes == 0) {
                        waiter.advance();
                    }
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
                throw ConnectionImpl.wrapper.exceptionWhenWritingWithTemporarySelector(ioe, byteBuffer.position(), byteBuffer.limit(), waiter.timeWaiting(),
                        tcpTimeouts.get_max_time_to_wait());
            } finally {
                if (tmpSelector != null) {
                    tmpSelector.cancelAndFlushSelector(sk);
                }
            }
            // if message not fully written, throw exception
            if (byteBuffer.hasRemaining() && waiter.isExpired()) {
                // failed to write entire message
                throw ConnectionImpl.wrapper.transportWriteTimeoutExceeded(tcpTimeouts.get_max_time_to_wait(), waiter.timeWaiting());
            }
        }
    }

    void closeTemporaryWriteSelector() throws IOException {
        synchronized (tmpWriteSelectorLock) {
            if (tmpWriteSelector != null) {
                try {
                    tmpWriteSelector.close();
                } catch (IOException ex) {
                    throw ex;
                }
            }
        }
    }

    TemporarySelector getTemporaryWriteSelector(SocketChannel socketChannel1) throws IOException {
        synchronized (tmpWriteSelectorLock) {
            if (tmpWriteSelector == null) {
                tmpWriteSelector = new TemporarySelector(socketChannel1);
            }
        }
        return tmpWriteSelector;
    }
}
