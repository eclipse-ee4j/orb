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

import java.util.Collection;
import java.util.Iterator;

import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.transport.Connection;
import com.sun.corba.ee.spi.transport.ConnectionCache;

import com.sun.corba.ee.spi.logging.ORBUtilSystemException;
import com.sun.corba.ee.spi.trace.Transport;

import org.glassfish.gmbal.ManagedAttribute ;
import org.glassfish.gmbal.Description ;
import org.glassfish.gmbal.NameValue ;

import org.glassfish.external.statistics.CountStatistic ;
import org.glassfish.external.statistics.impl.CountStatisticImpl ;
import org.glassfish.pfl.tf.spi.annotation.InfoMethod;


    ////////////////////////////////////////////////////
    //
    // spi.transport.ConnectionCache
    //
/**
 * @author Harold Carr
 */
@Transport
public abstract class ConnectionCacheBase
    implements
        ConnectionCache
{
    protected static final ORBUtilSystemException wrapper =
        ORBUtilSystemException.self ;
    private static final String STAT_UNIT = "count" ;

    private static final String TOTAL_ID_STD    = "TotalConnections" ;
    private static final String TOTAL_ID        = "totalconnections" ;
    private static final String IDLE_ID_STD     = "ConnectionsIdle" ;
    private static final String IDLE_ID         = "connectionsidle" ;
    private static final String BUSY_ID_STD     = "ConnectionsBusy" ;
    private static final String BUSY_ID         = "connectionsbusy" ;

    private static final String TOTAL_DESC = 
        "Total number of connections in the connection cache" ; 
    private static final String IDLE_DESC = 
        "Number of connections in the connection cache that are idle" ; 
    private static final String BUSY_DESC =
        "Number of connections in the connection cache that are in use" ; 

    protected ORB orb;
    protected long timestamp = 0;
    protected String cacheType;
    protected String monitoringName;

    protected ConnectionCacheBase(ORB orb, String cacheType,
                                       String monitoringName)
    {
        this.orb = orb;
        this.cacheType = cacheType;
        this.monitoringName = monitoringName;
        dprintCreation();
    }
    
    @NameValue
    public String getCacheType()
    {
        return cacheType;
    }

    public synchronized void stampTime(Connection c)
    {
        // _REVISIT_ Need to worry about wrap around some day
        c.setTimeStamp(timestamp++);
    }

    private CountStatistic  makeCountStat( String name, String desc, 
        long value ) {

        CountStatisticImpl result = new CountStatisticImpl( name,
            STAT_UNIT, desc ) ;
        result.setCount( value ) ;
        return result ;
    }

    public void close() {
        synchronized (backingStore()) {
            for (Object obj : values()) {
                ((Connection)obj).closeConnectionResources() ;
            }
        }
    }

    @ManagedAttribute( id=TOTAL_ID ) 
    @Description( TOTAL_DESC ) 
    private CountStatistic numberOfConnectionsAttr()
    {
        return makeCountStat( TOTAL_ID_STD, TOTAL_DESC, 
            numberOfConnections() ) ;
    }

    public long numberOfConnections()
    {
        long count = 0 ;
        synchronized (backingStore()) {
            count = values().size();
        }

        return count ;
    }

    @ManagedAttribute( id=IDLE_ID ) 
    @Description( IDLE_DESC )
    private CountStatistic numberOfIdleConnectionsAttr()
    {
        return makeCountStat( IDLE_ID_STD, IDLE_DESC, 
            numberOfIdleConnections() ) ;
    }

    public long numberOfIdleConnections()
    {
        long count = 0;
        synchronized (backingStore()) {
            Iterator connections = values().iterator();
            while (connections.hasNext()) {
                if (! ((Connection)connections.next()).isBusy()) {
                    count++;
                }
            }
        }

        return count ;
    }

    @ManagedAttribute( id=BUSY_ID ) 
    @Description( BUSY_DESC )
    private CountStatistic numberOfBusyConnectionsAttr()
    {
        return makeCountStat( BUSY_ID_STD, BUSY_DESC, 
            numberOfBusyConnections() ) ;
    }

    public long numberOfBusyConnections()
    {
        long count = 0;
        synchronized (backingStore()) {
            Iterator connections = values().iterator();
            while (connections.hasNext()) {
                if (((Connection)connections.next()).isBusy()) {
                    count++;
                }
            }
        }
        
        return count ;
    }


    /**
     * Discarding least recently used Connections that are not busy
     *
     * This method must be synchronized since one WorkerThread could
     * be reclaming connections inside the synchronized backingStore
     * block and a second WorkerThread (or a SelectorThread) could have
     * already executed the if (numberOfConnections &lt;= .... ). As a
     * result the second thread would also attempt to reclaim connections.
     *
     * If connection reclamation becomes a performance issue, the connection
     * reclamation could make its own task and consequently executed in
     * a separate thread.
     * Currently, the accept &amp; reclaim are done in the same thread, WorkerThread
     * by default. It could be changed such that the SelectorThread would do
     * it for SocketChannels and WorkerThreads for Sockets by updating the
     * ParserTable.
     */
    @Transport
    @Override
    synchronized public boolean reclaim() {
        long numberOfConnections = numberOfConnections() ;

        reclaimInfo( numberOfConnections,
            orb.getORBData().getHighWaterMark(),
            orb.getORBData().getNumberToReclaim() ) ;

        if (numberOfConnections <= orb.getORBData().getHighWaterMark()) {
            return false;
        }

        Object backingStore = backingStore();
        synchronized (backingStore) {

                // REVISIT - A less expensive alternative connection reclaiming
                //           algorithm could be investigated.

            for (int i=0; i < orb.getORBData().getNumberToReclaim(); i++) {
                Connection toClose = null;
                long lru = java.lang.Long.MAX_VALUE;
                Iterator iterator = values().iterator();

                // Find least recently used and not busy connection in cache
                while ( iterator.hasNext() ) {
                    Connection c = (Connection) iterator.next();
                    if ( !c.isBusy() && c.getTimeStamp() < lru ) {
                        toClose = c;
                        lru = c.getTimeStamp();
                    }
                }

                if ( toClose == null ) {
                    return false;
                }

                try {
                    closingInfo( toClose ) ;
                    toClose.close();
                } catch (Exception ex) {
                    // REVISIT - log
                }
            }

            connectionsReclaimedInfo(
                numberOfConnections - numberOfConnections() );
        }

        return true;
    }

    public String getMonitoringName()
    {
        return monitoringName;
    }

    ////////////////////////////////////////////////////
    //
    // Implementation
    //

    // This is public so folb.Server test can access it.
    public abstract Collection values();

    protected abstract Object backingStore();

    @InfoMethod
    private void creationInfo(String cacheType, String monitoringName) { }

    @Transport
    protected void dprintCreation() {
        creationInfo( getCacheType(), getMonitoringName() ) ;
    }

    @InfoMethod
    private void cacheStatsInfo( long numberOfConnections,
        long numberOfBusyConnections, long numberOfIdleConnections,
        int highWaterMark, int numberToReclaim) { }

    @Transport
    protected void cacheStatisticsInfo() {
        cacheStatsInfo( numberOfConnections(), numberOfBusyConnections(),
            numberOfIdleConnections(), orb.getORBData().getHighWaterMark(),
            orb.getORBData().getNumberToReclaim() ) ;
    }

    @InfoMethod
    private void reclaimInfo(long numberOfConnections, int highWaterMark,
        int numberToReclaim) { }

    @InfoMethod
    private void closingInfo(Connection toClose) { }

    @InfoMethod
    private void connectionsReclaimedInfo(long l) { }
}

// End of file.
