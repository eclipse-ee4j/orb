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

package corba.misc ;

import com.sun.corba.ee.impl.misc.ORBUtility;
import com.sun.corba.ee.spi.misc.ORBConstants;
import com.sun.corba.ee.spi.oa.rfm.ReferenceFactory;
import com.sun.corba.ee.spi.oa.rfm.ReferenceFactoryManager;
import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.presentation.rmi.PresentationManager;
import com.sun.corba.ee.spi.trace.CdrRead;
import com.sun.corba.ee.spi.trace.CdrWrite;
import com.sun.corba.ee.spi.trace.PrimitiveRead;
import com.sun.corba.ee.spi.trace.PrimitiveWrite;
import com.sun.corba.ee.spi.trace.ValueHandlerRead;
import com.sun.corba.ee.spi.trace.ValueHandlerWrite;
import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;
import org.glassfish.pfl.basic.logex.OperationTracer;
import org.glassfish.pfl.test.TestCaseTools;
import org.glassfish.pfl.tf.spi.MethodMonitorFactory;
import org.glassfish.pfl.tf.spi.MethodMonitorFactoryDefaults;
import org.glassfish.pfl.tf.spi.MethodMonitorRegistry;
import org.glassfish.pfl.tf.timer.spi.TimerFactory;
import org.glassfish.pfl.tf.timer.spi.TimerFactoryBuilder;
import org.omg.CORBA.Any;
import org.omg.CORBA.LocalObject;
import org.omg.CORBA.Policy;
import org.omg.CORBA_2_3.portable.InputStream;
import org.omg.CORBA_2_3.portable.OutputStream;
import org.omg.PortableServer.ForwardRequest;
import org.omg.PortableServer.ForwardRequestHelper;
import org.omg.PortableServer.IdAssignmentPolicyValue;
import org.omg.PortableServer.IdUniquenessPolicyValue;
import org.omg.PortableServer.ImplicitActivationPolicyValue;
import org.omg.PortableServer.LifespanPolicyValue;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAPackage.ObjectNotActive;
import org.omg.PortableServer.RequestProcessingPolicyValue;
import org.omg.PortableServer.Servant;
import org.omg.PortableServer.ServantLocator;
import org.omg.PortableServer.ServantLocatorPackage.CookieHolder;
import org.omg.PortableServer.ServantRetentionPolicyValue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.nio.ByteBuffer;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import javax.rmi.CORBA.Tie;
import javax.rmi.PortableRemoteObject;

//import com.sun.corba.ee.impl.orbutil.newtimer.generated.TimingPoints ;

public class Client extends TestCase
{
    static final String[][] idInfo = {
        { "", "orb" },
        { "A", "orb_A_1" },
        { "", "orb__2" },
        { "A", "orb_A_2" },
        { "B", "orb_B_1" } } ;

    static ORB orb ;
    static ORB[] orbs ;

    // An extension of the TestSetup test decorator (that is,
    // interceptor) that is used to initialize ORBs before the
    // tests start, and clean the ORBs up after the test completes.
    // Also sets up the tester remote object.
    private static class ORBManager extends TestSetup
    {
        public ORBManager( Test test ) 
        {
            super( test ) ;
        }

        @Override
        public void setUp()
        {
            orbs = new ORB[idInfo.length] ;
            for (int ctr=0; ctr<idInfo.length; ctr++) {
                orbs[ctr] = makeORB( idInfo[ctr][0] ) ;
                if (ctr==0) {
                    orb = orbs[ctr];
                }
            }
        }

        private ORB makeORB( String id ) 
        {
            Properties props = new Properties() ;
            props.setProperty( "org.omg.CORBA.ORBClass",
                "com.sun.corba.ee.impl.orb.ORBImpl" ) ;
            props.setProperty( "org.omg.CORBA.ORBId", id ) ;
            return (ORB)ORB.init( new String[0], props ) ;
        }

        @Override
        public void tearDown()
        {
            for (int ctr=0; ctr<orbs.length; ctr++) {
                orbs[ctr].destroy();
            }
            // System.out.println( "TimerFactories after ORB destruction:" ) ;
            // displayTimerFactories() ;
        }
    }

    static boolean debug = false ;

    public static void main( String[] args ) 
    {
        debug = (args.length>0) && args[0].equals( "-debug" ) ;

        Client root = new Client() ;
        TestResult result = junit.textui.TestRunner.run(Client.suite()) ;

        if (result.errorCount() + result.failureCount() > 0) {
            System.out.println( "Error: failures or errrors in JUnit test" ) ;
            System.exit( 1 ) ;
        } else {
            System.exit(0);
        }
    }

