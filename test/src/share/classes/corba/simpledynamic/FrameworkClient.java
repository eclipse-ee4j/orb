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

package corba.simpledynamic;

import org.glassfish.pfl.basic.func.NullaryFunction;
//import com.sun.corba.ee.impl.orbutil.newtimer.generated.TimingPoints;
import org.glassfish.pfl.tf.timer.spi.TimerManager;
import org.glassfish.pfl.tf.timer.spi.TimerGroup;
import org.glassfish.pfl.tf.timer.spi.LogEventHandler;
import java.io.IOException ;
import java.io.ObjectInputStream ;
import java.io.ObjectOutputStream ;
import java.io.Serializable ;

import java.rmi.MarshalException ;

import java.util.Properties ;

import java.rmi.RemoteException ;



import org.testng.Assert ;
import org.testng.annotations.Test ;
import org.testng.annotations.BeforeGroups ;
  
import corba.nortel.NortelSocketFactory ;

import com.sun.corba.ee.spi.misc.ORBConstants ;

import com.sun.corba.ee.spi.orb.ORB ;

import corba.misc.Buck ;

import static corba.framework.PRO.* ;

public class FrameworkClient extends Framework {
    private static final boolean RUN_FRAGMENT_TEST = false ;

    private static final String SERVER_NAME = "fromServer" ;
    private static final String CLIENT_NAME = "fromClient" ;
    private static final String TEST_REF_NAME = "testref" ;

    private static final String TESTREF_GROUP = "testref_group" ;
    private static final String GROUP_5161 = "5161_group" ;

    private Echo makeServant( String name ) {
        try {
            return new EchoImpl( name ) ;
        } catch (RemoteException rex) {
            Assert.fail( "Unexpected remote exception " + rex ) ;
            return null ; // never reached
        }
    }

    private void msg( String msg ) {
        System.out.println( "+++FrameworkClient: " + msg ) ;
    }

    @BeforeGroups( { TESTREF_GROUP } ) 
    public void initTestRef() {
        bindServant( makeServant( SERVER_NAME ), Echo.class, TEST_REF_NAME ) ;
    }

    @Test( groups = { TESTREF_GROUP } ) 
    public void firstTest() {
        try {
            InterceptorTester.theTester.clear() ;
            Echo servant = makeServant( CLIENT_NAME ) ;
            connectServant( servant, getClientORB() ) ;

            System.out.println( "Creating first echoref" ) ;
            Echo ref = toStub( servant, Echo.class ) ;

            System.out.println( "Hello?" ) ;
            System.out.println( "Looking up second echoref" ) ;
            Echo sref = findStub( Echo.class, TEST_REF_NAME ) ;
            Assert.assertEquals( sref.name(), SERVER_NAME ) ;

            if (RUN_FRAGMENT_TEST) {
                System.out.println( "Running test for bug 6578707" ) ;
                testFragmentation( sref ) ;
            }

            System.out.println( "Echoing first echoref" ) ;
            Echo rref = sref.say( ref ) ;
            Assert.assertEquals( rref.name(), CLIENT_NAME ) ;

            System.out.println( "Echoing second echoref" ) ;
            Echo r2ref = rref.say( sref ) ;
            Assert.assertEquals( r2ref.name(), SERVER_NAME ) ;

            System.out.println( "Echoing third echoref" ) ;
            Echo ref2 = ref.say( ref ) ;
            Assert.assertEquals( ref2.name(), ref.name() ) ;
        } catch (Exception exc) {
            System.out.println( "Caught exception " + exc ) ;
            exc.printStackTrace() ;
        }
    }

    @Override
    protected Properties extraServerProperties() {
        Properties result = new Properties() ;
        result.setProperty( ORBConstants.TIMING_POINTS_ENABLED, "true" ) ;

        result.setProperty( ORBConstants.DEBUG_PROPERTY, "valueHandler,streamFormatVersion,cdr" ) ;

        return result ;
    }

    @Override
    protected Properties extraClientProperties() {
        Properties result = new Properties() ;

        result.setProperty( ORBConstants.TIMING_POINTS_ENABLED, "true" ) ;

        result.setProperty( ORBConstants.DEBUG_PROPERTY, "valueHandler,streamFormatVersion,cdr" ) ;
        
        // register nortel socket factory
        result.setProperty( ORBConstants.SOCKET_FACTORY_CLASS_PROPERTY, 
            NortelSocketFactory.class.getName() ) ;
        
        // register ORBInitializer
        result.setProperty( ORBConstants.PI_ORB_INITIALIZER_CLASS_PREFIX + 
            InterceptorTester.class.getName(), "true" ) ;

        // result.setProperty( ORBConstants.DEBUG_PROPERTY, 
            // "transport" ) ;
        
        result.setProperty( ORBConstants.TRANSPORT_TCP_CONNECT_TIMEOUTS_PROPERTY,
            "100:2000:100" ) ;

        return result ;
    }

    private static class Fragment implements java.io.Serializable {
        String str;

        Fragment(int  size) {
            str="";
            for(int i=0;i<size;i++) {
                str+="B";
            }
        }
    }

