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

package pi.clientinterceptor;

import com.sun.corba.ee.spi.misc.ORBConstants;
import java.io.PrintStream;
import java.util.Properties;
import org.glassfish.pfl.test.JUnitReportHelper;
import org.omg.CORBA.ORB;
import org.omg.CORBA.UNKNOWN;

/**
 * Common methods for Client implementations in this test to use
 */
abstract public class ClientCommon
{
    // Set from run()
    protected com.sun.corba.ee.spi.orb.ORB orb;
    
    // Set from run()
    protected PrintStream out;
    
    // Set from run()
    protected PrintStream err;
   
    private JUnitReportHelper helper = new JUnitReportHelper( this.getClass().getName() ) ;

    /**
     * Creates a com.sun.corba.ee.spi.orb.ORB and notifies the TestInitializer of its presence
     */
    protected void createORB( String[] args ) {
        // create and initialize the ORB with initializer
        String testInitializer = "pi.clientinterceptor.TestInitializer";
        Properties props = new Properties() ;
        props.put( "org.omg.CORBA.ORBClass", 
                   System.getProperty("org.omg.CORBA.ORBClass"));
        props.put( ORBConstants.PI_ORB_INITIALIZER_CLASS_PREFIX +
                   testInitializer, "" );
        this.orb = (com.sun.corba.ee.spi.orb.ORB)ORB.init(args, props);
        TestInitializer.orb = this.orb;
    }

    protected void finish() {
        helper.done() ;
    }

    /**
     * Perform common ClientRequestInterceptor tests
     */
    protected void testClientInterceptor() throws Exception {
        out.println();
        out.println( "Running common ClientRequestInterceptor tests" );
        out.println( "=============================================" );

        // No exceptions thrown.  Should call send_request, sayHello, and
        // then receive_reply on all 3 interceptors in the correct order
        out.println( "- Testing standard invocation..." );
        testInvocation( "standardInvocation",
            SampleClientRequestInterceptor.MODE_NORMAL,
            "sr1sr2sr3rr3rr2rr1", "sayHello", true, false, false );

        // SYSTEM_EXCEPTION thrown by server.  Should call send_request,
        // saySystemException, and then receive_exception on all 3 
        // interceptors in the correct order.
        out.println( "- Testing invocation resulting in SYSTEM_EXCEPTION..." );
        testInvocation( "systemExceptionResult",
            SampleClientRequestInterceptor.MODE_NORMAL,
            "sr1sr2sr3re3re2re1", "sayException", true, true, false );
        
        // This is a one-way call, so receive_other will be invoked.
        // Should call send_request, sayOther, and then receive_other
        // on all 3 interceptors in the correct order.
        out.println( 
            "- Testing oneway invocation resulting in receive_other..." );
        testInvocation( "receiveOtherResult",
            SampleClientRequestInterceptor.MODE_NORMAL,
            "sr1sr2sr3ro3ro2ro1", "sayOneway", true, false, false );
        
        // SYSTEM_EXCEPTION thrown in send_request for second interceptor.
        // Should result in send_request being called for 1 and 2, but
        // not 3, and receive_exception being called for 1 only.  The
        // method sayHello should never be invoked.
        out.println(
            "- Testing standard invocation where interceptor #2 " +
            "throws exception." );
        testInvocation( "interceptor2SystemException",
            SampleClientRequestInterceptor.MODE_SYSTEM_EXCEPTION,
            "sr1sr2re1", "sayHello", false, true, false );
        
        // SYSTEM_EXCEPTION thrown in receive_reply for second interceptor.
        // Should result in send_request being called for all, 
        // receive_reply called for 3, and 2, and receive_exception for
        // 1.
        out.println(
            "- Testing standard invocation where interceptor #2 " +
            "throws system exception in receive_reply." );
        testInvocation( "interceptor2SystemExceptionReceiveReply",
            SampleClientRequestInterceptor.MODE_RECEIVE_REPLY_EXCEPTION,
            "sr1sr2sr3rr3rr2re1", "sayHello", true, true, false );
        
        // SYSTEM_EXCEPTION thrown in receive_reply for second interceptor.
        // Should result in send_request being called for all, 
        // receive_other called for 3, and 2, and receive_exception for
        // 1.
        out.println(
            "- Testing oneway invocation where interceptor #2 " +
            "throws SYSTEM_EXCEPTION in receive_other." );
        testInvocation( "interceptor2SystemExceptionReceiveOther",
            SampleClientRequestInterceptor.MODE_RECEIVE_OTHER_EXCEPTION,
            "sr1sr2sr3ro3ro2re1", "sayOneway", true, true, false );

        // ForwardRequest thrown in send_request for second interceptor.
        // Should result in send_request being called for 1 and 2, but
        // not 3, and receive_other being called for 1 only.  The
        // method sayHello should never be invoked.
        out.println(
            "- Testing standard invocation where interceptor #2 " +
            "throws ForwardRequest." );
        testInvocation( "interceptor2ForwardRequest",
            SampleClientRequestInterceptor.MODE_FORWARD_REQUEST,
            "sr1sr2ro1sr1sr2sr3rr3rr2rr1", "sayHello", true, false, true );
        
        // ForwardRequest thrown in receive_exception for second interceptor.
        // Should result in send_request being called for all, 
        // receive_exception called for 3, and 2, and receive_other for
        // 1.
        orb.setDebugFlag( "interceptor" ) ;
        orb.setDebugFlag( "subcontract" ) ;
        try {
            out.println(
                "- Testing standard invocation where interceptor #2 " +
                "throws ForwardRequest in receive_exception." );
            testInvocation( "intercepto2ForwardRequestReceiveException",
                SampleClientRequestInterceptor.MODE_RECEIVE_EXCEPTION_FORWARD,
                "sr1sr2sr3re3re2ro1sr1sr2sr3re3re2re1", "sayException", 
                true, true, true );
        } finally {
            orb.clearDebugFlag( "interceptor" ) ;
            orb.clearDebugFlag( "subcontract" ) ;
        }

        // Check that call counter is zero (balanced starting points with
        // ending points)
        out.print(
            "- Checking call counter: " + 
            SampleClientRequestInterceptor.callCounter + " " );

        if( SampleClientRequestInterceptor.callCounter == 0 ) {
            out.println( "(ok)" );
        }
        else {
            out.println( "(error - should be 0)" );
            throw new RuntimeException( "Call counter should be 0!" );
        }
    }

