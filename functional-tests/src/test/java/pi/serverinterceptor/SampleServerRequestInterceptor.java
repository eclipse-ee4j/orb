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

package pi.serverinterceptor;

import org.omg.CORBA.*;

import org.omg.PortableInterceptor.ForwardRequest;
import org.omg.PortableInterceptor.RequestInfo;
import org.omg.PortableInterceptor.ServerRequestInfo;
import org.omg.PortableInterceptor.ServerRequestInterceptor;

//import ORBInitTest.*;

/**
 * Sample ServerRequestInterceptor for use in testing
 */
public class SampleServerRequestInterceptor 
    extends org.omg.CORBA.LocalObject 
    implements ServerRequestInterceptor
{
    // This string is modified from within this class and from Server.java.
    // It keeps track of which method was invoked when by appending 
    // two-letter codes in succession.  The following codes are used
    // <name> represents the name of the interceptor invoked.  This is
    // used to check order of interceptor invocation.
    //
    //  rs<name> = receive_request_service_contexts
    //  rr<name> = receive_request
    //  sr<name> = send_reply
    //  se<name> = send_exception
    //  so<name> = send_other
    // For example, rs1rs2rr1rr2sr2sr1 would indicate a normal invocation.
    public static String invocationOrder = "";

    // This string is modified from within helloDelegate.  It is appended to
    // every time a relevant method is invoked so that we make check to make
    // sure the methods are invoked on the correct objects for each test.
    public static String methodOrder = "";

    // The message to embed in exceptions so they can be checked for validity.
    public static final String VALID_MESSAGE = "Valid Test Result.";
    
    // This attribute is set by Server.java to indicate how this interceptor
    // should behave.  There are a predetermined set of behavior values:
    //   MODE_NORMAL - All interceptors exit without throwing an Exception
    //   MODE_RRSC_SYSTEM_EXCEPTION - Interceptors 1 and 3 return normally,
    //     while interceptor 2 throws a SYSTEM_EXCEPTION from rrsc.
    //   MODE_RRSC_FORWARD_REQUEST - Interceptors 1 and 3 return normally,
    //     while interceptor 2 throws a ForwardRequest from rrsc.
    //   MODE_RR_SYSTEM_EXCEPTION - Interceptors 1 and 3 return normally,
    //     while interceptor 2 throws a SYSTEM_EXCEPTION from rr.
    //   MODE_RR_FORWARD_REQUEST - Interceptors 1 and 3 return normally,
    //     while interceptor 2 throws a ForwardRequest from rr.
    //   MODE_SR_SYSTEM_EXCEPTION - Interceptors 1 and 3 return normally,
    //     while interceptor 2 throws a SYSTEM_EXCEPTION from sr.

    private static int testMode;
    
    public static final int MODE_NORMAL                = 0;
    public static final int MODE_RRSC_SYSTEM_EXCEPTION = 1;
    public static final int MODE_RRSC_FORWARD_REQUEST  = 2;
    public static final int MODE_RR_SYSTEM_EXCEPTION   = 3;
    public static final int MODE_RR_FORWARD_REQUEST    = 4;
    public static final int MODE_SR_SYSTEM_EXCEPTION   = 5;
    public static final int MODE_SE_SYSTEM_EXCEPTION   = 6;
    public static final int MODE_SE_FORWARD_REQUEST    = 7;
    public static final int MODE_SO_SYSTEM_EXCEPTION   = 8;
    public static final int MODE_SO_FORWARD_REQUEST    = 9;

    // This is necessary because we invoke ending points once before the
    // actual invocation sequence we are recording (see the sequence
    // diagram in ServerCommon.checkOrder) and because the mode is reset
    // after the first time the mode flag becomes relevant.
    private static int endpointSkip;
    
    private String name;

    // Counter to make sure each start is mated by an end
    public static int callCounter = 0;

    // Normally, _is_a invocations are not recorded.  This is because _is_a
    // is invoked often by RMI lookup code.  If this flag is set,
    // _is_a invocations are recorded at most once.  This is useful for
    // when we actually want to record interception on the _is_a "special op".
    static boolean dontIgnoreIsA = false;

    public static boolean printPointEntryFlag = false;

    private void printPointEntry( String message, RequestInfo ri )
    {
        if (printPointEntryFlag) {
            System.out.println(message +
                               " " + ri.request_id() +
                               " " + ri.operation() +
                               " " + callCounter);
        }
    }

    public SampleServerRequestInterceptor( String name ) {
        this.name = name;
    }

    public String name() {
        return name;
    }

    public void destroy() {
    }

    public void receive_request_service_contexts (ServerRequestInfo ri) 
        throws ForwardRequest 
    {
        callCounter++; // Starting point - add
        printPointEntry("receive_request_service_contexts", ri);

        // Ignore any calls to _is_a since this happens quite often for
        // the RMI case and we are not interested in recording those.
        if( !dontIgnoreIsA && ri.operation().equals( "_is_a" ) ) {
            if( name.equals( "1" ) ) {
                System.out.println( 
                    "    - Interceptor: Ignoring _is_a call..." );
            }
        }
        else {
            // Log that we did a receive_request_service_contexts
            // on this interceptor so we can
            // verify invocation order was correct in test.
            invocationOrder += "rs" + name;

            if( name.equals( "2" ) ) {
                if( testMode == MODE_RRSC_SYSTEM_EXCEPTION ) {
                    // Reset to original test mode:
                    testMode = MODE_NORMAL;

                    // If we are the second interceptor, it is our turn to
                    // throw a SystemException here.

                    // Since this starting point is throwing an exception
                    // an ending point will not be called.  Therefore,
                    // explicitly decrement the call counter.
                    callCounter--;

                    throw new IMP_LIMIT( VALID_MESSAGE );
                }
                else if( testMode == MODE_RRSC_FORWARD_REQUEST ) {
                    testMode = MODE_NORMAL;

                    // Since this starting point is throwing an exception
                    // an ending point will not be called.  Therefore,
                    // explicitly decrement the call counter.
                    callCounter--;

                    throw new ForwardRequest( TestInitializer.helloRefForward );
                }
            }
        }
    }

    public void receive_request (ServerRequestInfo ri) 
        throws ForwardRequest
    {
        printPointEntry("receive_request", ri);

        // Ignore any calls to _is_a since this happens quite often for
        // the RMI case and we are not interested in recording those.
        if( dontIgnoreIsA || !ri.operation().equals( "_is_a" ) ) {
            // Log that we did a receive_request on this interceptor so we can
            // verify invocation order was correct in test.
            invocationOrder += "rr" + name;

            if( name.equals( "2" ) ) {
                if( testMode == MODE_RR_SYSTEM_EXCEPTION ) {
                    // Reset to original test mode:
                    testMode = MODE_NORMAL;

                    // If we are the second interceptor, it is our turn to
                    // throw a SystemException here.
                    throw new IMP_LIMIT( VALID_MESSAGE );
                }
                else if( testMode == MODE_RR_FORWARD_REQUEST ) {
                    testMode = MODE_NORMAL;
                    throw new ForwardRequest( TestInitializer.helloRefForward );
                }
            }
        }
    }

    public void send_reply (ServerRequestInfo ri) 
    {
        printPointEntry("send_reply", ri);
        callCounter--; // Ending point - subtract

        // Ignore any calls to _is_a since this happens quite often for
        // the RMI case and we are not interested in recording those.
        if( dontIgnoreIsA || !ri.operation().equals( "_is_a" ) ) {
            // Log that we did a send_reply on this interceptor so we can
            // verify invocation order was correct in test.
            invocationOrder += "sr" + name;

            if( name.equals( "2" ) ) {
                if( endpointSkip > 0 ) {
                    endpointSkip--;
                }
                else if( testMode == MODE_SR_SYSTEM_EXCEPTION ) {
                    // Reset to original test mode:
                    testMode = MODE_NORMAL;

                    // If we are the second interceptor, it is our turn to
                    // throw a SystemException here.
                    throw new IMP_LIMIT( VALID_MESSAGE );
                }
            }
        }

        // Reset dontIgnoreIsA so that the most number of times _is_a is
        // ever processed in a single call is once.
        if( name.equals( "1" ) && ri.operation().equals( "_is_a" ) ) {
            dontIgnoreIsA = false;
        }
    }

    public void send_exception (ServerRequestInfo ri) 
        throws ForwardRequest
    {
        printPointEntry("send_exception", ri);
        callCounter--; // Ending point - subtract

        try {
            System.out.println( "re: " + ri.exceptions()[0].id() );
        }
        catch( Exception e ) {
        }

        // Ignore any calls to _is_a since this happens quite often for
        // the RMI case and we are not interested in recording those.
        if( dontIgnoreIsA || !ri.operation().equals( "_is_a" ) ) {
            // Log that we did a send_exception on this interceptor so we can
            // verify invocation order was correct in test.
            invocationOrder += "se" + name;

            if( name.equals( "2" ) ) {
                if( endpointSkip > 0 ) {
                    endpointSkip--;
                }
                else if( testMode == MODE_SE_SYSTEM_EXCEPTION ) {
                    // Reset to original test mode:
                    testMode = MODE_NORMAL;

                    // If we are the second interceptor, it is our turn to
                    // throw a SystemException here.
                    throw new IMP_LIMIT( VALID_MESSAGE );
                }
                else if( testMode == MODE_SE_FORWARD_REQUEST ) {
                    testMode = MODE_NORMAL;
                    throw new ForwardRequest( TestInitializer.helloRefForward );
                }
            }
        }

        // Reset dontIgnoreIsA so that the most number of times _is_a is
        // ever processed in a single call is once.
        if( name.equals( "1" ) && ri.operation().equals( "_is_a" ) ) {
            dontIgnoreIsA = false;
        }
    }

    public void send_other (ServerRequestInfo ri) 
        throws ForwardRequest 
    {
        printPointEntry("send_other", ri);
        callCounter--; // Ending point - subtract

        // Ignore any calls to _is_a since this happens quite often for
        // the RMI case and we are not interested in recording those.
        if( dontIgnoreIsA || !ri.operation().equals( "_is_a" ) ) {
            // Log that we did a send_other on this interceptor so we can
            // verify invocation order was correct in test.
            invocationOrder += "so" + name;

            if( name.equals( "2" ) ) {
                if( endpointSkip > 0 ) {
                    endpointSkip--;
                }
                else if( testMode == MODE_SO_SYSTEM_EXCEPTION ) {
                    // Reset to original test mode:
                    testMode = MODE_NORMAL;

                    // If we are the second interceptor, it is our turn to
                    // throw a SystemException here.
                    throw new IMP_LIMIT( VALID_MESSAGE );
                }
                else if( testMode == MODE_SO_FORWARD_REQUEST ) {
                    testMode = MODE_NORMAL;
                    throw new ForwardRequest( TestInitializer.helloRefForward );
                }
            }
        }

        // Reset dontIgnoreIsA so that the most number of times _is_a is
        // ever processed in a single call is once.
        if( name.equals( "1" ) && ri.operation().equals( "_is_a" ) ) {
            dontIgnoreIsA = false;
        }
    }

    public static void setTestMode( int testMode ) {
        SampleServerRequestInterceptor.testMode = testMode;
        SampleServerRequestInterceptor.endpointSkip = 1;
    }

}