    public Client()
    {
        super() ;
    }

    public Client( String name )
    {
        super( name ) ;
    }

    public static Test suite()
    {
        System.out.println( 
            "==============================================================\n" +
            "Miscellaneous CORBA Tests\n" +
            "==============================================================\n" 
        ) ;

        // TestSuite created only to include the ORBManager setup wrapper,
        // which wraps the real TestSuite made from this class.
        // This causes the ORBs for this test to be created before all
        // tests run, and destroyed after all tests are completed.  
        TestSuite main = new TestSuite( "main" ) ;
        TestSuite ts = TestCaseTools.makeTestSuite( Client.class ) ;
        main.addTest( new ORBManager( ts ) ) ;
        return main ;
    }

    /** Test for bug 4919770: multiple ORBs share the same root MonitoredObject.
      *
    public void testMonitoringRootName()
    {
        for (int ctr=0; ctr<idInfo.length; ctr++) {
            String rootName = 
                orbs[ctr].getMonitoringManager().getRootMonitoredObject().getName() ;
            assertEquals( rootName, idInfo[ctr][1] ) ;
        }
    }
    */

/*
    // Test for bug 6177606: incorrect error handling for string_to_object.
    public static class NamingTestSuite extends TestCase
    {
        public NamingTestSuite()
        {
            super() ;
        }

        public NamingTestSuite( String name ) 
        {
            super( name ) ;
        }

        // Check that expected BAD_PARAM exception is thrown.
        private void expectException( String url, int mc )
        {
            try {
                orb.string_to_object( url ) ;
                fail( "Expected BAD_PARAM exception but instead did not throw exception" ) ;
            } catch (BAD_PARAM bp) {
                assertEquals( bp.minor, mc ) ;
            } catch (Exception exc) {
                fail( "Expected BAD_PARAM exception but got " + exc ) ;
            }
        }

        public void testSoBadSchemeName()
        {
            expectException( "foo:a_very_bad_url", 
                OMGSystemException.SO_BAD_SCHEME_NAME ) ;
        }

        public void testSoBadAddress()
        {
            expectException( "corbaloc:/another_bad_url", 
                OMGSystemException.SO_BAD_ADDRESS ) ;
        }

        public void testSoBadSchemaSpecific() 
        {
            expectException( "corbaname:iiop:1.2@localhost:49832#ABadCosName", 
                OMGSystemException.SO_BAD_SCHEMA_SPECIFIC ) ;
        }
    }
    */


    private static int counter = 1 ;

    private static synchronized int getCounter()
    {
        int result = counter++ ;
        if (counter > Byte.MAX_VALUE) {
            counter = 1;
        }
        return result ;
    }

    private static final Random gen = new Random() ;

    private static final int BUF_SIZE = 160 ;
    private static final int REP_COUNT = 10 ;
    private static final int NUM_THREADS = 5 ;

    private ByteBuffer getBuffer( int val, int len ) 
    {
        byte[] buff = new byte[len] ;
        for (int ctr=0; ctr<len; ctr++) {
            buff[ctr] = (byte) val;
        }
        ByteBuffer result = ByteBuffer.wrap( buff ) ;
        result.position( len ) ;
        return result ;
    }

    private class Printer extends Thread 
    {
        private int threadId ;
        private PrintStream ps ;

        public Printer( int threadId, PrintStream ps )
        {
            this.threadId = threadId ;
            this.ps = ps ;
        }

        private int getSleepTime() 
        {
            return 10 + gen.nextInt( 100 ) ;
        }

        @Override
        public void run() 
        {
            for (int ctr=0; ctr<REP_COUNT; ctr++) {
                ByteBuffer bb = getBuffer( getCounter(), BUF_SIZE ) ;

                try {
                    sleep( getSleepTime() ) ;
                } catch (InterruptedException ie) {
                    // ignore this; just trying to catch a little sleep here
                }

                ORBUtility.printBuffer( "Printer Thread " + threadId, bb, ps ) ;
            }
        }
    }

    /** Have 5 threads print 10 different buffers each with random delays.
     */
    public void testPrintBuffer()
    {
        ByteArrayOutputStream os = new ByteArrayOutputStream() ;
        PrintStream ps = new PrintStream( os ) ;

        counter = 1 ;
        Printer[] printers = new Printer[NUM_THREADS] ;
        for (int ctr = 0; ctr<NUM_THREADS; ctr++) {
            printers[ctr] = new Printer( ctr, ps ) ;
            printers[ctr].start() ;
        }

        for (int ctr=0; ctr<NUM_THREADS; ctr++) {
            try {
                printers[ctr].join() ;
            } catch (InterruptedException ie) {
                // ignore this
            }
        }

        ps.close() ;
        byte[] data = os.toByteArray() ; 
    }

