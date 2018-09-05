/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package pi.serverrequestinfo;

import com.sun.corba.ee.spi.misc.ORBConstants;
import corba.framework.InternalProcess;
import java.io.PrintStream;
import java.util.Properties;
import org.glassfish.pfl.test.JUnitReportHelper;
import org.omg.CORBA.ORB;

/**
 * This test works similar to the way the serverinterceptor test works.
 * The server controls the client by waitinf for a syncWithServer call
 * and then returns the name of the method to invoke.  The method is invoked
 * and on the next syncWithServer() call, the results are checked.
 *
 *      Client                      Server
 *        |                           |
 *       [ ]     syncWithServer()     | 
 *       [ ]------------------------>[ ]     rs1 rs2 rs3 rr1 rr2 rr3
 *       [ ]                         [ ]
 *       [ ]     "sayHello"          [ ]   // order cleared here
 *       [ ]<------------------------[ ]     sr3 sr2 sr1
 *       [ ]                          |
 *       [ ]     sayHello()           |    // <important>
 *       [ ]------------------------>[ ]     rs1 rs2 rs3 rr1 rr2 rr3
 *       [ ]                         [ ]
 *       [ ]     "hello there"       [ ]
 *       [ ]<------------------------[ ]     sr3 sr2 sr1
 *       [ ]                          |    // </important>
 *       [ ]     syncWithServer()     |
 *       [ ]------------------------>[ ]     rs1 rs2 rs3 rr1 rr2 rr3
 *       [ ]                         [ ] 
 *       [ ]                         [ ]   // check order here
 *       [ ]                         [ ]   // next test begins soon after.
 *       ...                         ...
 */
