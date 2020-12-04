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

package pi.clientrequestinfo;

import com.sun.corba.ee.spi.misc.ORBConstants;
import java.io.PrintStream;
import java.util.Properties;
import org.glassfish.pfl.test.JUnitReportHelper;
import org.omg.CORBA.ORB;

/**
 * Common methods for Client implementations in this test to use
 */
abstract public class ClientCommon
{
    // Set from run()
    protected com.sun.corba.ee.spi.orb.ORB orb;
    
    // Set from run()
    public PrintStream out;
    
    // Set from run()
    public PrintStream err;

    // Strategy for current run
    protected InterceptorStrategy interceptorStrategy;

    // Strategy for current run
    protected InvokeStrategy invokeStrategy;

    // The current Client being executed
    public static ClientCommon client;

    JUnitReportHelper helper = new JUnitReportHelper( this.getClass().getName() ) ;
    
    protected void finish() {
        helper.done() ;
    }

    /**
     * Creates a com.sun.corba.ee.spi.orb.ORB and notifies the TestInitializer of its presence
     */
    protected void createORB( String[] args ) {
        // create and initialize the ORB with initializer
        String testInitializer = "pi.clientrequestinfo.TestInitializer";
        Properties props = new Properties() ;
        props.put( "org.omg.CORBA.ORBClass", 
                   System.getProperty("org.omg.CORBA.ORBClass"));
        props.put( ORBConstants.PI_ORB_INITIALIZER_CLASS_PREFIX + 
                   testInitializer, "" );
        this.orb = (com.sun.corba.ee.spi.orb.ORB)ORB.init(args, props);
        TestInitializer.orb = this.orb;
    }

    /**
     * Perform common ClientRequestInfo tests
     */
    protected void testClientRequestInfo() throws Exception {
        out.println();
        out.println( "Running common ClientRequestInfo tests" );
        out.println( "======================================" );

        client = this;

        testRequestId();
        testAttributesValid();
        testOneWay();
        testServiceContext();

        testForwardReference();

        // _REVISIT_ Waiting on new IOR code to be checked in.
        //testEffectiveProfile();

        testException();
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
     * Prepars for a test invocation by setting the interceptor strategy
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
            out.println( "  - Resolving references." );
            resolveReferences();
            out.println( "  - Executing test " + testName + "." );
            SampleClientRequestInterceptor.strategy = interceptorStrategy;
            invokeStrategy.invoke();
            if( interceptorStrategy.failed ) {
                throw new RuntimeException( interceptorStrategy.failReason );
            }
            helper.pass() ;
        } catch (Exception exc) {
            helper.fail( exc ) ;
            throw exc ;
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
        runTestCase( "request_id.1" );

        // Test request_id is unique for each currently active
        // request/reply sequence:
        interceptorStrategy = new RequestId2Strategy();
        invokeStrategy = new InvokeRecursive();
        setParameters( interceptorStrategy, invokeStrategy );
        runTestCase( "request_id.2" );
    }

    /**
     * Tests various attributes are valid.  Attributes tested:
     *    operation(), sync_scope(), reply_status()
     */
    protected void testAttributesValid() 
        throws Exception 
    {
        out.println( "+ Testing for valid attributes..." );

        InterceptorStrategy interceptorStrategy = 
            new AttributesValidStrategy();
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
     * and add_request_service_context().
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
     * Tests effective_profile()
     */
    protected void testEffectiveProfile()
        throws Exception
    {
        out.println( "+ Testing effective_profile()..." );

        InterceptorStrategy interceptorStrategy = 
            new EffectiveProfileStrategy();
        InvokeStrategy invokeStrategy = new InvokeVisitAll();
        setParameters( interceptorStrategy, invokeStrategy );
        runTestCase( "effective_profile" );
    }

    /**
     * Tests received_exception() and received_exception_id()
     */
    protected void testException()
        throws Exception
    {
        out.println( "+ Testing received_exception[_id]()..." );

        InterceptorStrategy interceptorStrategy = 
            new ExceptionStrategy();
        InvokeStrategy invokeStrategy = new InvokeExceptions();
        setParameters( interceptorStrategy, invokeStrategy );
        runTestCase( "received_exception[_id]" );
    }

}