    public void testBrooksPOAActivationProblem() {
        try {
            POA rootPOA = (POA)orb.resolve_initial_references( "RootPOA" ) ;

            // Create POA in RETAIN USE_AOM USER_ID mode
            Policy[] policies = new Policy[] {
                rootPOA.create_servant_retention_policy( 
                    ServantRetentionPolicyValue.RETAIN ),
                rootPOA.create_request_processing_policy( 
                    RequestProcessingPolicyValue.USE_ACTIVE_OBJECT_MAP_ONLY ),
                rootPOA.create_id_assignment_policy( 
                    IdAssignmentPolicyValue.USER_ID ),
                rootPOA.create_implicit_activation_policy( 
                    ImplicitActivationPolicyValue.NO_IMPLICIT_ACTIVATION ),
                rootPOA.create_lifespan_policy( 
                    LifespanPolicyValue.TRANSIENT ), 
                rootPOA.create_id_uniqueness_policy( 
                    IdUniquenessPolicyValue.UNIQUE_ID ) 
            } ;

            POA myPOA = rootPOA.create_POA( "TestPOA", null, policies ) ;
            myPOA.the_POAManager().activate() ;

            // Activate an object with id1 (don't need a real servant for this)
            Servant servant = new Servant() {
                public String[] _all_interfaces( POA poa, byte[] objectId ) {
                    return new String[0] ;
                }
            } ;
            byte[] oid = new byte[] { 1, 2, 3, 4 } ;
            myPOA.activate_object_with_id( oid, servant ) ;

            // Deactivate the object
            myPOA.deactivate_object( oid ) ;

            // Call idToServant on id1
            boolean expectedException = true ;
            try {
                // This should fail with an ObjectNotActive exception 
                Servant servant2 = myPOA.id_to_servant( oid ) ;
            } catch (ObjectNotActive exc) {
                // this is expected
                expectedException = true ;
            } catch (Exception exc) {
                fail( "Unexpected exception on id_to_servant: " + exc ) ;
            }
            if (!expectedException) {
                fail("id_to_servant did not throw excepted exception");
            }

            // Try to Activate again: Failure?
            myPOA.activate_object_with_id( oid, servant ) ;

            // Destroy POA
            myPOA.destroy( false, true ) ;
        } catch (Exception exc) {
            exc.printStackTrace() ;
            fail( "Failed with exception: " + exc ) ;
        }
    }

/*
    // This test is intended to look for ORB shutdown problems
    // related to POAManager activation.  However, it does not perform
    // the intended test, because the syncronization window in ORB.shutdown
    // is too small to test reliably.  Some major changes in ORB
    // synchronization are required to fix the problem (see bug 6191561).

    // Test the following sequence of events:
    // set up POA with POAManager
    // Leave POAManager in holding state
    // Start a thread that blocks on POAManager.enter
    // Call orb.shutdown.  Does it hang?
    public void testPOAManagerAndORBShutdown() {
        try {
            Properties props = new Properties() ;
            props.setProperty( "org.omg.CORBA.ORBClass",
                "com.sun.corba.ee.impl.orb.ORBImpl" ) ;
            props.setProperty( "org.omg.CORBA.ORBId", "25678891" ) ;
            ORB lorb = (ORB)ORB.init( new String[0], props ) ;

            Properties props = new Properties() ;
            props.setProperty( "org.omg.CORBA.ORBClass",
                "com.sun.corba.ee.impl.orb.ORBImpl" ) ;
            props.setProperty( "org.omg.CORBA.ORBId", "25678891" ) ;
            ORB lorb = (ORB)ORB.init( new String[0], props ) ;

            POA rootPOA = (POA)lorb.resolve_initial_references( "RootPOA" ) ;

            // Create POA in RETAIN USE_AOM USER_ID mode
            Policy[] policies = new Policy[] {
                rootPOA.create_servant_retention_policy( 
                    ServantRetentionPolicyValue.RETAIN ),
                rootPOA.create_request_processing_policy( 
                    RequestProcessingPolicyValue.USE_ACTIVE_OBJECT_MAP_ONLY ),
                rootPOA.create_id_assignment_policy( 
                    IdAssignmentPolicyValue.USER_ID ),
                rootPOA.create_implicit_activation_policy( 
                    ImplicitActivationPolicyValue.NO_IMPLICIT_ACTIVATION ),
                rootPOA.create_lifespan_policy( 
                    LifespanPolicyValue.TRANSIENT ), 
                rootPOA.create_id_uniqueness_policy( 
                    IdUniquenessPolicyValue.UNIQUE_ID ) 
            } ;

            final POAImpl myPOA = (POAImpl)(rootPOA.create_POA( "ShutdownTestPOA", null, 
                policies )) ;
            new Thread( new Runnable() {
                public void run() {
                    try {
                        myPOA.enter() ;
                    } catch (OADestroyed oad) {
                        System.out.println( "Caught OADestroyed!" ) ;
                        return ;
                    }
                    System.out.println( "Did not catch OADestroyed!" ) ;
                }
            } ).start() ;

            System.out.println( "ORB shutdown starts..." ) ;

            lorb.shutdown( true ) ;

            System.out.println( "ORB shutdown completed" ) ;
        } catch (Exception exc) {
            exc.printStackTrace() ;
            fail( "Failed with exception: " + exc ) ;
        }
    }
*/
    // Various tests for enum marshaling
    //