    private static class Wrapper implements java.io.Serializable{
        Fragment f = null;
        java.util.Vector vec = null;

        public Wrapper(int len, java.util.Vector vec){
            this.vec = vec;
            f = new Fragment(len);
        }

        private void readObject(java.io.ObjectInputStream is
            ) throws java.io.IOException,  ClassNotFoundException{

            is.defaultReadObject();
        }

        private void writeObject(java.io.ObjectOutputStream is
            ) throws java.io.IOException{

            is.defaultWriteObject();
        }
    }

    public void testFragmentation( Echo sref ) {
        Throwable t = new Throwable();
        java.util.Vector v = new java.util.Vector();
        v.add(t);
        for (int i = 0; i < 1024; i++){
            try {
                System.out.println("Hello call " + i);
                Wrapper w = new Wrapper(i, v);
                sref.sayHello(w);
            } catch (Exception exc) {
                System.out.println( "Caught exception " + exc ) ;
                exc.printStackTrace() ;
            }
        }
    }

    private int[] makeIntArray( int size ) {
        int[] result = new int[size] ;
        for (int ctr=0; ctr<size; ctr++)
            result[ctr] = ctr ;
        return result ;
    }

    private void testWriteFailure( int[] arg ) {
        try {
            msg( "testWriteFailure with " + arg.length + " ints" ) ;
            InterceptorTester.theTester.clear() ;
            Echo sref = findStub( Echo.class, TEST_REF_NAME ) ;
            sref.echo( arg ) ;

            NortelSocketFactory.disconnectSocket() ;
            NortelSocketFactory.simulateConnectionDown() ;
            InterceptorTester.theTester.setExceptionExpected() ;

            msg( "******* Start Test with disconnected connection *******" ) ; 
            // getClientORB().setDebugFlag( "transport" ) ;
            sref.echo( arg ) ;
            // getClientORB().clearDebugFlag( "transport" ) ;
            msg( "******* End test with disconnected connection *******" ) ; 
        } catch (MarshalException exc) {
            msg( "Caught expected MarshalException" ) ;
        } catch (Exception exc) {
            exc.printStackTrace() ;
            Assert.fail( "Unexpected exception " + exc ) ;
        } finally {
            NortelSocketFactory.simulateConnectionUp() ;
            Assert.assertEquals( InterceptorTester.theTester.getErrors(), 0 ) ;
        }
    }

    @Test( groups = { TESTREF_GROUP } ) 
    public void testWriteFailureFragment() {
        testWriteFailure( makeIntArray( 50000 ) ) ;
    }

    @Test( groups = { TESTREF_GROUP } ) 
    public void testWriteFailureNoFragment() {
        testWriteFailure( makeIntArray( 50 ) ) ;
    }

    private static class RCTest implements Serializable {
        byte[] front ;
        Throwable thr ;

        void setPrefixSize( int size ) {
            front = new byte[size] ;
            for (int ctr=0; ctr<size; ctr++ ) {
                front[ctr] = (byte)(ctr & 255) ;
            }
        }

        RCTest( Throwable thr ) {
            setPrefixSize( 0 ) ;
            this.thr = thr ;
        }

        private void readObject( ObjectInputStream is ) throws IOException, ClassNotFoundException {
            is.defaultReadObject() ;
        }

        private void writeObject( ObjectOutputStream os ) throws IOException {
            os.defaultWriteObject() ;
        }
    }

    @Test()
    public void testRecursiveTypeCode() {
        int ctr=0 ;
        try {
            msg( "Start recursive TypeCode test" ) ;
            Throwable thr = new Throwable( "Top level" ) ;
            Throwable cause = new Throwable( "The cause" ) ;
            thr.initCause( cause ) ;
            RCTest rct = new RCTest( thr ) ;
            Echo sref = findStub( Echo.class, TEST_REF_NAME ) ;

            // getClientORB().setDebugFlag( "giop" ) ;
            for (ctr=0; ctr<4096; ctr+=256) {
                rct.setPrefixSize( ctr ) ;
                sref.echo( rct ) ;
            }
            // getClientORB().clearDebugFlag( "giop" ) ;

        } catch (Exception exc) {
            exc.printStackTrace() ;
            Assert.fail( "Unexpected exception in testRecursiveTypeCode for ctr = " + ctr + " :" + exc ) ;
        }
    }

