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

package pi.ort;

import com.sun.corba.ee.spi.misc.ORBConstants;
import IORInterceptorTest.*;    // for IDL
import corba.framework.InternalProcess;
import java.io.PrintStream;
import java.util.Hashtable;
import java.util.Properties;
import org.glassfish.pfl.test.JUnitReportHelper;
import org.omg.CORBA.ORB;
import org.omg.CORBA.Policy;
import org.omg.PortableInterceptor.NON_EXISTENT;
import org.omg.PortableServer.IdUniquenessPolicyValue;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAManager;

public class Server 
    implements InternalProcess 
{

    private static final String ROOT_POA = "RootPOA";

    private POA rootPOA;
    
    // Set from run()
    private PrintStream out;
    private PrintStream err;
    private ORB orb;

    private JUnitReportHelper helper = new JUnitReportHelper( this.getClass().getName() ) ;

    public static void main(String args[]) {
        try {
            (new Server()).run( System.getProperties(),
                                args, System.out, System.err, null );
        } catch( Exception e ) {
            e.printStackTrace( System.err );
            System.exit( 1 );
        }
    }

    public void run( Properties environment, String args[], PrintStream out,
                     PrintStream err, Hashtable extra) 
        throws Exception
    {
        try {
            this.out = out;
            this.err = err;

            out.println( "Instantiating ORB" );
            out.println( "=================" );

            // Initializer class
            String testInitializer = "pi.ort.ServerTestInitializer";
            ServerTestInitializer.out = out;

            // create and initialize the ORB
            Properties props = new Properties() ;
            props.put( "org.omg.CORBA.ORBClass", 
                       System.getProperty("org.omg.CORBA.ORBClass"));
            props.put( ORBConstants.PI_ORB_INITIALIZER_CLASS_PREFIX +
                       testInitializer, "" );
            props.put( ORBConstants.ORB_ID_PROPERTY, Constants.ORB_ID );
            props.put( ORBConstants.ORB_SERVER_ID_PROPERTY, 
                Constants.ORB_SERVER_ID );
            orb = ORB.init(args, props);
            ServerTestInitializer.orb = orb;

            // Get root POA:
            out.println( "Server retrieving root POA:" );
            rootPOA = (POA)orb.resolve_initial_references( "RootPOA" );
            rootPOA.the_POAManager().activate();

            // Check to make sure that POA state changes are notified
            // in the IOR Interceptor. 
            checkAdapterStateChangesTest1();

            //handshake:
            out.println("Server is ready.");
            out.flush();

            checkAdapterStateChangesTest2();

            checkAdapterStateChangesTest3();

            // NOTE: THIS TEST SHOULD ALWAYS BE THE LAST ONE. IT DESTROYS
            // THE POAMANAGER
            // Check to make sure that POAManager state changes are notified
            // in the IOR Interceptor. 
            checkAdapterManagerStateChanges();
        } finally {
            helper.done() ;
        }
    }

    /**
     * checkAdapterManagerStateChanges tests that the ORT notifications of
     * POAManager works right. These are the states it checks
     * ACTIVE -> HOLD -> DISCARD -> DEACTIVATE
     */
    private void checkAdapterManagerStateChanges() {
        helper.start( "checkAdapterManagerStateChanges" ) ;
        out.println( 
            "Checking if AdapterManagerStateChanges are registered..." );
        try {
            try {
                ORTStateChangeEvaluator stateChangeEvaluator =
                    ORTStateChangeEvaluator.getInstance( );
                stateChangeEvaluator.resetAllStates( );
                POAManager manager = rootPOA.the_POAManager( ); 
                manager.hold_requests( true );
                evaluateAdapterManagerStateChange( );
                manager.discard_requests( true );
                evaluateAdapterManagerStateChange( );
                manager.deactivate( false, true );
                evaluateAdapterManagerStateChange( );
            } catch( org.omg.PortableServer.POAManagerPackage.AdapterInactive e ) {
                err.println( "Unexpected AdapterInactive Exception in " +
                    " checkAdapterManagerStateChanges " );
                throw new RuntimeException(
                    "checkAdapterManagerStateChanges FAILED!");
            }
            out.println( "checkAdapterManagerStateChanges PASSED.." ); 
            helper.pass() ;
        } catch (RuntimeException exc) {
            helper.fail( exc ) ;
            throw exc ;
        }
    }

   
    /** 
     *  This is a utility method to check that the POAManager state and
     *  the state in ORTStateChangeEvaluator is same. If not, it raises
     *  a RuntimeException.
     */
    private void evaluateAdapterManagerStateChange( ) {
        ORTStateChangeEvaluator stateChangeEvaluator =
            ORTStateChangeEvaluator.getInstance( );
        POAManager manager = rootPOA.the_POAManager( ); 
        if( !stateChangeEvaluator.evaluateAdapterManagerState( (short)
            manager.get_state( ).value( ) ) ) 
        {
            err.println( "ERROR: Current POAManager state change:" + 
                manager.get_state( ).value( ) + 
                " is not notified to IORInterceptor.." );
            throw new RuntimeException(
                "checkAdapterManagerStateChanges FAILED!");
        }
    }


    /**
     *  These are the ORT Adapter State Change tests.
     *  Test 1: Create 2 group of POAs and destroy one group at a time and
     *          make sure that the destroyed notifications for all the POAs
     *          are obtained as a group.
     */
    private void checkAdapterStateChangesTest1( ) {
        helper.start( "checkAdapterStateChangesTest1" ) ;

        System.out.println( "checkAdapterStateChangesTest1 BEGIN.." );
        try {
            try {
                ORTStateChangeEvaluator stateChangeEvaluator =
                    ORTStateChangeEvaluator.getInstance( );
                stateChangeEvaluator.resetAllStates( );

                String[] poaGroup1 = { "POA1", "POA11", "POA12" };

                String[] poaGroup2 = { "POA2", "POA21", "POA22" }; 

                POA[] poaList1 = createPOAs( poaGroup1 );

                POA[] poaList2 = createPOAs( poaGroup2 );
         
                poaList1[0].destroy( false, true ); 
                evaluateAdapterStateChange( poaGroup1, null );

                stateChangeEvaluator.resetAllStates( );

                poaList2[0].destroy( false, true ); 
                evaluateAdapterStateChange( poaGroup2, null);

                System.out.println( "checkAdapterStateChanges Test1 PASSED.." );

            } catch( Exception e ) {
                err.println( "EXCEPTION : In checkAdapterStateChangeTest1 " + e );
                e.printStackTrace( );
                throw new RuntimeException( 
                    "checkAdapterStateChanges Test1 FAILED!");
            }

            helper.pass() ;
        } catch (RuntimeException exc) {
            helper.fail( exc ) ;
            throw exc ;
        }
    }

    /**
     *  Test 2: This test is very similar to Test 1, but little more complex.
     *          It creates a group of POAs and a Servant (DelayServant) using
     *          one of the child POAs. A method is invoked on the Servant in a
     *          separate thread, this method will sleep for a minute or so. 
     *          Parallely destroy the parent POA and check to see that detroyed
     *          notifications do happen after the completion of the method on
     *          DelayServant.
     */   
    private void checkAdapterStateChangesTest2( ) {
        helper.start( "checkAdapterStateChangesTest2" ) ;

        try {
            System.out.println( "checkAdapterStateChangesTest2 BEGIN.." );
            try {
                ORTStateChangeEvaluator stateChangeEvaluator =
                    ORTStateChangeEvaluator.getInstance( );
                stateChangeEvaluator.resetAllStates( );

                final String NOTIFICATION_TOKEN = "POA12INVOCATION_COMPLETE";
                String[] poasUnderTest = {"POA1", "POA11", "POA12" };
                POA[] poaList = createPOAs( poasUnderTest );

                org.omg.CORBA.Object object = createDelayServant( poaList[2] );
                final delay aDelay = delayHelper.narrow( object );
                new Thread( ) {
                    public void run() {
                        try {
                            aDelay.forInMillis(30000, NOTIFICATION_TOKEN ); 
                        } catch( Exception e ) {
                            System.err.println( "Failed to invoke on aDelay " +
                                " servant..." + e  );
                            e.printStackTrace( );
                            System.exit( 1 );
                        } 
                    }
                }.start( );
                
                // This sleep is to make sure that the Thread in the previous
                // statement is started for sure before calling POA.destroy
                Thread.sleep( 5000 );
                poaList[0].destroy( false, true ); 
                evaluateAdapterStateChange( poasUnderTest, NOTIFICATION_TOKEN );
                System.out.println( "checkAdapterStateChangesTest2 PASSED.." );
            } catch( Exception e ) {
                err.println( "EXCEPTION : In checkAdapterStateChangeTest2 " + e );
                e.printStackTrace( );
                throw new RuntimeException( 
                    "checkAdapterStateChanges Test2 FAILED!");
            }

            helper.pass() ;
        } catch (RuntimeException exc) {
            helper.fail( exc ) ;
            throw exc ;
        }
    }

    /**
     *  Test 3: This test is very similar to Test 2, but with 
     *          wait_for_completion for POA.destroy is set to false.
     *          It creates a group of POAs and a Servant (DelayServant) using
     *          one of the child POAs. A method is invoked on the Servant in a
     *          separate thread, this method will sleep for a minute or so.
     *          Parallely destroy the parent POA and check to see that destroyed
     *          notifications do happen after the completion of the method on
     *          DelayServant.
     *          
     *  NOTE: The sleep times are added to make sure that the destroyed 
     *  notification happens only after the invoke on DelayServant is complete.
     */  
    private void checkAdapterStateChangesTest3( ) {
        helper.start( "checkAdapterStateChangesTest2" ) ;

        System.out.println( "checkAdapterStateChangesTest3 BEGIN.." );

        try {
            try {
                ORTStateChangeEvaluator stateChangeEvaluator =
                    ORTStateChangeEvaluator.getInstance( );
                stateChangeEvaluator.resetAllStates( );

                final String NOTIFICATION_TOKEN = "POA11INVOCATION_COMPLETE";
                String[] poasUnderTest = {"POA1", "POA11", "POA12" };
                POA[] poaList = createPOAs( poasUnderTest );

                org.omg.CORBA.Object object = createDelayServant( poaList[1] );
                final delay aDelay = delayHelper.narrow( object );
                new Thread( ) {
                    public void run() {
                        try {
                            aDelay.forInMillis(30000,NOTIFICATION_TOKEN );
                        } catch( Exception e ) {
                            System.err.println( "Failed to invoke on aDelay " +
                                " servant..." + e  );
                            e.printStackTrace( );
                            System.exit( 1 );
                        }
                    }
                }.start( );
                // This sleep is to make sure that the Thread in the previous
                // statement is started for sure before calling POA.destroy
                Thread.sleep( 5000 );

                poaList[0].destroy( false, false );

                Thread.sleep( 5000 );

                boolean testStatus = true;

                //  Negative test to make sure that the destroy notification has
                //  not happened before completing DelayServant method.
                try {
                    evaluateAdapterStateChange( poasUnderTest, NOTIFICATION_TOKEN );
                    // If evaluation passed, then this test failed.  
                    testStatus = false;
                } catch( RuntimeException re ) {
                    // This is the expected result
                }

                //  Positive test to make sure that the destroy notification
                //  happened after completing DelaySerant method
                if (testStatus) {
                    // Wait for a while to finish the invocation on the DelayServant
                   int i = 0;
                    while(!stateChangeEvaluator.registerAdapterStateChangeCalled){
                        System.out.println( "..Wait Loop.." + i++ );
                        Thread.sleep( 5000 );
                    }
                    // Now check to see if the notifications have happened 
                    // correctly
                    evaluateAdapterStateChange( poasUnderTest, NOTIFICATION_TOKEN );
                    System.out.println( "checkAdapterStateChangesTest3 PASSED.." );
                } else {
                    // If we are here then the test failed
                    throw new RuntimeException(  "TEST FAILED..." );
                }
            } catch( Exception e ) {
                err.println( "EXCEPTION : In checkAdapterStateChangeTest3 " + e );
                e.printStackTrace( );
                throw new RuntimeException(
                    "checkAdapterStateChanges Test3 FAILED!");
            }

            helper.pass() ;
        } catch (RuntimeException exc) {
            helper.fail( exc ) ;
            throw exc ;
        }
    }


    /** 
     *  A Utility method to create POAs. This creates 
     *  1. POA<1> under rootPOA using poaIds[0]
     *  2. Child POAs using poaIds[1....n] under POA<1>
     *  
     *  NOTE: The return values is the ordered list of POAs created with 
     *  one to one mapping of poaIds passed. The first element is the parent and
     *  others are children of the first POA.
     */
    private POA[] createPOAs( String[] poaIds ) {
        try {
            POAManager poaManager = rootPOA.the_POAManager( );
            Policy[] policies = new Policy[1];

            policies[0] = rootPOA.create_id_uniqueness_policy(
                IdUniquenessPolicyValue.MULTIPLE_ID );

            POA[] poas = new POA[poaIds.length];

            poas[0] = rootPOA.create_POA( poaIds[0], poaManager, policies );
            for( int i = 1; i < poaIds.length; i++ ) {
                poas[i] = poas[0].create_POA( poaIds[i], poaManager, policies );
            }
            return poas;
        } catch( Exception e ) {
            err.println( "EXCEPTION : In checkAdapterStateChangeTest2 " + e );
            e.printStackTrace( );
            throw new RuntimeException( 
                "checkAdapterStateChanges Test2 FAILED!");
        }
            
    }
    
    /**
     * This checks to make sure that all POA destroyed notifications are 
     * registered in ORTStateChangeEvaluator. If a token is not null, it will
     * also check to make sure that token is sent from the delay servant.
     * It throws RuntimeException if the evaluation fails.
     */
    private void evaluateAdapterStateChange( 
        String[] poasWhoseStateChangesShouldbeReported, String token ) 
    {
        ORTStateChangeEvaluator stateChangeEvaluator =
            ORTStateChangeEvaluator.getInstance( );
        boolean testStatus = false;
        if( token == null ) {
            testStatus = stateChangeEvaluator.evaluateAdapterStateChange(
                         poasWhoseStateChangesShouldbeReported );
        } else {
            testStatus = stateChangeEvaluator.evaluateAdapterStateChange(
                         poasWhoseStateChangesShouldbeReported, token );
        }
        
        if( !testStatus ) {
            err.println( "ERROR: Adapter state change:" + 
                NON_EXISTENT.value + 
                " is not correctly notified to IORInterceptor.." );
            throw new RuntimeException(
                "checkAdapterStateChanges FAILED!");
        }
    }

    /**
     *  Create and Bind the DelayServant
     */
    public org.omg.CORBA.Object createDelayServant ( POA poa ) 
        throws Exception
    {
        org.omg.CORBA.Object result;
        // create servant and register it with the ORB
        DelayServant delayServantRef = new DelayServant();

        byte[] id = poa.activate_object(delayServantRef);
        result = poa.id_to_reference(id);

        return result;
    }

}