    public enum TestEnum {
        HELLO() {
            @Override
            public String message() {
                return "hello";
            }

        },
        BYE() {
            @Override
            public String message() {
                return "bye";
            }
        };
        abstract String message();
    }

    public enum Color { RED, BLUE, GREEN } ;

    public enum Coin {
        QUARTER( 25 ), 
        DIME( 10 ), 
        NICKEL( 5 ), 
        PENNY( 1 ) ;

        int value() {
            return value ;
        }

        private int value ;

        Coin( int value ) {
            this.value = value ;
        }
    }

    private static final List<Class<? extends Annotation>> ioannos =
        new ArrayList<Class<? extends Annotation>>() ;

    static {
        ioannos.add( CdrRead.class ) ;
        ioannos.add( CdrWrite.class ) ;
        ioannos.add( PrimitiveRead.class ) ;
        ioannos.add( PrimitiveWrite.class ) ;
        ioannos.add( ValueHandlerRead.class ) ;
        ioannos.add( ValueHandlerWrite.class ) ;
    }

    private void traceOn() {
        MethodMonitorFactory mmf = MethodMonitorFactoryDefaults.dprint() ;
        for (Class<? extends Annotation> cls : ioannos) {
            MethodMonitorRegistry.register( cls, mmf ) ;
        }
    }

    private void traceOff() {
        for (Class<? extends Annotation> cls : ioannos) {
            MethodMonitorRegistry.register( cls, null ) ;
        }
    }

    private static class RRTest implements Serializable {
        private int value ;

        public static RRTest rr1 = new RRTest( 1 ) ;
        public static RRTest rr2 = new RRTest( 2 ) ;

        public RRTest( int val ) {
            value = val ;
        }

        public int getValue() {
            return value ;
        }

        @Override
        public boolean equals( Object obj ) {
            if (obj == this) {
                return true;
            }

            if (obj instanceof RRTest) {
                RRTest other = (RRTest)obj ;
                return value == other.getValue() ;
            } else {
                return false ;
            }
        }

        public int hashCode() {
            return value ;
        }

        private Object readResolve() {
            if (value == 1) {
                return rr1;
            } else if (value == 2) {
                return rr2;
            } else {
                return this;
            }
        }
    }

    public void testReadResolve() {
        RRTest rr1_1 = new RRTest( 1 ) ;
        RRTest rr1_2 = new RRTest( 1 ) ;
        RRTest rr2_1 = new RRTest( 2 ) ;
        RRTest rr2_2 = new RRTest( 2 ) ;
        RRTest rr3_1 = new RRTest( 3 ) ;
        RRTest rr3_2 = new RRTest( 3 ) ;

        RRTest[] data = new RRTest[] {
            rr1_1, rr2_1, rr1_1, rr1_2, rr2_2, rr2_2, 
            rr3_1, rr3_2 } ;

        // Each element of result is either == the corresponding element of
        // expected, unless expected is null, then the result must have value 3.
        RRTest[] expected = new RRTest[] {
            RRTest.rr1, RRTest.rr2, RRTest.rr1, RRTest.rr1, RRTest.rr2, RRTest.rr2,
            null, null } ;

        ORB orb = null ;

        try {
            String[] args = new String[0] ;
            Properties props = new Properties() ;
            orb = ORB.class.cast( ORB.init( args, props ) ) ;

            OutputStream os = OutputStream.class.cast( orb.create_output_stream() ) ;
            os.write_value( data ) ;

            InputStream is = InputStream.class.cast( os.create_input_stream() ) ;
            RRTest[] result = (RRTest[])is.read_value() ;

            for (int ctr=0; ctr<result.length; ctr++) {
                RRTest exp = expected[ctr] ;
                RRTest res = result[ctr] ;
                if (exp == null) {
                    assertEquals( res.getValue(), 3 ) ;
                } else {
                    assertSame( exp, res ) ;
                }
            }
        } finally {
            orb.destroy() ;
        }
    }