    @Test()
    public void testCorbalocRir() {
        msg( "corbaloc:rir URL test" ) ;
        String name = "UseThisName" ;
        String url = "corbaloc:rir:/" + name ;
        ORB orb = getClientORB() ;
        try {
            Echo serv = makeServant( "purple" ) ;
            connectServant( serv, getClientORB() ) ;
            Echo stub = toStub( serv, Echo.class ) ;
            getClientORB().register_initial_reference( name, 
                (org.omg.CORBA.Object)stub ) ;

            Echo echo = narrow( orb.string_to_object( url ), Echo.class ) ;
            Assert.assertFalse( echo == null ) ;
        } catch (Exception exc) {
            exc.printStackTrace() ;
            Assert.fail( "Unexpected exception in testCorbalocRir: " + exc ) ;
        }
    }

/*
    private static class CDRTimerContext {
        private LogEventHandler clientLEH ;
        private LogEventHandler serverLEH ;
        private TimerGroup clientCDR ;
        private TimerGroup serverCDR ;

        public CDRTimerContext( ORB clientORB, ORB serverORB ) {
            final TimerManager<TimingPoints> clientTM = 
                clientORB.makeTimerManager( TimingPoints.class) ;
            clientLEH = clientTM.factory().makeLogEventHandler( "Client_CDR_LEH" ) ;
            clientTM.controller().register( clientLEH ) ;
            clientCDR = clientTM.points().Cdr() ;

            final TimerManager<TimingPoints> serverTM = 
                serverORB.makeTimerManager( TimingPoints.class ) ;
            serverLEH = serverTM.factory().makeLogEventHandler( "Server_CDR_LEH" ) ;
            serverTM.controller().register( serverLEH ) ;
            serverCDR = serverTM.points().Cdr() ;
        }

        public void enable() {
            clientCDR.enable() ;
            serverCDR.enable() ;
        }

        public void disable() {
            clientCDR.disable() ;
            serverCDR.disable() ;
        }

        public void display( String msg ) {
            System.out.println( "Displaying CDR events for: " + msg ) ;
            clientLEH.display( System.out, "Client-side events" ) ;
            serverLEH.display( System.out, "Server-side events" ) ;
        }

        public void clear() {
            clientLEH.clear() ;
            serverLEH.clear() ;
        }
    }
*/

    private static final boolean DEBUG_5161 = false ;

    private Echo clientRef5161 = null ;
    //private CDRTimerContext timerContext = null ;

    private void doOperation( String msg, NullaryFunction func ) {
        System.out.println( msg ) ;
        try {
/*
            if (DEBUG_5161) {
                timerContext.enable() ;
            }
*/

            func.evaluate() ;
        // } catch (Exception exc) {
            // System.out.println( msg + ": caught exception " + exc ) ;
            // exc.printStackTrace() ;
            // Assert.fail( "Failed with exception " + exc ) ;
        } finally {
/*
            if (DEBUG_5161) {
                timerContext.disable() ;
                timerContext.display( msg ) ;
                timerContext.clear() ;
            }
*/
        }
    }
/*

    @BeforeGroups( { GROUP_5161 } )
    public void init5161() {
        // Make sure that echo is implemented in server, but the reference
        // is bound in client for the test: we want this to test marshaling,
        // not local optimization copying.
        final Echo servant = makeServant( "echotest" ) ;
        bindServant( servant, Echo.class, "BuckPasser" ) ;
        clientRef5161 = findStub( Echo.class, "BuckPasser" ) ;

        if (DEBUG_5161)
            // Prepare timing for client and server ORBs
            timerContext = new CDRTimerContext( getClientORB(), getServerORB() ) ;
    }

    // btrace hooks
    private void stop() {}
    private void start() {}

    @Test( groups = { GROUP_5161 } )
    public void test5161VectorOriginal() {
        doOperation( "Testing VectorOriginal", new NullaryFunction() {
            public Object evaluate() {
                try {
                    BuckPasserVectorOriginal bpvo = new BuckPasserVectorOriginal() ;
                    bpvo.add( new Buck( "The Buck" ) ) ;
                    start() ;
                    BuckPasserVectorOriginal bpvo2 = null ;
                    try {
                        bpvo2 = clientRef5161.echo( bpvo ) ;
                    } finally {
                        stop() ;
                    }
                    Assert.assertTrue( bpvo2.equals( bpvo ) ) ;
                    return null ;
                } catch (RemoteException exc) {
                    throw new RuntimeException( exc ) ;
                }
            }
        } ) ;
    }
*/

    // @Test( groups = { GROUP_5161 } )
    public void test5161() throws RemoteException {
        // System.out.println( "Running test for issue 5161" ) ;

        /* This does not reproduce the problem
        final BuckPasserAL bpal = new BuckPasserAL() ;
        bpal.add( new Buck( "The Buck" ) ) ;

        final BuckPasserV bpv = new BuckPasserV() ;
        bpv.add( new Buck( "The Buck" ) ) ;

        OutputStream out = (OutputStream)orb.create_output_stream();

        out.write_value(bpal) ;
        out.write_value(bpv) ;

        InputStream in = (InputStream)out.create_input_stream();

        BuckPasserAL bpal2 = (BuckPasserAL)in.read_value() ;
        BuckPasserV bpv2 = (BuckPasserV)in.read_value() ;
        */

        /** This case passes, so comment out for now
        doOperation( "Testing ArrayList", new NullaryFunction() {
            public Object evaluate() {
                try {
                    BuckPasserAL bpal2 = clientRef5161.echo( bpal ) ;
                    Assert.assertTrue( bpal2.equals( bpal ) ) ;
                    return null ;
                } catch (RemoteException exc) {
                    throw new RuntimeException( exc ) ;
                }
            }
        } ) ;
        */
    }

    public static void main( String[] args ) {
        Class[] classes = { FrameworkClient.class } ;
        Framework.run( "gen/corba/simpledynamic/test-output", classes ) ;
    }
}
