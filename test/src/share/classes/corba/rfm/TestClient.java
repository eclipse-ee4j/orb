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

package corba.rfm ;

import java.rmi.Remote ;
import java.rmi.RemoteException ;

import java.util.Arrays ;
import java.util.List ;
import java.util.ArrayList ;
import java.util.Map ;
import java.util.HashMap ;
import java.util.Properties ;

import javax.rmi.PortableRemoteObject ;

import javax.rmi.CORBA.Tie ;

import org.omg.CORBA.ORB ;
import org.omg.CORBA.Policy ;
import org.omg.CORBA.BAD_OPERATION ;
import org.omg.CORBA.LocalObject ;

import org.omg.CosNaming.NamingContext ;
import org.omg.CosNaming.NamingContextHelper ;
import org.omg.CosNaming.NamingContextExt ;
import org.omg.CosNaming.NamingContextExtHelper ;
import org.omg.CosNaming.NameComponent ;
import org.omg.CosNaming.NamingContextPackage.CannotProceed ;
import org.omg.CosNaming.NamingContextPackage.InvalidName ;
import org.omg.CosNaming.NamingContextPackage.AlreadyBound ;
import org.omg.CosNaming.NamingContextPackage.NotFound ;

import org.omg.PortableServer.ForwardRequest ;
import org.omg.PortableServer.POA ;
import org.omg.PortableServer.Servant ;
import org.omg.PortableServer.ServantLocator ;

import org.omg.PortableServer.ServantLocatorPackage.CookieHolder ;

import com.sun.corba.ee.spi.oa.rfm.ReferenceFactory ;
import com.sun.corba.ee.spi.oa.rfm.ReferenceFactoryManager ;

import com.sun.corba.ee.spi.extension.ServantCachingPolicy ;

import com.sun.corba.ee.spi.presentation.rmi.PresentationManager ;

import com.sun.corba.ee.spi.misc.ORBConstants ;

import com.sun.corba.ee.impl.naming.cosnaming.TransientNameService ;
import org.glassfish.pfl.basic.contain.Pair;

/** This is a test for the ReferenceFactoryManager.
 * The basic idea is to start up a number of client threads,
 * both remote and co-located, that delay for various
 * times, and have these tests invoke several objects.
 * Then we reconfigure the POAs using ReferenceFactoryManager.restart,
 * and make sure that all threads continue to work correctly,
 * and that invocations complete before reconfiguration.
 * <P>
 * Structure of this test:
 *
 * Two ORB instances:
 * - Server ORB, which has persistent server ID and port set, 
 *   the user configurator for the ReferenceFactoryManager,
 *   initial host set to localhost, and initial port set to
 *   the persistent server port.  This ORB is also set up for
 *   fully optimized colocated RMI-IIOP calls.
 * - Client ORB, which has initial host set to localhost, and
 *   initial port set to the same value as the Server ORB's
 *   persistent server port.
 *
 * A simple remote interface that supports echo and delay operations.
 * Delay takes a parameter that tells the server how long to wait 
 * until responding.
 *
 * A client thread that simply:
 * while (running)
 *      delay( config time )
 *      invoke either echo or delay
 *      log completion
 *
 * The objref that the client thread uses may be resolved in
 * either the client or server ORBs.  This allows testing both
 * the remote and the various colocated call paths.
 * This is essential because the code is different in these cases.
 *
 * The test needs two ReferenceFactories configured as follows:
 * - With full servant caching enabled (test FullServantCacheLocalCRDImpl restart)
 * - Without any servant caching (test POAlocalCRDImpl restart)
 * We create 3 objrefs for each type of caching with different delay
 * length (0, 5, 21 msec)
 * These are named cache/0 cache/5 cache/21 nocache/0 nocache/5 and
 * nocache/21
 * 
 *
 * The ServantLocator does a couple of things:
 * - Delays for a configurable time before returning a fixed servant
 * - checks its state to make sure that it is supposed to be in use.
 *   
 * There is also a controller that starts and coordinates all testing
 * activity.
 * The controller basically starts up a number of different types of
 * client threads, then starts another thread that delays, then 
 * does a restart in the ReferenceFactoryManager.  The restart will
 * introduce a new ServantLocator in all ReferenceFactory instances,
 * and set the state of the old ServantLocator to log errors if
 * any further invocations are received in the old ServantLocator.
 * Several restart cycles will be performed.
 *
 * In addition, we need to test nested calls; that is, calls to co-located
 * objrefs on the server side.  This also include creating new objrefs
 * through the RFM.
 */ 