    // We expected that the contactInfoListIteratorNext durations will start at near 0,
    // then follow the expectedInitiailTimeout/expectedBackoff pattern. 
    // The total time is only approximate, because the last (and longest) wait time
    // before a timeout can cause the wait time to be longer than the max time.
    // To figure this out, we need a little math:
    // What we have here is a geometric progression, with a=expectedInitialTimeout 
    // and r=expectedBackoff/100.  Note that a>0 and r>1 (by configuration).
    // Let S(a,n)= Sum(k-0,k=n) a*r^k (the standard geometric progression.
    // The we know (by the usual multiply Sn by r argument) that S(n)=a * (r^(n+1)-1)/(r-1).
    // But more importantly here the geometric series satifies the equation
    // S(a,n+1) = r*S(a,n) + a.  So, if we know that S(n)<M (the max time), then
    // S(a,n+1) = r*S(a,n) + a < r*M + a.  Therefore the entire timeout should
    // complete in at most (backoff/100)*maxWait+initialTimeout milliseconds.
    // The test will also allow an error of 50% for other computation, GC,
    // and other unpredicatable events.  Also note that the first call to Next
    // will take some non-zero time less than the initialTimeout.
    // All time arguments are in milliseconds; the time in the timer is in nanoseconds.
    /*
    void validateLogEvents( final ORB orb, final LogEventHandler leh, 
        final int initialTimeout, final int maxWait, final int backoff ) {

        final TimerManager<TimingPoints> tm = orb.makeTimerManager(
            TimingPoints.class ) ;
        final TimingPoints tp = tm.points() ;
        final Timer nextTime = tp.ContactInfoListIteratorImpl__next() ;

        // per-event data (all events)
        boolean firstEvent = true ;
        long startTime = 0 ;

        // per-next event data (only nextTime events)
        boolean firstNextEvent = true ;
        long currentWait = initialTimeout ; // each wait after first must be in [c,1.1*c]
        long currentTime = 0 ; // duration of current nextTime event in milliseconds

        for (TimerEvent te : leh) {
            if (firstEvent) {
                startTime = te.time() ;
                firstEvent = false ;
            }

            if (te.timer() == nextTime) {
                if (te.type() == TimerEvent.TimerEventType.ENTER) {
                    currentTime = te.time() ;
                } else { // EXIT
                    final long duration = (te.time()-currentTime)/1000000 ;

                    if (firstNextEvent) {
                        assertTrue( duration <= 1 ) ; // assume first wait is <= 1 msec.

                        firstNextEvent = false ;
                    } else {
                        assertTrue( "Expected duration " + duration 
                            + " to be at least " + currentWait, duration >= currentWait ) ;

                        final long upperBound = (currentWait * 150)/100 ;
                        assertTrue( "Expected duration " + duration 
                            + " to be less than " + upperBound, duration < upperBound ) ;

                        currentWait = (backoff * currentWait)/100 ;
                    }
                }
            }

            currentTime = te.time() ; // keep track of last time for overall duration
        }

        // Check that overall duration is within range.
        final long totalTime = (currentTime - startTime)/1000000 ;

        assertTrue( totalTime > maxWait ) ;
        assertTrue( totalTime < ((backoff*maxWait)/100 + initialTimeout) ) ;
    } */

