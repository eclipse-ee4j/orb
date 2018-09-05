/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.transport.connection;

import java.io.IOException ;

import java.util.logging.Logger ;

import java.util.Map ;
import java.util.HashMap ;

import com.sun.corba.ee.spi.transport.connection.Connection ;
import com.sun.corba.ee.spi.transport.connection.InboundConnectionCache ;

import com.sun.corba.ee.spi.transport.concurrent.ConcurrentQueue;
import com.sun.corba.ee.spi.trace.Transport;
import org.glassfish.pfl.tf.spi.annotation.InfoMethod;

/** Manage connections that are initiated from another VM. 
 *
 * @author Ken Cavanaugh
 */
@Transport
public final class InboundConnectionCacheBlockingImpl<C extends Connection> 
    extends ConnectionCacheBlockingBase<C> 
    implements InboundConnectionCache<C> {

    private final Map<C,ConnectionState<C>> connectionMap ;

    protected String thisClassName() {
        return "InboundConnectionCacheBlockingImpl" ;
    }

    private static final class ConnectionState<C extends Connection> {
        final C connection ;            // Connection of the 
                                        // ConnectionState
        int busyCount ;                 // Number of calls to 
                                        // get without release
        int expectedResponseCount ;     // Number of expected 
                                        // responses not yet 
                                        // received

        ConcurrentQueue.Handle reclaimableHandle ;  // non-null iff connection 
                                                    // is not in use and has no
                                                    // outstanding requests

        ConnectionState( final C conn ) {
            this.connection = conn ;

            busyCount = 0 ;
            expectedResponseCount = 0 ;
            reclaimableHandle = null ;
        }
    }

    public InboundConnectionCacheBlockingImpl( final String cacheType, 
        final int highWaterMark, final int numberToReclaim, final long ttl ) {

        super( cacheType, highWaterMark, numberToReclaim, ttl ) ;

        this.connectionMap = new HashMap<C,ConnectionState<C>>() ;
    }

    // We do not need to define equals or hashCode for this class.

    @InfoMethod
    private void display( String msg, Object value ) {}

    @InfoMethod
    private void msg( String msg ) {}

    @Transport
    public synchronized void requestReceived( final C conn ) {
        ConnectionState<C> cs = getConnectionState( conn ) ;

        final int totalConnections = totalBusy + totalIdle ;
        if (totalConnections > highWaterMark())
            reclaim() ;

        ConcurrentQueue.Handle<C> reclaimHandle = cs.reclaimableHandle ;
        if (reclaimHandle != null) {
            reclaimHandle.remove() ;
            display( "removed from reclaimableQueue", conn ) ;
        }

        int count = cs.busyCount++ ;
        if (count == 0) {
            display( "moved from idle to busy", conn ) ;

            totalIdle-- ;
            totalBusy++ ;
        }
    }

    @Transport
    public synchronized void requestProcessed( final C conn, 
        final int numResponsesExpected ) {
        final ConnectionState<C> cs = connectionMap.get( conn ) ;

        if (cs == null) {
            msg( "connection was closed") ;
            return ;
        } else {
            cs.expectedResponseCount += numResponsesExpected ;
            int numResp = cs.expectedResponseCount ;
            int numBusy = --cs.busyCount ;

            display( "responses expected", numResp ) ;
            display( "connection busy count", numBusy ) ;

            if (numBusy == 0) {
                totalBusy-- ;
                totalIdle++ ;

                if (numResp == 0) {
                    display( "queuing reclaimable connection", conn ) ;

                    if ((totalBusy+totalIdle) > highWaterMark()) {
                        close( conn ) ;
                    } else {
                        cs.reclaimableHandle =
                            reclaimableConnections.offer( conn ) ;
                    }
                }
            }
        }
    }

    /** Decrement the number of expected responses.  When a connection is idle 
     * and has no expected responses, it can be reclaimed.
     */
    @Transport
    public synchronized void responseSent( final C conn ) {
        final ConnectionState<C> cs = connectionMap.get( conn ) ;
        final int waitCount = --cs.expectedResponseCount ;
        if (waitCount == 0) {
            display( "reclaimable connection", conn ) ;

            if ((totalBusy+totalIdle) > highWaterMark()) {
                close( conn ) ;
            } else {
                cs.reclaimableHandle =
                    reclaimableConnections.offer( conn ) ;
            }
        } else {
            display( "wait count", waitCount ) ;
        }
    }

    /** Close a connection, regardless of whether the connection is busy
     * or not.
     */
    @Transport
    public synchronized void close( final C conn ) {
        final ConnectionState<C> cs = connectionMap.remove( conn ) ;
        display( "connection state", cs ) ;

        int count = cs.busyCount ;

        if (count == 0)
            totalIdle-- ;
        else
            totalBusy-- ;

        final ConcurrentQueue.Handle rh = cs.reclaimableHandle ;
        if (rh != null) {
            msg( "connection was reclaimable") ;
            rh.remove() ;
        }

        try {
            conn.close() ;
        } catch (IOException exc) {
            display( "close threw", exc ) ;
        }
    }

    // Atomically either get the ConnectionState for conn OR 
    // create a new one AND put it in the cache
    private ConnectionState<C> getConnectionState( C conn ) {
        // This should be the only place a CacheEntry is constructed.
        ConnectionState<C> result = connectionMap.get( conn ) ;
        if (result == null) {
            result = new ConnectionState( conn ) ;
            connectionMap.put( conn, result ) ;
            totalIdle++ ;
        }

        return result ;
    }
}

// End of file.
