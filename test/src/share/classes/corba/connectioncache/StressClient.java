/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package corba.connectioncache ;

import java.util.List ;
import java.util.ArrayList ;

import java.util.logging.Logger ;
import java.util.logging.Handler ;
import java.util.logging.Level ;
import java.util.logging.StreamHandler ;
import java.util.logging.Formatter ;
import java.util.logging.LogRecord ;

import java.io.IOException ;

import org.testng.Assert ;
import org.testng.annotations.Test ;

import com.sun.corba.ee.spi.transport.connection.ConnectionCache ;
import com.sun.corba.ee.spi.transport.connection.ContactInfo ;
import com.sun.corba.ee.spi.transport.connection.InboundConnectionCache ;
import com.sun.corba.ee.spi.transport.connection.OutboundConnectionCache ;
import com.sun.corba.ee.spi.transport.connection.ConnectionCacheFactory ;

import corba.framework.TestngRunner ;
import java.util.Random;

public class StressClient {
    // Ignore all of the LogRecord information except the message.
    public static class ReallySimpleFormatter extends Formatter {
        public synchronized String format( LogRecord record ) {
            return record.getMessage() + "\n" ;
        }
    }

    // Similar to ConsoleHandler, but outputs to System.out
    // instead of System.err, which works better with the 
    // CORBA test framework.
    public static class SystemOutputHandler extends StreamHandler {
        public SystemOutputHandler() {
            try {
                setLevel(Level.FINER);
                setFilter(null);
                setFormatter(new ReallySimpleFormatter());
                setEncoding(null);
                setOutputStream(System.out);
            } catch (Exception exc) {
                System.out.println( "Caught unexpected exception " + exc ) ;
            }
        }

        @Override
        public void publish(LogRecord record) {
            super.publish(record);      
            flush();
        }

        @Override
        public void close() {
            flush();
        }
    }

    private static final boolean DEBUG = false ;

    private static final Logger logger = Logger.getLogger( "test.corba" ) ;
    private static final Handler handler = new SystemOutputHandler() ;

    static {
        if (DEBUG) {
            logger.setLevel( Level.FINER ) ;
            logger.addHandler( handler ) ;
        }
    }

    private void testBanner( String msg ) {
        if (DEBUG) {
            System.out.println( 
                "======================================="
                + "==============================" ) ;

            System.out.println( msg ) ;

            System.out.println( 
                "======================================="
                + "==============================" ) ;
        }
    }

    private void checkStat( long actual, long expected, String type ) {
        Assert.assertEquals( actual, expected, type ) ;
    }

    private void checkStats( ConnectionCache<ConnectionImpl> cc, int idle,
        int reclaimable, int busy, int total ) {

        checkStat( cc.numberOfIdleConnections(), idle, 
            "Idle connections" ) ;
        checkStat( cc.numberOfReclaimableConnections(), reclaimable,
            "Reclaimable connections" ) ;
        checkStat( cc.numberOfBusyConnections(), busy,
            "Busy connections" ) ;
        checkStat( cc.numberOfConnections(), total, 
            "Total connections" ) ;
    }

    private static final int NUM_ITERATIONS = 2000 ;

    private static final int NUM_CONTACT_INFO = 8 ;
    private static final int NUM_THREADS = 40 ;
    private static final int MAX_CREATE_CONNECTION_DELAY = 19 ;
    private static final int MIN_CREATE_CONNECTION_DELAY = 1 ;
    private static final int MAX_OPERATION_DELAY = 11 ;
    private static final int MIN_OPERATION_DELAY = 1 ;

    private static final int NUMBER_TO_RECLAIM = 1 ;
    private static final int MAX_PARALLEL_CONNECTIONS = 4 ;
    private static final int HIGH_WATER_MARK = 15 ; 
    private static final int TTL = 2*60*1000 ;      // 2 minute TTL (time to live)
                                                    // for reclaimable connections

    private final OutboundConnectionCache<ConnectionImpl> obcache ; 
    private final InboundConnectionCache<ConnectionImpl> ibcache ; 
    private final List<ContactInfoImpl> cinfos ;
    private final RandomDelay operationDelay ;

    private final List<Thread> threads ;

    public class ClientThread extends Thread {
        private final Random rand = new Random() ;

        @Override
        public void run() {
            /*
            *      - randomly picks one of several ContactInfos
            *      - calls get to get a connection (the createConnections takes some random amount of time)
            *      - waits
            *      - accesses the connection
            *      - waits
            *      - calls release to release the connection expecting 1 response
            *      - waits
            *      - accesses the connection
            *      - calls responseReceived
             */
            for (int ctr=0; ctr<NUM_ITERATIONS; ctr++) {
                try {
                    final int cindex = rand.nextInt( cinfos.size() ) ;
                    ContactInfo<ConnectionImpl> cinfo = cinfos.get( cindex ) ;

                    ConnectionImpl conn = obcache.get(cinfo) ;

                    operationDelay.randomWait();
                    conn.access() ;
                    operationDelay.randomWait();
                    obcache.release( conn, 1 ) ;
                    operationDelay.randomWait();
                    conn.access() ;
                    operationDelay.randomWait();
                    obcache.responseReceived(conn);
                } catch (IOException exc) {
                    Assert.fail( "Error in thread", exc ) ;
                }
            }
        }
    }

    public StressClient() {
        obcache = ConnectionCacheFactory.<ConnectionImpl>
                makeBlockingOutboundConnectionCache(
                    "BlockingOutboundCache", HIGH_WATER_MARK, NUMBER_TO_RECLAIM,
                    MAX_PARALLEL_CONNECTIONS, TTL ) ;

        ibcache = ConnectionCacheFactory.<ConnectionImpl>
                makeBlockingInboundConnectionCache(
                    "BlockingInboundCache", HIGH_WATER_MARK, NUMBER_TO_RECLAIM, TTL ) ;

        cinfos = new ArrayList<ContactInfoImpl>() ;
        for (int ctr = 0; ctr < NUM_CONTACT_INFO; ctr++ ) {
            cinfos.add( ContactInfoImpl.get( "ContactInfo:"+ctr, 
                MIN_CREATE_CONNECTION_DELAY, MAX_CREATE_CONNECTION_DELAY ) ) ;
        }

        operationDelay = new RandomDelay(
            MIN_OPERATION_DELAY, MAX_OPERATION_DELAY ) ;

        threads = new ArrayList<Thread>() ;
    }

    @Test
    void stressOutboundCache() {
        for (int ctr=0; ctr<NUM_THREADS; ctr++) {
            threads.add( new ClientThread() ) ;
        }

        for (int ctr=0; ctr<NUM_THREADS; ctr++) {
            threads.get(ctr).start() ;
        }

        for (int ctr=0; ctr<NUM_THREADS; ctr++) {
            Thread thr = threads.get(ctr) ;
            while (thr.isAlive()) {
                try {
                    thr.join();
                } catch (InterruptedException ex) {
                    // ignore
                }
            }
        }

        System.out.println( "Number of busy connections: " +
            ibcache.numberOfBusyConnections() ) ;
        System.out.println( "Number of connections: " +
            ibcache.numberOfConnections() ) ;
        System.out.println( "Number of idle connections: " +
            ibcache.numberOfIdleConnections() ) ;
        System.out.println( "Number of reclaimable connections: " +
            ibcache.numberOfReclaimableConnections() ) ;
    }
    
    public static void main( String[] args ) {
        TestngRunner runner = new TestngRunner() ;
        runner.registerClass( Client.class ) ;
        runner.run() ;
        runner.systemExit() ;
    }
}