public class TestClient {
    static {
        // This is needed to guarantee that this test will ALWAYS use dynamic
        // RMI-IIOP.  Currently the default is dynamic when renamed to "ee",
        // but static in the default "se" packaging, and this test will
        // fail without dynamic RMI-IIOP.
        System.setProperty( ORBConstants.USE_DYNAMIC_STUB_PROPERTY, "true" ) ;
    }

    public static synchronized void log( String msg ) {
        System.out.println( msg ) ;
    }

    public static synchronized void fatal( String msg, Throwable thr ) {
        thr.printStackTrace() ;
        log( msg ) ;
        System.exit( 1 ) ;
    }

    public static void sleep( int delay ) {
        try {
            Thread.sleep( delay ) ;
        } catch (InterruptedException exc) {
            // Don't care if my sleep is interrupted.
        }
    }

    private interface Test extends Remote {
        public void createAnObject( int threadId ) throws RemoteException ;

        public int echo( Test remoteSelf, int threadId, int value ) throws RemoteException ;

        public int delay( Test remoteSelf, int threadId, int value, int delay ) throws RemoteException ;
    }

    private static class TestImpl extends PortableRemoteObject implements Test {
        public TestImpl() throws RemoteException {
            super() ;
        }

        private void log( int threadId, String msg ) {
            StringBuilder sbuff = new StringBuilder() ;
            sbuff.append( "[" ) 
                .append( threadId )
                .append( "] " ) 
                .append( msg ) ;
            System.out.println( sbuff.toString() ) ;
        } 

        public void createAnObject( int threadId ) throws RemoteException {
            byte[] key = { (byte)1, (byte)2, (byte)3 } ;
            
            log( threadId, "Creating a reference with cacheFactory" ) ;
            cacheFactory.createReference( key ) ;
        }

        public int echo( Test remoteSelf, int threadId, int value ) throws RemoteException {
            remoteSelf.createAnObject( threadId ) ;

            log( threadId, "Echo returns" + value ) ;
            return value ;
        }

        public int delay( Test remoteSelf, int threadId, int value, int delay ) throws RemoteException {
            log( threadId, "Delay sleeps for " + delay + " milliseconds" ) ;
            remoteSelf.createAnObject( threadId ) ;

            sleep( delay ) ;

            log( threadId, "Delay returns " + value ) ;
            return value ;
        }
    }

    static Object runningLock = new Object() ;
    static boolean running = true ;

    private static class Client extends Thread {
        private int threadId ;
        private int delay ;
        private Test testref ;
        private int value ;
        private boolean useEcho ;
        private int callDelay ;

        public String toString() {
            return "Client[" + threadId + "]" ;
        }

        // Create a Client thread that invokes the Test remoteSelf, echo method
        public Client( int threadId, int delay, Test testref ) {
            this.threadId = threadId ;
            this.delay = delay ;
            this.testref = testref ;
            this.value = 0 ;
            this.useEcho = true ;
            this.callDelay = 0 ;
            clients.add( this ) ;
        }

        // Create a Client thread that invokes the delay method
        public Client( int threadId, int delay, Test testref, int callDelay ) {
            this.threadId = threadId ;
            this.delay = delay ;
            this.testref = testref ;
            this.value = 0 ;
            this.useEcho = false ;
            this.callDelay = callDelay ;
            clients.add( this ) ;
        }

        private void logStart( ) {
            StringBuilder sbuff = new StringBuilder() ;
            sbuff.append( "[" ) 
                .append( threadId )
                .append( ":" ) 
                .append( value ) 
                .append( "] " ) 
                .append( "Call to " )
                .append( useEcho ? "echo" : "delay" )
                .append( " started" ) ;
            log( sbuff.toString() ) ;
        }

        private void logComplete( int result ) {
            StringBuilder sbuff = new StringBuilder() ;
            sbuff.append( "[" ) 
                .append( threadId )
                .append( ":" ) 
                .append( value ) 
                .append( "] " ) 
                .append( "Call to " )
                .append( useEcho ? "echo" : "delay" )
                .append( " completed with result " )
                .append( result ) ;
            log( sbuff.toString() ) ;
        }

        public void halt() {
            synchronized( runningLock ) {
                running = false ;
            }

            this.interrupt() ;
        }