    /**
     * Perform POA special operations test
     * (only invoked from POA clients)
     */
    protected void testSpecialOperations()
        throws Exception
    {
        out.println();
        out.println( "Running Special Operations Tests" );
        out.println( "================================" );

        // Note: shouldInvoke is false because we have no hook code on
        // the server for these operations so we cannot tell if it was
        // actually invoked.

        out.println( "+ Testing _is_a..." );
        testInvocation( "isATest",
            SampleClientRequestInterceptor.MODE_NORMAL,
            "sr1sr2sr3rr3rr2rr1", "_is_a", false, false, false );

        // Expected exception return value since we do not implement this
        // method.
        out.println( "+ Testing _get_interface_def..." );
        testInvocation( "getInterfaceDefTest",
            SampleClientRequestInterceptor.MODE_NORMAL,
            "sr1sr2sr3re3re2re1", "_get_interface_def", false, false, false );

        out.println( "+ Testing _non_existent..." );
        testInvocation( "nonExistentTest",
            SampleClientRequestInterceptor.MODE_NORMAL,
            "sr1sr2sr3rr3rr2rr1", "_non_existent", false, false, false );
    }
    
    /**
     * Clear invocation flags of helloRef and helloRefForward
     */
    abstract protected void clearInvoked() throws Exception;

    /**
     * Invoke the method with the given name on the object
     */
    abstract protected void invokeMethod( String name ) throws Exception;

    /**
     * Return true if the method was invoked
     */
    abstract protected boolean wasInvoked() throws Exception;

    /**
     * Return true if the method was forwarded 
     */
    abstract protected boolean didForward() throws Exception;

    /**
     * Re-resolves all references to eliminate any cached ForwardRequests
     * from the last invocation.
     */
    abstract protected void resolveReferences() throws Exception;

