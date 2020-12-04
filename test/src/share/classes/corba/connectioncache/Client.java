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

package corba.connectioncache ;

import java.util.Collection ;
import java.util.List ;
import java.util.ArrayList ;
import java.util.Set ;
import java.util.HashSet ;

import java.util.logging.Logger ;
import java.util.logging.Handler ;
import java.util.logging.Level ;
import java.util.logging.StreamHandler ;
import java.util.logging.Formatter ;
import java.util.logging.LogRecord ;

import java.io.IOException ;

import org.testng.Assert ;
import org.testng.annotations.Test ;

import com.sun.corba.ee.spi.transport.concurrent.ConcurrentQueue ;
import com.sun.corba.ee.spi.transport.concurrent.ConcurrentQueueFactory ;

import com.sun.corba.ee.spi.transport.connection.ConnectionFinder ;
import com.sun.corba.ee.spi.transport.connection.ConnectionCache ;
import com.sun.corba.ee.spi.transport.connection.ContactInfo ;
import com.sun.corba.ee.spi.transport.connection.InboundConnectionCache ;
import com.sun.corba.ee.spi.transport.connection.OutboundConnectionCache ;
import com.sun.corba.ee.spi.transport.connection.ConnectionCacheFactory ;

import corba.framework.TestngRunner ;

public class Client {
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

    private static final int NUMBER_TO_RECLAIM = 1 ;
    private static final int MAX_PARALLEL_CONNECTIONS = 4 ;
    private static final int HIGH_WATER_MARK = 20 ; // must be a multiple of
                                                    // MAX_PARALLEL_CONNECTIONS
                                                    // for outboundCacheTest4.
    private static final int TTL = 2*60*1000 ;      // 2 minute TTL (time to live)
                                                    // for reclaimable connections

    private static final OutboundConnectionCache<ConnectionImpl> obcache = 
        ConnectionCacheFactory.<ConnectionImpl>
            makeBlockingOutboundConnectionCache(
                "BlockingOutboundCache", HIGH_WATER_MARK, NUMBER_TO_RECLAIM,
                MAX_PARALLEL_CONNECTIONS, TTL ) ;

    private static final InboundConnectionCache<ConnectionImpl> ibcache = 
        ConnectionCacheFactory.<ConnectionImpl>
            makeBlockingInboundConnectionCache(
                "BlockingInboundCache", HIGH_WATER_MARK, NUMBER_TO_RECLAIM, TTL ) ;

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

    // Test ConcurrentQueue
    @Test()
    public void nonBlockingConcurrentQueueTest() {
        testBanner( "nonBlockingConccurentQueueTest" ) ;
        ConcurrentQueue<Integer> testQ = 
            ConcurrentQueueFactory.<Integer>makeConcurrentQueue( TTL ) ;
        testConcurrentQueue( testQ ) ;
    }

    private ConcurrentQueue.Handle<Integer> addData( 
        ConcurrentQueue<Integer> arg,
        int[] data, int valueForHandleToReturn ) {

        ConcurrentQueue.Handle<Integer> savedHandle = null ;

        for (int val : data) {
            ConcurrentQueue.Handle<Integer> handle = arg.offer( val ) ;
            if (val == valueForHandleToReturn) {
                savedHandle = handle ;
            }
        }

        return savedHandle ;
    }

    private void destructiveValidation( ConcurrentQueue<Integer> queue,
        int[] master ) {

        Assert.assertEquals( queue.size(), master.length ) ;
        for (int val : master) {
            int qval = queue.poll().value() ;
            Assert.assertEquals( val, qval ) ; 
        }
    }
    
    private void testConcurrentQueue( ConcurrentQueue<Integer> arg ) {
        final int[] data = { 23, 43, 51, 3, 7, 9, 22, 33 } ;
        final int valueToDelete = 51 ;
        final int[] dataAfterDelete = { 23, 43, 3, 7, 9, 22, 33 } ;

        addData( arg, data, valueToDelete ) ;

        destructiveValidation( arg, data ) ;

        final ConcurrentQueue.Handle<Integer> delHandle =
            addData( arg, data, valueToDelete ) ;

        delHandle.remove() ;

        destructiveValidation( arg, dataAfterDelete ) ;
    }

