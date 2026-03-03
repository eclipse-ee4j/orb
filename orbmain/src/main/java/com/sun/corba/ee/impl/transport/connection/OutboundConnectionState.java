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

package com.sun.corba.ee.impl.transport.connection;

import com.sun.corba.ee.spi.trace.Transport;
import com.sun.corba.ee.spi.transport.concurrent.ConcurrentQueue ;
import com.sun.corba.ee.spi.transport.connection.Connection ;
import com.sun.corba.ee.spi.transport.connection.ContactInfo ;

import java.io.IOException ;

import org.glassfish.gmbal.Description;
import org.glassfish.gmbal.ManagedAttribute ;
import org.glassfish.gmbal.ManagedData ;
import org.glassfish.pfl.tf.spi.annotation.InfoMethod;

@Transport
@ManagedData
public class OutboundConnectionState<C extends Connection> {
// The real state of the connection
    private enum ConnectionStateValue { NEW, BUSY, IDLE }

    private ConnectionStateValue csv ;  // Indicates state of connection

    final ContactInfo<C> cinfo ;        // ContactInfo used to create this 
                                        // Connection
    final C connection ;                // Connection of the ConnectionState
                                        //
    final OutboundCacheEntry<C> entry ; // This Connection's OutboundCacheEntry

    private int busyCount ;             // Number of calls to get without release
              
    int expectedResponseCount ;         // Number of expected responses not yet 
                                        // received

    // At all times, a connection is either on the busy or idle queue in 
    // its ConnectionEntry.  If the connection is on the idle queue, 
    // reclaimableHandle may also be non-null if the Connection is also on 
    // the reclaimableConnections queue.
    ConcurrentQueue.Handle<C> reclaimableHandle ;   // non-null iff 
                                                    // connection is not 
                                                    // in use and has no
                                                    // outstanding requests

    public String toString() {
        return "OutboundConnectionState[csv=" + csv
            + ",cinfo=" + cinfo
            + ",connection=" + connection
            + ",busyCount=" + busyCount
            + ",expectedResponceCount=" + expectedResponseCount + "]" ;
    }

// State exposed as managed attributes
    @ManagedAttribute
    @Description( "The current state of this connection")
    private synchronized ConnectionStateValue state() { return csv ; }

    @ManagedAttribute
    @Description( "The contactInfo used to create this connection")
    private synchronized ContactInfo<C> contactInfo() { return cinfo ; }

    @ManagedAttribute
    @Description( "The underlying connection for this ConnectionState")
    private synchronized C connection() { return connection ; }

    @ManagedAttribute
    private synchronized OutboundCacheEntry<C> cacheEntry() { return entry ; }
    
    @ManagedAttribute
    private synchronized int busyCount() { return busyCount ; }

    @ManagedAttribute
    private synchronized int expectedResponseCount() {
        return expectedResponseCount ;
    }

    @ManagedAttribute
    public synchronized boolean isReclaimable() {
        return reclaimableHandle != null ;
    }

    public OutboundConnectionState( final ContactInfo<C> cinfo, 
        final OutboundCacheEntry<C> entry, final C conn ) {

        this.csv = ConnectionStateValue.NEW ;
        this.cinfo = cinfo ;
        this.connection = conn ;
        this.entry = entry ;

        busyCount = 0 ;
        expectedResponseCount = 0 ;
        reclaimableHandle = null ;
    }

// Methods used in OutboundConnectionCacheBlockingImpl

    public synchronized boolean isBusy() { 
        return csv == ConnectionStateValue.BUSY ; 
    } 

    public synchronized boolean isIdle() { 
        return csv == ConnectionStateValue.IDLE ; 
    } 

    // Mark this connection as being busy, and increment 
    // busyCount.
    @Transport
    public synchronized void acquire() { 
        if (busyCount == 0) {
            entry.idleConnections.remove( connection ) ;
            removeFromReclaim() ;
            csv = ConnectionStateValue.BUSY ;
        } else {
            // Remove from busy queue so we can add it
            // back to LRU end later.
            entry.busyConnections.remove( connection ) ;
        }

        busyCount++ ;
        entry.busyConnections.offer( connection ) ;
    }

    public synchronized void setReclaimableHandle( 
        ConcurrentQueue.Handle<C> handle ) {
        reclaimableHandle = handle ;
    }

    @InfoMethod
    private void msg( String m ) {}

    @InfoMethod
    private void display( String m, Object value ) {}

    // Decrement busyCount, and move to IDLE if busyCount is 0.
    // Returns total number of expected responses
    @Transport
    public synchronized int release( int numResponsesExpected ) {
        expectedResponseCount += numResponsesExpected ;
        busyCount-- ;
        if (busyCount < 0) {
            msg( "ERROR: numBusy is <0!" ) ;
        }

        if (busyCount == 0) {
            csv = ConnectionStateValue.IDLE ;
            boolean wasOnBusy = entry.busyConnections.remove( connection ) ;
            if (!wasOnBusy) {
               msg( "connection not on busy queue, should have been" ) ;
            }
            entry.idleConnections.offer( connection ) ;
        }

        display( "expectedResponseCount", expectedResponseCount ) ;
        display( "busyCount", busyCount ) ;

        return expectedResponseCount ;
    }

    // Returns true iff the connection is idle and reclaimable
    @Transport
    public synchronized boolean responseReceived() {
        boolean result = false ;
        --expectedResponseCount ;
        display( "expectedResponseCount", expectedResponseCount ) ;

        if (expectedResponseCount < 0) {
            msg( "ERROR: expectedResponseCount<0!" ) ;
            expectedResponseCount = 0 ;
        }

        result = (expectedResponseCount == 0) && (busyCount == 0) ;

        return result ;
    }

    @Transport
    public synchronized void close() throws IOException {
        removeFromReclaim() ;

        if (csv == ConnectionStateValue.IDLE) {
            entry.idleConnections.remove( connection ) ;
        } else if (csv == ConnectionStateValue.BUSY) {
            entry.busyConnections.remove( connection ) ;
        }

        csv = ConnectionStateValue.NEW ;
        busyCount = 0 ;
        expectedResponseCount = 0  ;

        connection.close() ;
    }

    @Transport
    private void removeFromReclaim() {
        if (reclaimableHandle != null) {
            if (!reclaimableHandle.remove()) {
                display( "result was not on reclaimable Q", cinfo ) ;
            }
            reclaimableHandle = null ;
        }
    }
}