    // Create a corbaloc URL that points to nowhere, then attempt to 
    // narrow and invoke on it.  This will fail, but we are interested
    // in studying the ORB retry behavior.
    /*
    public void testConnectionFailure( boolean useSticky ) {
        final String url = "corbaloc:iiop:1.2@ThisHostDoesNotExist:5555/NameService" ;

        final Properties props = new Properties() ;
        props.setProperty( "org.omg.CORBA.ORBClass",
            "com.sun.corba.ee.impl.orb.ORBImpl" ) ;

        if (useSticky) {
            props.setProperty( 
                ORBConstants.IIOP_PRIMARY_TO_CONTACT_INFO_CLASS_PROPERTY,
                IIOPPrimaryToContactInfoImpl.class.getName() ) ;
        }

        final int expectedInitialTimeout = 50 ;
        final int expectedMaxWait = 2000 ;
        final int expectedBackoff = 100 ;
        final String timeoutString = expectedInitialTimeout + ":" 
            + expectedMaxWait + ":" + expectedBackoff ;

        props.setProperty( ORBConstants.TRANSPORT_TCP_CONNECT_TIMEOUTS_PROPERTY, 
            timeoutString ) ;
        props.setProperty( ORBConstants.TIMING_POINTS_ENABLED, "true" ) ;
        final ORB orb = (ORB)ORB.init( new String[0], props ) ;

        final TcpTimeouts timeouts = orb.getORBData().getTransportTcpConnectTimeouts() ;
        assertEquals( timeouts.get_initial_time_to_wait(), expectedInitialTimeout ) ;
        assertEquals( timeouts.get_max_time_to_wait(),     expectedMaxWait ) ;
        assertEquals( timeouts.get_backoff_factor(),       expectedBackoff + 100 ) ;
        
        TimerManager<TimingPoints> tm = orb.makeTimerManager(
            TimingPoints.class ) ;
        LogEventHandler leh = tm.factory().makeLogEventHandler( "ContactInfoListIterator" ) ;
        tm.controller().register( leh ) ;
        TimingPoints tp = tm.points() ;
        TimerGroup cili = tm.factory().makeTimerGroup( "cili", 
            "TimerGroup for ContactInfoListIteratorImpl" ) ;
        cili.add( tp.ContactInfoListIteratorImpl__hasNext() ) ;
        cili.add( tp.ContactInfoListIteratorImpl__next() ) ;
        cili.add( tp.ContactInfoListIteratorImpl__reportException() ) ;
        cili.add( tp.ContactInfoListIteratorImpl__reportAddrDispositionRetry() ) ;
        cili.add( tp.ContactInfoListIteratorImpl__reportRedirect() ) ;
        cili.add( tp.ContactInfoListIteratorImpl__reportSuccess() ) ;

        try {
            cili.enable() ;
            org.omg.CORBA.Object obj = orb.string_to_object( url ) ;
            NamingContextExt nc = NamingContextExtHelper.narrow( obj ) ;
            nc.resolve_str( "this/does/not/exist" ) ;
            cili.disable() ;

        } catch (Exception exc) {
            if (debug) {
                System.out.println( "Received exception: " ) ;
                exc.printStackTrace() ;
            }
        } finally {
            if (debug) {
                leh.display( System.out, "Connection timer log contents" ) ;
                validateLogEvents( orb, leh, expectedInitialTimeout, expectedMaxWait,
                    expectedBackoff+100 ) ;
                orb.destroy() ;
            }
        }
    }


    public void DONTRUNtestConnectionFailureWithStickyManager() {
        testConnectionFailure( true ) ;
    }

    public void DONTRUNtestConnectionFailureWithoutStickyManager() {
        testConnectionFailure( false ) ;
    }
    */

    private static void displayTimerFactories() {
        System.out.println( "Current TimerFactory instances:" ) ;
        for (TimerFactory tf : TimerFactoryBuilder.contents()) {
            System.out.println( "\t" + tf ) ;
        }
    }

    private static TimerFactory findTimerFactoryForORB( String orbId ) {
        for (TimerFactory tf : TimerFactoryBuilder.contents()) {
            if (tf.name().contains( orbId )) {
                return tf;
            }
        }

        return null ;
    }


    /* TODO rewrite this test so it doesn't need the enhance functionality. - REG
    public void testORBInit() {
        final String orbId = "OrbOne" ;
        Properties props = new Properties() ;
        props.setProperty( "org.omg.CORBA.ORBClass",
            "com.sun.corba.ee.impl.orb.ORBImpl" ) ;
        props.setProperty( "org.omg.CORBA.ORBId", orbId ) ;
        ORB lorb = null ;
        
        for (int ctr=0; ctr<2; ctr++) {
            lorb = (ORB)ORB.init( new String[0], props ) ;
            // If we don't create a TimerManager, there won't be a 
            // TimerFactory.
            TimerManager<TimingPoints> tm =
                lorb.makeTimerManager( TimingPoints.class ) ;

            // displayTimerFactories() ;
            assertTrue( findTimerFactoryForORB(orbId) != null ) ;

            lorb.destroy() ;
            // displayTimerFactories() ;
            tm.destroy() ; // ORB.destroy won't clean this up!
            assertTrue( findTimerFactoryForORB(orbId) == null ) ;
        }
    }   */

    private void print2( String actual, String expected ) {
        assertEquals( actual, expected ) ;
        // System.out.println( "actual=" + actual + ",expected=" + expected ) ;
    }