        public void run() {
            Thread.currentThread().setName( "Client_" + threadId ) ;
            log( "Thread " + threadId + " started" ) ;

            while (true) {
                synchronized( runningLock ) {
                    if (!running)
                        break ;
                }

                try {
                    log( "Thread " + threadId + " sleeping" ) ;
                    sleep( delay ) ;
                    int result ;
                    logStart( ) ;
                    if (useEcho)
                        result = testref.echo( testref, threadId, value ) ;
                    else
                        result = testref.delay( testref, threadId, value, callDelay ) ;
                    logComplete( result ) ;
                    if (result != value)
                        throw new Exception( "value and result do not match" ) ;
                    value++ ;
                } catch (InterruptedException exc) {
                    log( "Thread " + threadId + " interrupted" ) ;
                } catch (Exception exc) {
                    fatal( "Exception in client: " + exc, exc ) ;
                } finally {
                    value++ ;
                }
            }

            log( "Thread " + threadId + " terminated" ) ;
        }
    }

    private static List<Client> clients = new ArrayList<Client>() ;

    private static final String PORT_NUM = "3074" ;
    private static ORB clientORB ;
    private static ORB serverORB ;
    private static ReferenceFactoryManager rfm ;
    private static NamingContextExt clientNamingRoot ;
    private static NamingContextExt serverNamingRoot ;

    private static String nocacheFactoryName = "nocache" ;
    private static List<Policy> nocacheFactoryPolicies ;
    private static ReferenceFactory nocacheFactory ;

    private static String cacheFactoryName = "cache" ;
    private static List<Policy> cacheFactoryPolicies ;
    private static ReferenceFactory cacheFactory ;

    private static TestServantLocator locator ;

    private static void cleanUp() {
        for (Client cl : clients) 
            cl.halt() ;

        for (Client cl : clients) {
            try {
                cl.join( 500 ) ;
            } catch (InterruptedException exc) {
                log( "Caught interrupted excption while joining thread " + cl ) ;
            }
        }

        log( "Shutting down clientORB" ) ;
        clientORB.shutdown( true ) ;
        log( "Destroying clientORB" ) ;
        clientORB.destroy() ;

        log( "Shutting down serverORB" ) ;
        serverORB.shutdown( true ) ;
        log( "Destroying serverORB" ) ;
        serverORB.destroy() ;
    }

    private static void initializeORBs( String[] args ) {
        // The following must be set as system properties 
        System.setProperty( "javax.rmi.CORBA.PortableRemoteObjectClass",
            "com.sun.corba.ee.impl.javax.rmi.PortableRemoteObject" ) ;
        System.setProperty( "javax.rmi.CORBA.StubClass",
            "com.sun.corba.ee.impl.javax.rmi.CORBA.StubDelegateImpl" ) ;
        System.setProperty( "javax.rmi.CORBA.UtilClass",
            "com.sun.corba.ee.impl.javax.rmi.CORBA.Util" ) ;
        System.setProperty( ORBConstants.USE_DYNAMIC_STUB_PROPERTY,
            "true" ) ;

        // initializer client and server ORBs.
        // Initialize server using RFM and register objrefs in naming
        Properties baseProps = new Properties() ;
        baseProps.setProperty( "org.omg.CORBA.ORBSingletonClass",
            "com.sun.corba.ee.impl.orb.ORBSingleton" ) ;
        baseProps.setProperty( "org.omg.CORBA.ORBClass",
            "com.sun.corba.ee.impl.orb.ORBImpl" ) ;
        baseProps.setProperty( ORBConstants.INITIAL_HOST_PROPERTY,
            "localhost" ) ;
        baseProps.setProperty( ORBConstants.INITIAL_PORT_PROPERTY,
            PORT_NUM ) ;
        baseProps.setProperty( ORBConstants.ALLOW_LOCAL_OPTIMIZATION,
            "true" ) ;
        // For debugging only
        baseProps.setProperty( ORBConstants.DEBUG_PROPERTY,
            "poa" ) ;
        
        Properties clientProps = new Properties( baseProps ) ;
        clientProps.setProperty( ORBConstants.ORB_ID_PROPERTY,
            "clientORB" ) ;

        Properties serverProps = new Properties( baseProps ) ;
        serverProps.setProperty( ORBConstants.PERSISTENT_SERVER_PORT_PROPERTY,
            PORT_NUM ) ;
        serverProps.setProperty( ORBConstants.SERVER_HOST_PROPERTY,
            "localhost" ) ;
        serverProps.setProperty( ORBConstants.ORB_ID_PROPERTY,
            "serverORB" ) ;
        serverProps.setProperty( ORBConstants.ORB_SERVER_ID_PROPERTY,
            "300" ) ;
        serverProps.setProperty( ORBConstants.RFM_PROPERTY,
            "1" ) ;

        // Ignore the args passed in, since they contain a test framework
        // determined value of -ORBInitialPort
        String[] myArgs = {} ;

        serverORB = ORB.init( myArgs, serverProps ) ;
        new TransientNameService( 
            com.sun.corba.ee.spi.orb.ORB.class.cast(serverORB) ) ;

        clientORB = ORB.init( myArgs, clientProps ) ;
    }