    private void checkStat( long actual, long expected, String type ) {
        Assert.assertEquals( actual, expected, type ) ;
    }

    private void checkStats( ConnectionCache cc, int idle, int reclaimable, 
        int busy, int total ) {

        checkStat( cc.numberOfIdleConnections(), idle, 
            "Idle connections" ) ;
        checkStat( cc.numberOfReclaimableConnections(), reclaimable,
            "Reclaimable connections" ) ;
        checkStat( cc.numberOfBusyConnections(), busy,
            "Busy connections" ) ;
        checkStat( cc.numberOfConnections(), total, 
            "Total connections" ) ;
    }

    // Each of the simple tests expects that all connections in the cache have 
    // been closed, which may not be the case if a test fails.  So we impose 
    // an order on the tests to make sure that we stop if a test leaves things 
    // in a bad state.
    //
    // Inbound and Outbound can be tested independently.
    
    // Do a single get/release/responseReceived cycle
    @Test(dependsOnMethods={"nonBlockingConcurrentQueueTest"})
    public void outboundTest1() throws IOException {
        testBanner( "outboundTest1: single cycle" ) ;
        ContactInfoImpl cinfo = ContactInfoImpl.get( "FirstContact" ) ;
        ConnectionImpl c1 = obcache.get( cinfo ) ;
        checkStats( obcache, 0, 0, 1, 1 ) ;

        obcache.release( c1, 1 ) ;
        checkStats( obcache, 1, 0, 0, 1 ) ;

        obcache.responseReceived( c1 ) ;
        checkStats( obcache, 1, 1, 0, 1 ) ;
        
        obcache.close( c1 ) ;
        checkStats( obcache, 0, 0, 0, 0 ) ;
    }

    // Do two interleaved get/release/responseReceived cycles
    @Test(dependsOnMethods={"nonBlockingConcurrentQueueTest", "outboundTest1"})
    public void outboundTest2() throws IOException {
        testBanner( "outboundTest2: 2 cycles interleaved" ) ;
        ContactInfoImpl cinfo = ContactInfoImpl.get( "FirstContact" ) ;
        ConnectionImpl c1 = obcache.get( cinfo ) ;
        checkStats( obcache, 0, 0, 1, 1 ) ;

        ConnectionImpl c2 = obcache.get( cinfo ) ;
        checkStats( obcache, 0, 0, 2, 2 ) ;

        Assert.assertNotSame( c1, c2) ;

        obcache.release( c1, 1 ) ;
        checkStats( obcache, 1, 0, 1, 2 ) ;

        obcache.release( c2, 1 ) ;
        checkStats( obcache, 2, 0, 0, 2 ) ;

        obcache.responseReceived( c1 ) ;
        checkStats( obcache, 2, 1, 0, 2 ) ;
        
        obcache.responseReceived( c2 ) ;
        checkStats( obcache, 2, 2, 0, 2 ) ;
        
        obcache.close( c2 ) ;
        checkStats( obcache, 1, 1, 0, 1 ) ;
        
        obcache.close( c1 ) ;
        checkStats( obcache, 0, 0, 0, 0 ) ;
    }