    public void testOperationTracer() {
        System.out.println( "Testing OperationTracer" ) ;
        long time = System.currentTimeMillis() ;
        OperationTracer.enable() ;
        OperationTracer.finish() ;
        print2( OperationTracer.getAsString(), "" ) ;
        OperationTracer.begin( "Initial" ) ;
        print2( OperationTracer.getAsString(), "Initial:" ) ;
        OperationTracer.startReadValue( "Foo" ) ;
        print2( OperationTracer.getAsString(), "Initial:Foo" ) ;
        OperationTracer.readingField( "a" ) ;
        print2( OperationTracer.getAsString(), "Initial:Foo.a" ) ;
        OperationTracer.readingField( "b" ) ;
        print2( OperationTracer.getAsString(), "Initial:Foo.b" ) ;
        OperationTracer.endReadValue() ;
        print2( OperationTracer.getAsString(), "Initial:" ) ;
        OperationTracer.startReadArray( "Bar", 27 ) ;
        print2( OperationTracer.getAsString(), "Initial:Bar<27>" ) ;
        OperationTracer.readingIndex( 0 ) ;
        print2( OperationTracer.getAsString(), "Initial:Bar<27>[0]" ) ;
        OperationTracer.readingIndex( 1 ) ;
        print2( OperationTracer.getAsString(), "Initial:Bar<27>[1]" ) ;
        OperationTracer.startReadValue( "Baz" ) ;
        print2( OperationTracer.getAsString(), "Initial:Bar<27>[1],Baz" ) ;
        OperationTracer.readingField( "x1" ) ;
        print2( OperationTracer.getAsString(), "Initial:Bar<27>[1],Baz.x1" ) ;
        OperationTracer.endReadValue() ;
        print2( OperationTracer.getAsString(), "Initial:Bar<27>[1]" ) ;
        OperationTracer.endReadArray() ;
        print2( OperationTracer.getAsString(), "Initial:" ) ;
        OperationTracer.finish() ;
        print2( OperationTracer.getAsString(), "" ) ;
        double elapsed = (time - System.currentTimeMillis())/1000 ;
        System.out.println( 
            "OperationTracer test complete in " + elapsed + " milliseconds" ) ;
    }

    public void test5161() {
        System.out.println( "Running test for issue 5161" ) ;
        BuckPasserAL bpal = new BuckPasserAL() ;
        bpal.add( new Buck( "The Buck" ) ) ;
        BuckPasserV bpv = new BuckPasserV() ;
        bpv.add( new Buck( "The Buck" ) ) ;

        OutputStream out = (OutputStream)orb.create_output_stream();

        out.write_value(bpal) ;
        out.write_value(bpv) ;

        InputStream in = (InputStream)out.create_input_stream();

        BuckPasserAL bpal2 = (BuckPasserAL)in.read_value() ;
        BuckPasserV bpv2 = (BuckPasserV)in.read_value() ;

        assertTrue( bpal2.equals( bpal ) ) ;
        assertTrue( bpv2.equals( bpv ) ) ;
    }

    public void testClassMarshaling() {
        System.out.println( "Running test for serialization of primitive classes" ) ;

        Object[] arr = {
            boolean.class,
            byte.class,
            Byte.class,
            short.class,
            int.class,
            float.class,
            long.class,
            double.class,
            char.class,
            this.getClass() 
        } ;

        OutputStream out = (OutputStream)orb.create_output_stream();
        out.write_value( arr ) ;
        InputStream in = (InputStream)out.create_input_stream() ;
        Object[] result = (Object[])in.read_value() ;

        int errorCount = 0 ;
        for (int ctr=0; ctr<arr.length; ctr++) {
            if (!arr[ctr].equals( result[ctr] )) {
                System.out.printf( "Error: expected class %s but read %s\n",
                    arr[ctr].toString(), result[ctr].toString() ) ;
                errorCount++ ;
            }
        }

        if (errorCount > 0) {
            fail( "Class marshaling test failed with " + errorCount + " errors" ) ;
        }
    }

    public static abstract class WebContent implements Serializable {
        long id = 42 ;

        java.util.Set extractFieldSet = null ;
        java.util.Set linkSet = null ;
        Object stats = null ;
        java.lang.Long wcmsNodeId = Long.valueOf(716368 ) ;
        java.lang.String wcmsPath = "/tag/destination/US/CA/075/san-francisco.xml" ;

        private final void writeObject(java.io.ObjectOutputStream os ) throws IOException {
            os.defaultWriteObject() ;
        }
    }

    public static class Trip extends WebContent {
        int clientId = 2 ;
        long id = 23900 ;
        boolean isActive = false ;
        int lengthDays = 12 ;
        int version = 232 ;
        char visibilityCode = 'A' ;

        Date createData = new Date() ;
        String description = "a description of the trip" ;
        Destination dest = new Destination() ;
        String destinationText = "some airport and hotel" ;
        String name = "John Doe" ;
        Object member = null ;
        Object stats = null ;
        Date startDate = new Date() ;
        Date updatedDate = new Date() ;
        Object updatedBy = null ;