    private static class TestServantLocator extends LocalObject
        implements ServantLocator {
        private boolean isActive ;
        private Servant servant ;

        public TestServantLocator( ORB orb, ReferenceFactoryManager rfm ) {
            isActive = true ;

            TestImpl impl = null ;
            try {
                impl = new TestImpl() ;
            } catch (Exception exc) {
                fatal( "Exception in creating servant: " + exc, exc ) ;
            }

            Tie tie = com.sun.corba.ee.spi.orb.ORB.class.cast( orb )
                .getPresentationManager().getTie() ;
            tie.setTarget( impl ) ;
            servant = Servant.class.cast( tie ) ;
        }

        public synchronized void deactivate() {
            isActive = false ;
        }

        public synchronized Servant preinvoke( byte[] oid, POA adapter,
            String operation, CookieHolder the_cookie 
        ) throws ForwardRequest {
            if (!isActive)
                throw new BAD_OPERATION( "Attempt to use deactivated ServantLocator" ) ;

            int delay = oid[0] ;

            log( "ServantLocator.preinvoke for " + operation + " with delay " + delay ) ;

            if (delay > 0)
                sleep( delay ) ;
            log( "ServantLocator.preinvoke for " + operation + " returning servant" ) ;

            return servant ;
        }

        public void postinvoke( byte[] oid, POA adapter,
            String operation, Object the_cookie, Servant the_servant ) {
            log( "ServantLocator.postinvoke for " + operation + " called" ) ;
        }
    }

    // Info for creating objrefs:
    // refname, delay, cacheflag
    private static Object[][] objrefData = {
        { "cache/0", 0, true },
        { "cache/5", 5, true },
        { "cache/23", 23, true },
        { "nocache/0", 0, false },
        { "nocache/5", 5, false },
        { "nocache/23", 23, false }} ;
   
    private static void initializeServer() {
        // Get the RFM and naming service
        try {
            rfm = ReferenceFactoryManager.class.cast( 
                serverORB.resolve_initial_references( "ReferenceFactoryManager" )) ;
            rfm.activate() ;
            serverNamingRoot = NamingContextExtHelper.narrow(
                serverORB.resolve_initial_references( "NameService" )) ;
            clientNamingRoot = NamingContextExtHelper.narrow(
                clientORB.resolve_initial_references( "NameService" )) ;
        } catch (Exception exc) {
            fatal( "Exception in getting initial references: " + exc, exc ) ;
        }

        // Create required ReferenceFactory instances
        locator = new TestServantLocator( serverORB, rfm ) ;

        PresentationManager pm = 
            com.sun.corba.ee.spi.orb.ORB.getPresentationManager() ;
        String repositoryId ;

        try {
            repositoryId = pm.getRepositoryId( new TestImpl() ) ;
        } catch (Exception exc) {
            throw new RuntimeException( exc ) ;
        }
        
        nocacheFactoryPolicies = null ;
        nocacheFactory = rfm.create( nocacheFactoryName,
            repositoryId, nocacheFactoryPolicies, locator ) ;

        cacheFactoryPolicies = Arrays.asList( 
            (Policy)ServantCachingPolicy.getPolicy() ) ;
        cacheFactory = rfm.create( cacheFactoryName, 
            repositoryId, cacheFactoryPolicies, locator ) ;

        // Use RMs to create objrefs and register them with naming
        for ( Object[] data : objrefData) {
            String sname = String.class.cast( data[0] ) ;
            int delay = Integer.class.cast( data[1] ) ;
            boolean useCaching = Boolean.class.cast( data[2] ) ;
            ReferenceFactory factory = useCaching ? cacheFactory : nocacheFactory ;
            byte[] oid = new byte[] { (byte)delay } ;
            org.omg.CORBA.Object objref = factory.createReference( oid ) ; 
            try {
                bindName( serverNamingRoot, sname, objref ) ;
            } catch (Exception exc) {
                fatal( "Error in initializeServer: " + exc, exc ) ;
            }
        }
    }