    // Do enough gets to start using busy connections.
    @Test(dependsOnMethods={"nonBlockingConcurrentQueueTest", "outboundTest2"})
    public void outboundTest3() throws IOException {
        testBanner( "outboundTest3: cycle to busy connections" ) ;
        ContactInfoImpl cinfo = ContactInfoImpl.get( "FirstContact" ) ;
        Set<ConnectionImpl> conns = new HashSet<ConnectionImpl>() ;
        for (int ctr=0; ctr<MAX_PARALLEL_CONNECTIONS; ctr++) {
            ConnectionImpl conn = obcache.get( cinfo ) ;
            conns.add( conn ) ;
        }
        Assert.assertEquals( conns.size(), MAX_PARALLEL_CONNECTIONS, 
            "Connections after add" ) ;
        checkStats( obcache, 0, 0, 4, 4 ) ;

        ConnectionImpl c1 = obcache.get( cinfo ) ;
        Assert.assertTrue( conns.contains( c1 ), 
            "Expect connection c1 is already in conns" ) ;
        checkStats( obcache, 0, 0, 4, 4 ) ;

        for (ConnectionImpl conn : conns ) {
            obcache.release( conn, 1 ) ;
        }
        checkStats( obcache, 3, 0, 1, 4 ) ;

        for (ConnectionImpl conn : conns ) {
            obcache.responseReceived( conn ) ;
        }
        checkStats( obcache, 3, 3, 1, 4 ) ;

        obcache.release( c1, 0 ) ;
        checkStats( obcache, 4, 4, 0, 4 ) ;

        for (ConnectionImpl conn : conns ) {
            obcache.close( conn ) ;
        }
        checkStats( obcache, 0, 0, 0, 0 ) ;
    }

    // Do enough gets on enough ContactInfos to start reclaiming
    @Test(dependsOnMethods={"nonBlockingConcurrentQueueTest", "outboundTest3"})
    public void outboundTest4() throws IOException {
        testBanner( "outboundTest4: test reclamation" ) ;
        final int numContactInfo = HIGH_WATER_MARK/MAX_PARALLEL_CONNECTIONS;
        final List<ContactInfoImpl> cinfos = new ArrayList<ContactInfoImpl>() ;
        for (int ctr=0; ctr<numContactInfo; ctr++) {
            cinfos.add( ContactInfoImpl.get( "ContactInfo" + ctr ) ) ;
        }
        final ContactInfoImpl overcinfo = 
            ContactInfoImpl.get( "OverflowContactInfo" ) ;

        // Open up HIGH_WATER_MARK total connections 
        List<HashSet<ConnectionImpl>> csa = 
            new ArrayList<HashSet<ConnectionImpl>>() ;
        for (int ctr=0; ctr<numContactInfo; ctr++) {
            HashSet<ConnectionImpl> set = new HashSet<ConnectionImpl>() ;
            csa.add( set ) ;
            for (int num=0; num<MAX_PARALLEL_CONNECTIONS; num++) {
                set.add( obcache.get( cinfos.get(ctr) ) ) ;
            }
        }

        checkStats( obcache, 0, 0, HIGH_WATER_MARK, HIGH_WATER_MARK ) ;

        // Now open up connection on so far unused ContactInfoImpl
        ConnectionImpl over = obcache.get( overcinfo ) ;
        checkStats( obcache, 0, 0, HIGH_WATER_MARK+1, HIGH_WATER_MARK+1 ) ;

        // Free the overflow connection, expecting a response
        obcache.release( over, 1 ) ;
        checkStats( obcache, 1, 0, HIGH_WATER_MARK, HIGH_WATER_MARK+1 ) ;

        // Get a response to free overflow conn: should close
        obcache.responseReceived( over ) ;
        checkStats( obcache, 0, 0, HIGH_WATER_MARK, HIGH_WATER_MARK ) ;
        
        // Again open up connection on so far unused ContactInfoImpl
        over = obcache.get( overcinfo ) ;
        checkStats( obcache, 0, 0, HIGH_WATER_MARK+1, HIGH_WATER_MARK+1 ) ;

        // Free the overflow connection, no response
        obcache.release( over, 0 ) ;
        checkStats( obcache, 0, 0, HIGH_WATER_MARK, HIGH_WATER_MARK ) ;

        // get overflow twice: should get same connection back second time
        ConnectionImpl over1 = obcache.get( overcinfo ) ;
        ConnectionImpl over2 = obcache.get( overcinfo ) ;
        checkStats( obcache, 0, 0, HIGH_WATER_MARK+1, HIGH_WATER_MARK+1 ) ;
        Assert.assertEquals( over1, over2, 
            "Connections from two overflow get calls" ) ;

        obcache.release( over2, 0 ) ;
        obcache.release( over1, 0 ) ;
        checkStats( obcache, 0, 0, HIGH_WATER_MARK, HIGH_WATER_MARK ) ;

        // Clean up everything: just close
        for (Set<ConnectionImpl> conns : csa ) {
            for (ConnectionImpl conn : conns) {
                obcache.close( conn ) ;
            }
        }
        checkStats( obcache, 0, 0, 0, 0 ) ;
    }

