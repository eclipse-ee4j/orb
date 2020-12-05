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

package pi.policyfactory;

import com.sun.corba.ee.spi.misc.ORBConstants;
import org.omg.CORBA.ORB;
import org.omg.CORBA.BAD_POLICY;
import org.omg.CORBA.Any;
import java.util.Properties;
import org.glassfish.pfl.test.JUnitReportHelper;

public class Client implements Runnable
{

    static final java.lang.Object lock = new java.lang.Object ();
    static boolean errorOccured = false;

    static ORB orb;

    private static boolean SUCCESS = true;

    private static boolean FAILURE = false;

    private String msg = null ;

    public void signalError () {
        synchronized (Client.lock) {
            errorOccured = true;
            System.exit(1);
        }
    }

    public static void main(String args[]) {
        new Client().run();
    }

    public void run()
    {
        JUnitReportHelper helper = new JUnitReportHelper( this.getClass().getName() ) ;
        try {
            // create and initialize the ORB
            Properties props = new Properties() ;
            props.put( "org.omg.CORBA.ORBClass",
                       "com.sun.corba.ee.impl.orb.ORBImpl" );
            props.put( ORBConstants.PI_ORB_INITIALIZER_CLASS_PREFIX +
                       "pi.policyfactory.TestORBInitializer", "" );
            orb = ORB.init( (String[]) null, props );

            boolean testStatus = SUCCESS;
            // Test ClientRequestInfo.arguments() method.
            helper.start( "positiveTest" ) ;
            testStatus = positiveTest();
            if( testStatus == SUCCESS ) {
                System.out.println( "PolicyFactory positive tests Success" );
                System.out.flush();
                helper.pass() ;
            } else {
                System.err.println( "PolicyFactory positive tests Failure" );
                System.err.flush();
                signalError (); 
                helper.fail( msg ) ;
            }

            helper.start( "negativeTest" ) ;
            testStatus = negativeTest();
            if( testStatus == SUCCESS ) {
                System.out.println( "PolicyFactory negative tests Success" );
                System.out.flush();
                helper.pass() ;
            } else {
                System.err.println( "PolicyFactory negative tests Failure" );
                System.err.flush();
                signalError (); 
                helper.fail( msg ) ;
            }
        } catch( Exception e ) {
            System.err.println( "PolicyFactory test Failed with exception" + e);
            System.err.flush();
            signalError (); 
        } finally {
            helper.done() ;
        }
    }

    /** This method tests 
     *  1. To see whether the Policy created with type 100 is created from 
     *     PolicyFactoryHundred. This check is made by testing 
     *     whether policy.policy_type method returns 100.
     *  2. To see whether the Policy created with type 10000 is created from 
     *     PolicyFactoryThousandPlus. This check is made by testing 
     *     whether policy.policy_type method returns 10000.
     */
    private boolean positiveTest( ) {
        org.omg.CORBA.Policy policy = null;
        Any any = orb.create_any() ;
        try {
            policy = orb.create_policy( 100, any );
        }
        catch( Exception e) {
            msg = "PolicyFactoryTest.positiveTest failed with " + " an Exception " + e ;
            System.err.println( msg ) ;
            System.err.flush( );
            e.printStackTrace();
            return FAILURE;
        }
        if( policy == null ) {
            msg = "PolicyFactoryTest.positiveTest failed because"+
                " policy is not created as expected " ;
            System.err.println( msg ) ;
            System.err.flush( );
            return FAILURE;
        }
        if( policy.policy_type() != 100 ) {
            msg = "PolicyFactoryTest.positiveTest failed because"+
                " policy.policy_type() != 100 " ;
            System.err.println( msg ) ;
            System.err.flush( );
            return FAILURE;
        }   
        try {
            policy = orb.create_policy( 10000, any );
        } catch( Exception e ) {  
            msg = "PolicyFactoryTest.positiveTest failed with " +
                " an Exception " + e ;
            System.err.println( msg ) ;
            System.err.flush( );
            e.printStackTrace();
            return FAILURE;
        }
        if( policy == null ) {
            msg = "PolicyFactoryTest.positiveTest failed because"+
                " policy is not created as expected " ;
            System.err.println( msg ) ;
            System.err.flush( );
            return FAILURE;
        }
        if( policy.policy_type() != 10000 ) {
            msg = "PolicyFactoryTest.positiveTest failed because"+
                " policy.policy_type() != 10000 " ;
            System.err.println( msg ) ;
            System.err.flush( );
            return FAILURE;
        }   
        return SUCCESS;
    }

    /** This method tests to see whether the Policy could be created with 
     *  type 100000 for which there is no PolicyFactory registered.
     *  Before invoking this methos the ORBInitializer (TestORBInitializer)
     *  registers 3 policy factories with types 100, 1000 and 1000000. If the 
     *  call to create policy with type 100000 does not raise policy error 
     *  then it's an error.
     */
    private boolean negativeTest( ) {
        try {
            Any any = orb.create_any() ;
            org.omg.CORBA.Policy policy = orb.create_policy( 100000, any );
        } 
        catch( org.omg.CORBA.PolicyError e ) {
            msg = "Caught org.omg.CORBA.PolicyError in " +
                "PolicyFactory.negativeTest() as expected..." ;
            System.out.println( msg ) ;
            System.out.flush( );
            if( e.reason != BAD_POLICY.value ) {
                return FAILURE;
            }
            return SUCCESS;
        }
        return FAILURE;
    }
       
}