    /** 
     * Tests a standard invocation by resolving a reference to helloServer
     * and making an invcation, recording the interceptor invocation ordering.
     * @param mode - See SampleClientRequestIntreceptor.testMode for more 
     *     details of mode parameter.  
     * @param correctOrder - See SampleClientRequestInterceptor.
     *     invocationOrder for more details on correctOrder.  
     * @param methodName is either "sayHello", "sayException", or "sayOther"
     * @param shouldInvokeTarget - True if the method should have been invoked,
     *     or false if not.
     * @param exceptionExpected - True if it is expected that this call
     *     should return an UNKNOWN exception with the message 
     *     "Valid Test Result" embedded within.
     * @param forwardExpected - True if it is expected that this call
     *     should result in a forward to another object and that that object
     *     should be invoked.
     */
    protected void testInvocation( String testName, int mode, 
                                   String correctOrder,
                                   String methodName,
                                   boolean shouldInvokeTarget,
                                   boolean exceptionExpected,
                                   boolean forwardExpected ) 
        throws Exception 
    {
        helper.start( testName ) ;

        try {
            // Tell interceptor to behave while clearing invocation flag:
            SampleClientRequestInterceptor.testMode = 
                SampleClientRequestInterceptor.MODE_NORMAL;

            resolveReferences();

            // Clear invocation flags in helloRef and helloRefForward:
            clearInvoked();
            SampleClientRequestInterceptor.testMode = mode;
            
            // Clear invocation order.  It is critical that this is done after
            // clearInvoked is called so that we do not record the interceptors
            // called during the clearInvoked() invocation itself.
            SampleClientRequestInterceptor.invocationOrder = "";
            
            try {
                // Invoke the method.
                invokeMethod( methodName );
            } catch( UNKNOWN e ) {
                // Check to make sure we expected this exception.
                if( !exceptionExpected ) {
                    throw e;
                }
            }
            
            // Tell interceptor to behave while analyzing results:
            SampleClientRequestInterceptor.testMode = 
                SampleClientRequestInterceptor.MODE_NORMAL;
            
            // Examine invocation order to ensure everything was called in the
            // right order.
            //
            // NOTE: This is merely a convenient way to check ordering.
            // Since the specification does not require that the order in which
            // interceptors are registered match the order in which they are
            // invoked, the actual invocationOrder may be different but still
            // valid.  This test may need modification if we change the way
            // we determine the initial invocation order of interceptors.
            String order = SampleClientRequestInterceptor.invocationOrder;
            checkOrder( correctOrder, order );
            
            // Determine if the method was invoked when it was supposed to be
            // or not invoked when it was not supposed to be.

            // But first, if this was a oneway call, leave sufficient time for
            // the call to get there. 
            if( methodName.equals( "sayOneway" ) ) {
                try {
                    // 2 seconds *should* almost always be enough.
                    Thread.sleep( 2000 );
                }
                catch( InterruptedException e ) {
                }
            }

            boolean didInvoke = wasInvoked();
            boolean didForward = didForward();
            
            out.println( "    + Should inovke method: " + shouldInvokeTarget );
            out.println( "    + Did invoke method: " + didInvoke );
            if( didInvoke != shouldInvokeTarget ) {
                throw new RuntimeException( "Method should " + 
                    (!shouldInvokeTarget ? "not" : "") + " have been invoked!" );
            }
            out.println( "    + Should forward and invoke: " + forwardExpected );
            out.println( "    + Did forward and invoke: " + didForward );
            if( didForward != forwardExpected ) {
                throw new RuntimeException( "Method should " +
                    (!forwardExpected ? "not" : "") + " have forwarded!" );
            }

            helper.pass() ;
        } catch (Exception exc) {
            helper.fail( exc ) ;
            throw exc ;
        }
    }

    /**
     * Checks the invocation order against the correct invocation order,
     * displays some debug output and throws an Exception if they do not
     * match.
     */
    private void checkOrder( String correctOrder, String order ) 
        throws Exception 
    {
        out.println( "    + Expected invocation order: " + correctOrder );
        out.println( "    + Actual invocation order: " + order );
        if( !order.equals( correctOrder ) ) {
            out.println( "    + MISMATCH.  Exiting." );
            throw new Exception( "Invocation order mismatch." );
        }
    }
    
}