    // Test a ContactInfoImpl that throws an IOException
    @Test(dependsOnMethods={"nonBlockingConcurrentQueueTest", "outboundTest4"},
        expectedExceptions={IOException.class})
    public void outboundTest5() throws IOException {
        testBanner( "outboundTest5: test connection open error" ) ;
        ContactInfoImpl cinfo = ContactInfoImpl.get( "ExceptionTest" ) ;
        cinfo.setUnreachable( true ) ;
        try {
            obcache.get( cinfo ) ; // should throw an IOException
        } finally {
            checkStats( obcache, 0, 0, 0, 0 ) ;
            cinfo.setUnreachable( false ) ;
        }
    }
    
    private static <V> V getSecondOrFirst( Collection<V> coll ) {
        V first = null ;
        V second = null ;
        int count = 0 ;
        for (V v : coll) {
            if (count == 0) {
                first = v ;
            } else if (count == 1) {
                return v ;
            } else {
                break ;
            }
            count++ ;
        }
        return first ;
    }

    // Several tests for ConnectionFinders
    private static ConnectionFinder<ConnectionImpl> cf1 = 
        new ConnectionFinder<ConnectionImpl>() {
            public ConnectionImpl find( 
                ContactInfo<ConnectionImpl> cinfo,
                Collection<ConnectionImpl> idleConnections,
                Collection<ConnectionImpl> busyConnections 
                ) throws IOException {

                return getSecondOrFirst( idleConnections ) ;
            } 
        } ;

    private static ConnectionFinder<ConnectionImpl> cf2 = 
        new ConnectionFinder<ConnectionImpl>() {
            public ConnectionImpl find( 
                ContactInfo<ConnectionImpl> cinfo,
                Collection<ConnectionImpl> idleConnections,
                Collection<ConnectionImpl> busyConnections 
                ) throws IOException {

                return getSecondOrFirst( busyConnections ) ;
            } 
        } ;

    private static ConnectionFinder<ConnectionImpl> cf3 = 
        new ConnectionFinder<ConnectionImpl>() {
            public ConnectionImpl find( 
                ContactInfo<ConnectionImpl> cinfo,
                Collection<ConnectionImpl> idleConnections,
                Collection<ConnectionImpl> busyConnections 
                ) throws IOException {

                return cinfo.createConnection() ;
            } 
        } ;

    private static ConnectionFinder<ConnectionImpl> cf4 = 
        new ConnectionFinder<ConnectionImpl>() {
            public ConnectionImpl find( 
                ContactInfo<ConnectionImpl> cinfo,
                Collection<ConnectionImpl> idleConnections,
                Collection<ConnectionImpl> busyConnections 
                ) throws IOException {

                return null ;
            } 
        } ;

