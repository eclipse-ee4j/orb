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

import com.sun.corba.ee.impl.misc.*;
import com.sun.corba.ee.impl.interceptors.*;
import org.omg.PortableInterceptor.*;
import org.omg.IOP.*;
import ClientRequestInfo.*;
import org.omg.CORBA.*;

/**
 * Strategy to test received_exception() and received_exception_id().
 * Expected invocation order:
 *     count = 1, send_request, receive_reply
 *     count = 2, send_request, receive_exception (SystemException)
 *     count = 3, send_request, receive_exception (UserException)
 *     count = 4, send_request, receive_other
 * All points are checked in order to assure received_exception() 
 * can only be called in the receive_exception interception point.
 */
public class ExceptionStrategy
    extends InterceptorStrategy
{
    
    private int count = 0;


    // True if this test is being run in DII mode.  In DII mode, all 
    // UserException tests are skipped.
    //
    // _REVISIT_ Remove this special mode once UserExceptions work properly
    // with DII.
    boolean diiMode;

    public ExceptionStrategy() {
        this( false );
    }

    public ExceptionStrategy( boolean diiMode ) {
        this.diiMode = diiMode;
    }

    public void send_request (
        SampleClientRequestInterceptor interceptor, ClientRequestInfo ri)
        throws ForwardRequest
    {
        super.send_request( interceptor, ri );
        
        try { 
            count++;

            testException( "send_request", ri );
        }
        catch( Exception ex ) {
            failException( "send_request", ex );
        }
    }

    public void send_poll (
        SampleClientRequestInterceptor interceptor, ClientRequestInfo ri)
    {
        super.send_poll( interceptor, ri );
        // never executed in our orb.
    }

    public void receive_reply (
        SampleClientRequestInterceptor interceptor, ClientRequestInfo ri)
    {
        super.receive_reply( interceptor, ri );

        try {
            testException( "receive_reply", ri );
        }
        catch( Exception ex ) {
            failException( "receive_reply", ex );
        }
    }


    public void receive_exception (
        SampleClientRequestInterceptor interceptor, ClientRequestInfo ri) 
        throws ForwardRequest
    {
        super.receive_exception( interceptor, ri );

        try {
            testException( "receive_exception", ri );
        }
        catch( Exception ex ) {
            failException( "receive_exception", ex );
        }
    }

    public void receive_other (
        SampleClientRequestInterceptor interceptor, ClientRequestInfo ri) 
        throws ForwardRequest
    {
        super.receive_other( interceptor, ri );

        try {
            testException( "receive_other", ri );
        }
        catch( Exception ex ) {
            failException( "receive_other", ex );
        }
    }

    private void testException( String methodName, 
                                ClientRequestInfo ri ) 
    {
        String header = methodName + "(): ";
        if( methodName.equals( "receive_exception" ) ) {
            if( count == 2 ) {
                // Called for System Exception:
                // Test received_exception:
                Any receivedException = ri.received_exception();
                SystemException sysex = ORBUtility.extractSystemException( 
                    receivedException );
                if( !(sysex instanceof UNKNOWN) ) {
                    fail( header + "received_exception() did not return " +
                          "correct SystemException" );
                }
                else {
                    log( header + "received_exception() returned " +
                         "correct SystemException." );
                }
                
                // Test received_exception_id:
                String exceptionId = ri.received_exception_id();

                log( header + "exceptionId for SystemException is: " + 
                    exceptionId );
                
                if( exceptionId.indexOf( "UNKNOWN" ) == -1 ) {
                    fail( header + "exceptionId incorrect!" );
                }
            }
            else if( count == 3 ) {
                // Skip this test in DII mode:
                if( diiMode ) {
                    log( header + "skipping UserException test for DII" );    
                }
                else {
                    // Called for User Exception:
                    // Test received_exception:
                    Any receivedException = ri.received_exception();

                    ExampleException exception = 
                        ExampleExceptionHelper.extract( receivedException );
                    if( !exception.reason.equals( "valid" ) ) {
                        fail( header + 
                              "received_exception() did not return valid " +
                              "ExampleException" );
                    }
                    else {
                        log( header + "received_exception() is valid." );
                    }

                    // Test received_exception_id:
                    String exceptionId = ri.received_exception_id();

                    log( header + "exceptionId for UserException is: " + 
                        exceptionId );
                    
                    if( exceptionId.indexOf( "ExampleException" ) == -1 ) {
                        fail( header + "exceptionId incorrect!" );
                    }
                }
            }
            else {
                fail( header + "receive_exception should not be " +
                      "called when count = " + count );
            }
        }
        else {
            // We should not be able to access received_exception!
            try {
                ri.received_exception();
                fail( header + 
                      "received_exception() did not raise BAD_INV_ORDER!" );
            }
            catch( BAD_INV_ORDER e ) {
                log( header + "received_exception() raised BAD_INV_ORDER (ok)");
            }
            
            // We should not be able to access received_exception_id!
            try {
                ri.received_exception_id();
                fail( header + 
                      "received_exception_id() did not raise BAD_INV_ORDER!" );
            }
            catch( BAD_INV_ORDER e ) {
                log( header + 
                     "received_exception_id() raised BAD_INV_ORDER (ok)");
            }
        }
    }

}