        private final void writeObject( ObjectOutputStream os ) throws IOException {
           os.defaultWriteObject() ;
        }
    }
    
    public static class Destination implements Serializable {
        int typeCode = 4 ;
        Object airport = null ;
        String featuresCode = "a Feature" ;
        String fullName = "Full name" ;
        Point geoPoint = new Point() ;
        String name = "name" ;

        private final void writeObject( ObjectOutputStream os ) throws IOException {
            os.defaultWriteObject() ;
        }
    }

    public static class Geometry implements Serializable {
        int dimension = 2 ;
        boolean haveMeasure = true ;
        int srid ;
        int type = 1 ;
    }

    public static class Point extends Geometry {
        double m = 1.0 ;
        double x = -23.343 ;
        double y = 102.2325446 ;
        double z = 100.23 ;
    }

    public void testTrip() {
        try {
            System.out.println( "test case testTrip" ) ;          
            Trip trip = new Trip() ;

            OutputStream os = (OutputStream)orb.create_output_stream() ;
            os.write_value( trip ) ;
            InputStream is = (InputStream)os.create_input_stream() ;
            Trip newTrip = (Trip)is.read_value() ;
            //Assert.assertEquals( lid, newLid ) ;
        } catch (Exception exc) {
            exc.printStackTrace() ;
            fail( exc.toString() ) ;
        }
    }

    private interface ETest extends Remote {
        int echo( int arg ) throws RemoteException ;
    }
    
    private static class ETestImpl extends PortableRemoteObject implements ETest {
        ETestImpl() throws RemoteException {
            super() ;
        }

        public int echo( int arg ) { return arg ; }
    }

    private static class TestServantLocator extends LocalObject
        implements ServantLocator {

        private final ORB orb ;
        private final Servant servant ;

        public TestServantLocator( ORB orb ) {
            this.orb = orb ;
            ETestImpl impl = null ;
            try {
                impl = new ETestImpl() ;
            } catch (Exception exc) {
                // do nothing
            }
            Tie tie = orb.getPresentationManager().getTie() ;
            tie.setTarget( impl ) ;
            servant = Servant.class.cast( tie ) ;
        }

        public synchronized Servant preinvoke( byte[] oid, POA adapter,
            String operation, CookieHolder the_cookie ) throws ForwardRequest {
            return servant ;
        }

        public void postinvoke( byte[] oid, POA adapter,
            String operation, Object the_cookie, Servant the_servant ) {
        }
    }

    public void testTypeCode() {
        System.out.println( "Test case testTypeCode" ) ;
        try {
            final String[] args = new String[0] ;
            final Properties props = new Properties() ;
            props.setProperty( ORBConstants.RFM_PROPERTY, "1" ) ;
            props.setProperty( ORBConstants.USE_DYNAMIC_STUB_PROPERTY, "true" ) ;
            props.setProperty( ORBConstants.ORB_SERVER_ID_PROPERTY,
                "300" ) ;
            props.setProperty( ORBConstants.PERSISTENT_SERVER_PORT_PROPERTY,
                "3755" ) ;
            orb = ORB.class.cast( ORB.init( args, props ) ) ;

            // Just get some object referece for testing
            final ServantLocator locator = new TestServantLocator( orb ) ;

            ReferenceFactoryManager rfm = null ;

            try {
                rfm = ReferenceFactoryManager.class.cast(
                    orb.resolve_initial_references( "ReferenceFactoryManager" )) ;
                } catch (Exception exc) {
                    // do nthing
                }
            rfm.activate() ;

            final PresentationManager pm = 
                com.sun.corba.ee.spi.orb.ORB.getPresentationManager() ;
            String repositoryId ;

            try {
                repositoryId = pm.getRepositoryId( new ETestImpl() ) ;
            } catch (Exception exc) {
                throw new RuntimeException( exc ) ;
            }
        
            final List<Policy> policies = new ArrayList<Policy>() ;
            final ReferenceFactory rf = rfm.create( "factory", repositoryId, 
                policies, locator ) ;

            // arbitrary
            final byte[] oid = new byte[] { 0, 3, 5, 7 } ;

            final org.omg.CORBA.Object ref = rf.createReference( oid ) ;

            final Any any = orb.create_any() ;
            final ForwardRequest fr = new ForwardRequest(ref) ;

            // The whole point of this test
            ForwardRequestHelper.insert(any, fr) ;
        } finally {
            if (orb != null) {
                orb.destroy() ;
            }
        }
    }
}
