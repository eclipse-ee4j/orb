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

package pi.orbinit;

import corba.framework.InternalProcess;
import java.io.PrintStream;
import java.util.Hashtable;
import java.util.Properties;
import org.glassfish.pfl.test.JUnitReportHelper;
import org.omg.CORBA.ORB;
import org.omg.IOP.CodecFactory;

public abstract class ClientCommon
    implements InternalProcess 
{
    JUnitReportHelper helper = new JUnitReportHelper( this.getClass().getName() ) ;

    // Set from run()
    private ORB orb;
    
    // Set from run()
    private PrintStream out;
    
    // Set from run()
    private PrintStream err;
    
    private CodecFactory codecFactory;

    public void run( Properties environment, String args[], PrintStream out,
                     PrintStream err, Hashtable extra) 
        throws Exception
    {
        out.println( "Client" );
        out.println( "======" );

        this.out = out;
        this.err = err;
        ClientTestInitializer.out = this.out;

        this.orb = createORB( args );
        ClientTestInitializer.orb = this.orb;

        try {
            // Test ORBInitializer
            testORBInitializer();

            // Test ORBInitInfo
            testORBInitInfo();

            // Test destroy
            testDestroy();
        } finally {
            helper.done() ;
        }
    }

    /**
     * Perform ORBInitializer-related tests
     */
    private void testORBInitializer() {
        helper.start( "testORBInitializer" ) ;

        try {
            out.println();
            out.println( "Testing ORBInitializer" );
            out.println( "======================" );

            // Ensure the test initializer was initialized appropriately.
            out.println( "Verifying testInitializer: " );
            if( !ClientTestInitializer.initializedAppropriately() ) {
                throw new RuntimeException( 
                    "ClientTestInitializer not initialized appropriately." );
            }
            out.println( "  - initialized appropriately. (ok)" );

            if( !ClientTestInitializer.post_post_init() ) {
                throw new RuntimeException( 
                    "ORBInitInfo allowed access after post_init." );
            }
            helper.pass() ;
        } catch (RuntimeException exc) {
            helper.fail( exc ) ;
            throw exc ;
        }
    }

    /**
     * Perform ORBInitInfo-related tests
     */
    private void testORBInitInfo() {
        helper.start( "testORBInitInfo" ) ;

        try {
            // Any tests on ORBInitInfo are actually done inside the 
            // ORBInitializer.  At this point, we just analyze the results of
            // tests that have already run.

            out.println();
            out.println( "Testing ORBInitInfo" );
            out.println( "===================" );

            // Analyze resolve_initial_references results
            out.println( ClientTestInitializer.resolveInitialReferencesResults );
            if( !ClientTestInitializer.passResolveInitialReferences ) {
                throw new RuntimeException( 
                    "resolve_initial_references not functioning properly." );
            }
            else if( !ClientTestInitializer.passResolveInitialReferencesInvalid ) {
                throw new RuntimeException( 
                    "resolve_initial_references not raising InvalidName." );
            }

            // Analyze add_*_interceptor
            out.println( "Testing pre_init add interceptor..." );
            out.println( ClientTestInitializer.preAddInterceptorResult );
            if( !ClientTestInitializer.preAddInterceptorPass ) {
                throw new RuntimeException(
                    "pre_init add interceptor test failed." );
            }

            out.println( "Testing post_init add interceptor..." );
            out.println( ClientTestInitializer.postAddInterceptorResult );
            if( !ClientTestInitializer.postAddInterceptorPass ) {
                throw new RuntimeException(
                    "post_init add interceptor test failed." );
            }

            // Analyze get/set_slot test results
            out.println( "Testing get/set slot from within ORBInitializer..." );
            out.println( ClientTestInitializer.getSetSlotResult );
            if( !ClientTestInitializer.getSetSlotPass ) {
                throw new RuntimeException( "get/set slot test failed." );
            }
 
            helper.pass() ;
        } catch (RuntimeException exc) {
            helper.fail( exc ) ;
            throw exc ;
        }
    }

    /**
     * Test that destroy is called on all interceptors.
     */
    private void testDestroy() 
        throws Exception
    {
        helper.start( "testDestroy" ) ;

        try {
            out.println();
            out.println( "Testing destroy functionality" );
            out.println( "=============================" );

            out.println( "Checking destroy counts before calling destroy..." );
            int clientCount = SampleClientRequestInterceptor.destroyCount;
            int serverCount = SampleServerRequestInterceptor.destroyCount;
            int iorCount = SampleIORInterceptor.destroyCount;
            checkDestroyCount( "Client", 0, clientCount );
            checkDestroyCount( "Server", 0, serverCount );
            checkDestroyCount( "IOR", 0, iorCount );

            out.println( "Calling ORB.destroy..." );
            orb.destroy();

            out.println( 
                "Checking that interceptors' destroy methods were called." );
            clientCount = SampleClientRequestInterceptor.destroyCount;
            serverCount = SampleServerRequestInterceptor.destroyCount;
            iorCount = SampleIORInterceptor.destroyCount;

            checkDestroyCount( "Client", 6, clientCount );
            checkDestroyCount( "Server", 2, serverCount );
            checkDestroyCount( "IOR", 2, iorCount );
            helper.pass() ;
        } catch (Exception exc) {
            helper.fail( exc ) ;
            throw exc ;
        }
    }

    /**
     * Checks that a single interceptor passed the destroy test
     */
    private void checkDestroyCount( String name, int expected, int actual ) 
        throws Exception
    {
        out.println( "* " + name + " interceptor: Expected " + expected + 
            " destroys.  Received " + actual + "." );
        if( expected != actual ) {
            throw new RuntimeException( 
                "Incorrect number of destroys called." );
        }
    }

    abstract protected ORB createORB( String[] args );
}
