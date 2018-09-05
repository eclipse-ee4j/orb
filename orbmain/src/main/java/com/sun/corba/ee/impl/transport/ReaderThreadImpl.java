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

import com.sun.corba.ee.spi.transport.Connection;
import com.sun.corba.ee.spi.transport.ReaderThread;

import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.threadpool.Work;

import com.sun.corba.ee.spi.logging.ORBUtilSystemException;
import com.sun.corba.ee.spi.trace.Transport;
import org.glassfish.pfl.tf.spi.annotation.InfoMethod;

@Transport
public class ReaderThreadImpl implements ReaderThread, Work {
    private ORB orb;
    private Connection connection;
    private boolean keepRunning;
    private long enqueueTime;
    private static final ORBUtilSystemException wrapper =
        ORBUtilSystemException.self ;

    public ReaderThreadImpl(ORB orb, Connection connection)
    {
        this.orb = orb;
        this.connection = connection;
        keepRunning = true;
    }

    ////////////////////////////////////////////////////
    // 
    // ReaderThread methods.
    //

    public Connection getConnection() {
        return connection;
    }

    @Transport
    public synchronized void close() {
        keepRunning = false;

        // Note: do not close the connection here, as it may be 
        // re-used if we are simply closing the ReaderThread
        // because it has completed its operation.
        // If we are calling close because of transport shutdown,
        // the connection will be closed when the connection caches are closed.
    }

    private synchronized boolean isRunning() {
        return keepRunning ;
    }

    ////////////////////////////////////////////////////
    //
    // Work methods.
    //

    @InfoMethod
    private void display( String msg ) { }

    @InfoMethod
    private void display( String msg, Object value ) { }


    // REVISIT - this needs alot more from previous ReaderThread.
    @Transport
    public void doWork()
    {
        while (isRunning()) {
            try {
                display( "Start readerThread cycle", connection ) ;

                if (connection.read()) {
                    // REVISIT - put in pool;
                    return;
                }

                display( "End readerThread cycle" ) ;
            } catch (Throwable t) {
                wrapper.exceptionInReaderThread( t ) ;
                display( "Exception in read", t ) ;

                orb.getTransportManager().getSelector(0)
                    .unregisterForEvent(getConnection().getEventHandler());

                try {
                    if (isRunning()) {
                        getConnection().close();
                    }
                } catch (Exception exc) {
                    wrapper.ioExceptionOnClose( exc ) ;
                }
            }
        }
    }

    public void setEnqueueTime(long timeInMillis) {
        enqueueTime = timeInMillis;
    }

    public long getEnqueueTime() {
        return enqueueTime;
    }

    public String getName() { return "ReaderThread"; }
}

// End of file.