    private static void bindName( NamingContext ctx, String sname,
        org.omg.CORBA.Object objref )
        throws NotFound, CannotProceed, AlreadyBound, InvalidName 
    {
        NameComponent[] name = serverNamingRoot.to_name( sname ) ;
        NamingContext current = ctx ;
        for (int ctr=0; ctr<name.length; ctr++) {
            NameComponent[] arr = new NameComponent[] { name[ctr] } ;

            if (ctr < name.length - 1) {
                try {
                    org.omg.CORBA.Object ref = current.resolve( arr ) ;
                    if (ref._is_a(NamingContextHelper.id()))
                        current = NamingContextHelper.narrow( ref ) ;
                    else
                        throw new BAD_OPERATION( 
                            "Name is bound to a non-context object reference" ) ;
                } catch (NotFound exc) {
                    current = current.bind_new_context( arr ) ;
                }
            } else {
                current.bind( arr, objref ) ; 
            }
        }
    }

    // Info for creating clients:
    // threadId, delay, refname, callDelay 
    private static Object[][] clientData = {
        { 1,   5, "cache/0", 5 },
        { 2,   9, "cache/0", 0 },
        { 3,  16, "cache/0", 0 },
        { 4,   5, "cache/5", 0 },
        { 5,   9, "cache/5", 5 },
        { 6,  16, "cache/5", 0 },
        { 7,   5, "cache/23", 0 },
        { 8,   9, "cache/23", 0 },
        { 9,  16, "cache/23", 5 },
        { 10,  5, "nocache/0", 5 },
        { 11,  9, "nocache/0", 0 },
        { 12, 16, "nocache/0", 0 },
        { 13,  5, "nocache/5", 0 },
        { 14,  9, "nocache/5", 5 },
        { 15, 16, "nocache/5", 0 },
        { 16,  5, "nocache/23", 0 },
        { 17,  9, "nocache/23", 0 },
        { 18, 16, "nocache/23", 5 }} ;

    // calls echo: Client( int threadId, int delay, Test testref ) 
    // calls delay: Client( int threadId, int delay, Test testref, int callDelay ) 
    private static void makeClient( int threadId, int delay, String name, 
        NamingContextExt namingRoot, int callDelay ) {
        Test ref = null ;
        try {
            ref = Test.class.cast( PortableRemoteObject.narrow( 
                namingRoot.resolve_str( name ), Test.class )) ;
        } catch (Exception exc) {
            fatal( "Exception in makeClient: " + exc, exc ) ;
        }

        Client client ;
        if (callDelay == 0) {
            client = new Client( threadId, delay, ref ) ;
        } else {
            client = new Client( threadId, delay, ref, callDelay ) ;
        }
        client.setName( "Client[" + threadId + "]" ) ;
        client.start() ;
    }

    public static void initializeClients() {
        // Create and start a client for each table entry
        // Do this resolving the objref one each for the server and client ORBs
        // Add 1000 to the threadId for the client ORB
        for (Object[] data : clientData) {
            int threadId = Integer.class.cast( data[0] ) ;
            int delay = Integer.class.cast( data[1] ) ;
            String name = String.class.cast( data[2] ) ;
            int callDelay = Integer.class.cast( data[3] ) ;

            makeClient( threadId, delay, name, serverNamingRoot, callDelay ) ;
            makeClient( threadId + 1000, delay, name, clientNamingRoot, callDelay ) ;
        }
    }

    private static final int RUN_TIME = 1000 ; // 1 second

    public static void reconfigure() {
        // deactivate the old ServantLocator
        locator.deactivate() ;

        // create a new ServantLocator
        locator = new TestServantLocator( serverORB, rfm ) ;
        
        // update ReferenceFactoryManager with map giving new ServantLocators
        Map<String,Pair<ServantLocator,List<Policy>>> map = new
            HashMap<String,Pair<ServantLocator,List<Policy>>>() ;
        map.put( nocacheFactoryName, new Pair( locator, nocacheFactoryPolicies ) ) ;
        map.put( cacheFactoryName, new Pair( locator, cacheFactoryPolicies ) ) ;
        rfm.restartFactories( map ) ;
    }

    public static void main( String[] args ) {
        try {
            initializeORBs( args ) ;
            initializeServer() ;
            initializeClients() ;
        
            //
            // At this point all clients are running and sending
            // requests.  Now the test begins.
            
            for (int ctr=0; ctr<3; ctr++) {
                sleep( RUN_TIME ) ;
                log( "RFM>>>Suspend" ) ;
                rfm.suspend() ;
                log( "RFM>>>Reconfigure" ) ;
                reconfigure() ;
                log( "RFM>>>Resume" ) ;
                rfm.resume() ;
            }

            synchronized (runningLock) {
                running = false ;
            }

            cleanUp() ;
            log( "Test Complete" ) ;
            System.exit(0) ;
        } catch (Throwable thr) {
            fatal( "Test FAILED: Caught throwable " + thr, thr ) ;
        }
    }
}