public abstract class ServerCommon
    implements InternalProcess 
{
    // Set from run()
    com.sun.corba.ee.spi.orb.ORB orb;
    
    // Set from run()
    PrintStream out;
    
    // Set from run()
    PrintStream err;

    // Strategy for current run
    InterceptorStrategy interceptorStrategy;

    // Strategy for current run
    InvokeStrategy invokeStrategy;

    // True if the client is currently waiting for syncWithServer to return
    // or false otherwise.
    static boolean syncing = false;
    
    // The name of the next method to invoke:
    static String nextMethodToInvoke;
    
    // An object for syncWithServer to wait on before returning to the client.
    static final Integer syncObject = new Integer( 0 );
    
    // Constant string to indicate to the client that we are done.
    static final String EXIT_METHOD = "exit";

    // Set to true by syncWithServer if the last invocation resulted in
    // an exception on the client side.
    static boolean exceptionRaised;

    // The currently executing server.
    static ServerCommon server;

    private JUnitReportHelper helper = new JUnitReportHelper( this.getClass().getName() ) ;

    protected void finish() {
        helper.done() ;
    }

    /**
     * Creates a com.sun.corba.ee.spi.orb.ORB and notifies the TestInitializer of its presence
     */
    void createORB( String[] args, Properties props ) {
        // create and initialize the ORB with initializer
        String testInitializer = "pi.serverrequestinfo.TestInitializer";
        props.put( "org.omg.CORBA.ORBClass",
                   System.getProperty("org.omg.CORBA.ORBClass"));
        props.put( ORBConstants.PI_ORB_INITIALIZER_CLASS_PREFIX +
                   testInitializer, "" );
        this.orb = (com.sun.corba.ee.spi.orb.ORB)ORB.init(args, props);
        TestInitializer.orb = this.orb;
    }

    /**
     * Perform common ServerRequestInfo tests
     */
    void testServerRequestInfo() throws Exception {
        out.println();
        out.println( "Running common ServerRequestInfo tests" );
        out.println( "======================================" );

        ServerCommon.server = this;
        waitForClient();
        
        // Run tests:
        testRequestId();
        testAttributesValid();
        testOneWay();
        testForwardReference();
        testServiceContext();
        testException();
        testRequestInfoStack();
    }

    /**
     * Invoke the method with the given name on the object
     */
    protected void invokeMethod( String methodName ) 
        throws Exception
    {
        // We are already synchronized with the client.
        
        // Prepare for the call:
        nextMethodToInvoke = methodName;
        SampleServerRequestInterceptor.methodOrder = "";
        
        // Return from syncWithServer() and let the client make the call:
        synchronized( syncObject ) {
            syncObject.notify();
        }

        // If this is a oneway call, wait an extra second to make sure
        // everything went through okay.
        if( methodName.equals( "sayOneway" ) ) {
            out.println( "    - Delaying for oneway..." );
            try {
                Thread.sleep( 1000 );
            }
            catch( InterruptedException e ) {
            }
            out.println( "    - This should be long enough." );
        }
        
        // Wait for client to synchronize with server again.  Now we know
        // for sure that the method invocation is complete.  Even if the
        // call was a oneway, we can be fairly certain the call has completed
        // since we delay for 0.5 seconds before exiting waitForClient()
        // which should be enough time in most cases.
        waitForClient();
    }

    /**
     * Waits for the client to sync with the server
     */
    void waitForClient() {
        out.println( "    - Waiting for client..." );
        while( !syncing ) {
            try {
                Thread.sleep( 100 );
            }
            catch( InterruptedException e ) {
            }
        }
        
        // Leave enough time for the method and interceptors to finish 
        // invoking so we can clear the invocation history:
        try {
            Thread.sleep( 500 );
        }
        catch( InterruptedException e ) {
        }
        out.println( "    - Synchronized with client." );
    }
    
    /**
     * Notifies the client it is time to exit
     */
    void exitClient() {
        out.println( "+ Notifying client it's time to exit..." );
        nextMethodToInvoke = EXIT_METHOD;
        synchronized( syncObject ) {
            syncObject.notify();
        }
    }

    /**
     * Prepares for a test invocation by setting the interceptor strategy
     * and the invocation and forward objects.
     */
    protected void setParameters( InterceptorStrategy interceptorStrategy,
                                  InvokeStrategy invokeStrategy )
    {
        out.println( "  - Using interceptor strategy " +
            interceptorStrategy.getClass().getName() );
        out.println( "  - Using invocation strategy " +
            invokeStrategy.getClass().getName() );
        this.interceptorStrategy = interceptorStrategy;
        this.invokeStrategy = invokeStrategy;
    }

    /**
     * Executes the test case set up with the parameters in setParameters
     */
    protected void runTestCase( String testName )
        throws Exception
    {
        helper.start( testName ) ;

        try {
            out.println( "  - Executing test " + testName + "." );
            SampleServerRequestInterceptor.strategy = interceptorStrategy;
            SampleServerRequestInterceptor.intercepted = false;
            invokeStrategy.invoke();
            if( interceptorStrategy.failed ) {
                throw new RuntimeException( interceptorStrategy.failReason );
            }
            if( !SampleServerRequestInterceptor.intercepted ) {
                throw new RuntimeException( "No interceptors were invoked!" );
            }

            helper.pass() ;
        } catch (Exception exc) {
            helper.fail( exc ) ;
        }
    }

    /*
     *********************************************************************
     * Test assertions
     *********************************************************************/
   
    /**
     * Tests request_id().
     */
    protected void testRequestId()
        throws Exception
    {
        out.println( "+ Testing request_id()..." );

        // Test request_id is same for request as for reply:
        InterceptorStrategy interceptorStrategy = new RequestId1Strategy();
        InvokeStrategy invokeStrategy = new InvokeVisitAll();
        setParameters( interceptorStrategy, invokeStrategy );
        runTestCase( "request_id" );
    }

    /**
     * Overridden by subclasses.  Calls testAttributesValid( String, String )
     * passing in a valid and invalid repository id for the technology type
     * being used.
     */
    protected abstract void testAttributesValid()
        throws Exception;

    /**
     * Tests various attributes are valid.  Attributes tested:
     *    operation(), sync_scope(), reply_status()
     */
    protected void testAttributesValid( String validRepId, String invalidRepId)
        throws Exception
    {
        out.println( "+ Testing for valid attributes..." );

        InterceptorStrategy interceptorStrategy =
            new AttributesValidStrategy( validRepId, invalidRepId );
        InvokeStrategy invokeStrategy = new InvokeVisitAll();
        setParameters( interceptorStrategy, invokeStrategy );
        runTestCase( "attributes_valid" );
    }

    /**
     * Tests response_expected() by invoking a oneWay method
     */
    protected void testOneWay()
        throws Exception
    {
        out.println( "+ Testing response_expected() with one way..." );

        InterceptorStrategy interceptorStrategy = new OneWayStrategy();
        InvokeStrategy invokeStrategy = new InvokeOneWay();
        setParameters( interceptorStrategy, invokeStrategy );
        runTestCase( "response_expected" );
    }

    /**
     * Tests forward_reference()
     */
    protected void testForwardReference()
        throws Exception
    {
        out.println( "+ Testing forward_reference()..." );

        InterceptorStrategy interceptorStrategy =
            new ForwardReferenceStrategy();
        InvokeStrategy invokeStrategy = new InvokeVisitAllForward();
        setParameters( interceptorStrategy, invokeStrategy );
        runTestCase( "forward_reference" );
    }

    /**
     * Tests get_request_service_context(), get_reply_service_context().
     * and add_reply_service_context().
     */
    protected void testServiceContext()
        throws Exception
    {
        out.println( "+ Testing {get|add}_*_service_context()..." );

        InterceptorStrategy interceptorStrategy =
            new ServiceContextStrategy();
        InvokeStrategy invokeStrategy = new InvokeVisitAll();
        setParameters( interceptorStrategy, invokeStrategy );
        runTestCase( "{get|add}_*_service_context" );
    }

    /**
     * Tests sending_exception() 
     */
    protected void testException()
        throws Exception
    {
        out.println( "+ Testing sending_exception()..." );

        InterceptorStrategy interceptorStrategy =
            new ExceptionStrategy();
        InvokeStrategy invokeStrategy = new InvokeExceptions();
        setParameters( interceptorStrategy, invokeStrategy );
        runTestCase( "sending_exception" );
    }

    /**
     * Tests that, if we make a co-located orb-mediated call from within a 
     * servant, the info stack is exercised.
     */
    protected void testRequestInfoStack()                   
        throws Exception
    {
        out.println( "+ Testing request info stack..." );

        InterceptorStrategy interceptorStrategy =
            new RequestInfoStackStrategy();
        InvokeStrategy invokeStrategy = new InvokeVisitAll( "sayInvokeAgain.");
        setParameters( interceptorStrategy, invokeStrategy );
        runTestCase( "request_info_stack" );
    }

}