    @Test(dependsOnMethods={"nonBlockingConcurrentQueueTest", "outboundTest4"})
    private void outboundTest6() throws IOException {
        testBanner( "outboundTest6: test ConnectionFinder non-error case" ) ;
        ContactInfoImpl cinfo = ContactInfoImpl.get( "CFTest" ) ;

        // Set up 2 idle and 2 busy connections on cinfo
        ConnectionImpl idle1 = obcache.get( cinfo ) ;
        ConnectionImpl idle2 = obcache.get( cinfo ) ;
        ConnectionImpl busy1 = obcache.get( cinfo ) ;
        ConnectionImpl busy2 = obcache.get( cinfo ) ;
        obcache.release( idle1, 0 ) ;
        obcache.release( idle2, 0 ) ;
        checkStats( obcache, 2, 2, 2, 4 ) ;

        // Test cf1
        ConnectionImpl test = obcache.get( cinfo, cf1 ) ;
        Assert.assertEquals( test, idle2 ) ;
        checkStats( obcache, 1, 1, 3, 4 ) ;
        obcache.release( test, 0 ) ;
        checkStats( obcache, 2, 2, 2, 4 ) ;

        // Test cf2
        test = obcache.get( cinfo, cf2 ) ;
        Assert.assertEquals( test, busy2 ) ;
        checkStats( obcache, 2, 2, 2, 4 ) ;
        obcache.release( test, 0 ) ;
        checkStats( obcache, 2, 2, 2, 4 ) ;
        obcache.release( busy2, 0 ) ;
        checkStats( obcache, 3, 3, 1, 4 ) ;

        // Test cf3
        test = obcache.get( cinfo, cf3 ) ;
        checkStats( obcache, 3, 3, 2, 5 ) ;
        obcache.release( test, 0 ) ;
        checkStats( obcache, 4, 4, 1, 5 ) ;
        obcache.close( test ) ;
        checkStats( obcache, 3, 3, 1, 4 ) ;

        // Test cf4
        test = obcache.get( cinfo, cf4 ) ;
        checkStats( obcache, 2, 2, 2, 4 ) ;
        obcache.release( test, 0 ) ;
        checkStats( obcache, 3, 3, 1, 4 ) ;

        obcache.close( idle1 ) ;
        obcache.close( idle2 ) ;
        obcache.close( busy1 ) ;
        obcache.close( busy2 ) ;
        checkStats( obcache, 0, 0, 0, 0 ) ;
    }

    @Test( expectedExceptions = { IOException.class } )
    private void outboundTest7() throws IOException {
        testBanner( "outboundTest7: test ConnectionFinder error case" ) ;
        ContactInfoImpl cinfo = ContactInfoImpl.get( "CFTest" ) ;
        cinfo.setUnreachable( true ) ;
        try {
            ConnectionImpl test = obcache.get( cinfo, cf3 ) ;
        } finally {
            checkStats( obcache, 0, 0, 0, 0 ) ;
        }
    }

    // Test inboundConnectionCache
    //
    // Do a single requestReceived/requestProcessed/responseSent cycle
    @Test(dependsOnMethods={"nonBlockingConcurrentQueueTest"})
    public void inboundTest1() throws IOException {
        testBanner( "inboundTest1: single cycle" ) ;
        ContactInfoImpl cinfo = ContactInfoImpl.get( "FirstContact" ) ;
        ConnectionImpl c1 = cinfo.createConnection() ;
        ibcache.requestReceived( c1 ) ;
        checkStats( ibcache, 0, 0, 1, 1 ) ;

        ibcache.requestProcessed( c1, 1 ) ;
        checkStats( ibcache, 1, 0, 0, 1 ) ;

        ibcache.responseSent( c1 ) ;
        checkStats( ibcache, 1, 1, 0, 1 ) ;
        
        ibcache.close( c1 ) ;
        checkStats( ibcache, 0, 0, 0, 0 ) ;
    }

    // Do two interleaved requestReceived/requestProcessed/responseSent cycles
    @Test(dependsOnMethods={"nonBlockingConcurrentQueueTest", "inboundTest1"})
    public void inboundTest2() throws IOException {
        testBanner( "inboundTest2: 2 cycles interleaved" ) ;
        ContactInfoImpl cinfo = ContactInfoImpl.get( "FirstContact" ) ;
        ConnectionImpl c1 = cinfo.createConnection() ;
        ibcache.requestReceived( c1 ) ;
        checkStats( ibcache, 0, 0, 1, 1 ) ;

        ConnectionImpl c2 = cinfo.createConnection() ;
        ibcache.requestReceived( c2 ) ;
        checkStats( ibcache, 0, 0, 2, 2 ) ;

        Assert.assertNotSame( c1, c2) ;

        ibcache.requestProcessed( c1, 1 ) ;
        checkStats( ibcache, 1, 0, 1, 2 ) ;

        ibcache.requestProcessed( c2, 1 ) ;
        checkStats( ibcache, 2, 0, 0, 2 ) ;

        ibcache.responseSent( c1 ) ;
        checkStats( ibcache, 2, 1, 0, 2 ) ;
        
        ibcache.responseSent( c2 ) ;
        checkStats( ibcache, 2, 2, 0, 2 ) ;
        
        ibcache.close( c2 ) ;
        checkStats( ibcache, 1, 1, 0, 1 ) ;
        
        ibcache.close( c1 ) ;
        checkStats( ibcache, 0, 0, 0, 0 ) ;
    }

    // Do enough gets on enough ContactInfos to start reclaiming
    @Test(dependsOnMethods={"nonBlockingConcurrentQueueTest", "inboundTest2"})
    public void inboundTest3() throws IOException {
        testBanner( "inboundTest3: test reclamation" ) ;
        final int numContactInfo = HIGH_WATER_MARK/MAX_PARALLEL_CONNECTIONS;
        final List<ContactInfoImpl> cinfos = new ArrayList<ContactInfoImpl>() ;
        for (int ctr=0; ctr<numContactInfo; ctr++) {
            cinfos.add( ContactInfoImpl.get( "ContactInfo" + ctr ) ) ;
        }
        final ContactInfoImpl overcinfo = 
            ContactInfoImpl.get( "OverflowContactInfo" ) ;

        // Open up HIGH_WATER_MARK total connections 
        List<HashSet<ConnectionImpl>> csa = 
            new ArrayList<HashSet<ConnectionImpl>>() ;
        for (int ctr=0; ctr<numContactInfo; ctr++) {
            ContactInfoImpl cinfo = cinfos.get(ctr) ;
            HashSet<ConnectionImpl> set = new HashSet<ConnectionImpl>() ;
            csa.add( set ) ;
            for (int num=0; num<MAX_PARALLEL_CONNECTIONS; num++) {
                ConnectionImpl conn = cinfo.createConnection() ;
                ibcache.requestReceived( conn ) ;
                set.add( conn ) ;
            }
        }

        checkStats( ibcache, 0, 0, HIGH_WATER_MARK, HIGH_WATER_MARK ) ;

        // Now open up connection on so far unused ContactInfoImpl
        ConnectionImpl over = overcinfo.createConnection() ;
        ibcache.requestReceived( over ) ;
        checkStats( ibcache, 0, 0, HIGH_WATER_MARK+1, HIGH_WATER_MARK+1 ) ;

        // Free the overflow connection, expecting a response
        ibcache.requestProcessed( over, 1 ) ;
        checkStats( ibcache, 1, 0, HIGH_WATER_MARK, HIGH_WATER_MARK+1 ) ;

        // Get a response to free overflow conn: should close
        ibcache.responseSent( over ) ;
        checkStats( ibcache, 0, 0, HIGH_WATER_MARK, HIGH_WATER_MARK ) ;
        
        // Again open up connection on so far unused ContactInfoImpl
        over = overcinfo.createConnection() ;
        ibcache.requestReceived( over ) ;
        checkStats( ibcache, 0, 0, HIGH_WATER_MARK+1, HIGH_WATER_MARK+1 ) ;

        // Free the overflow connection, no response
        ibcache.requestProcessed( over, 0 ) ;
        checkStats( ibcache, 0, 0, HIGH_WATER_MARK, HIGH_WATER_MARK ) ;

        // add overflow twice
        ConnectionImpl over1 = overcinfo.createConnection() ;
        ibcache.requestReceived( over1 ) ;
        ibcache.requestReceived( over1 ) ;
        checkStats( ibcache, 0, 0, HIGH_WATER_MARK+1, HIGH_WATER_MARK+1 ) ;

        ibcache.requestProcessed( over1, 0 ) ;
        ibcache.requestProcessed( over1, 0 ) ;
        checkStats( ibcache, 0, 0, HIGH_WATER_MARK, HIGH_WATER_MARK ) ;

        // Clean up everything: just close
        for (Set<ConnectionImpl> conns : csa ) {
            for (ConnectionImpl conn : conns) {
                ibcache.close( conn ) ;
            }
        }
        checkStats( ibcache, 0, 0, 0, 0 ) ;
    }

    public static void main( String[] args ) {
        TestngRunner runner = new TestngRunner() ;
        runner.registerClass( Client.class ) ;
        runner.run() ;
        runner.systemExit() ;
    }
}
