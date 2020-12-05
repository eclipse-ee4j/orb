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

import com.sun.corba.ee.impl.interceptors.*;
import org.omg.PortableInterceptor.*;
import org.omg.IOP.*;
import com.sun.corba.ee.spi.servicecontext.ORBVersionServiceContext;
import org.omg.CORBA.*;
import java.util.*;

/**
 * Strategy to test get_request_service_context and get_reply_service_context.
 * <p>
 * Both get_request_service_context and get_reply_service_context will be
 * tried for each interception point.
 */
public class ServiceContextStrategy
    extends InterceptorStrategy
{
    // Service context ID that is known to be invalid.
    private final static int INVALID_ID = 1234;

    // Fake ids and data:
    private final static int FAKEID1 = 2123;
    private final static int FAKEID2 = 2124;
    private final static int FAKEID3 = 2125;

    private final static byte[] FAKEDATA1 = { 
        (byte)1, (byte)2, (byte)3, (byte)4 };
    private final static byte[] FAKEDATA2 = { 
        (byte)5, (byte)6, (byte)7 };
    private final static byte[] FAKEDATA3 = { 
        (byte)8 };


    public void send_request (
        SampleClientRequestInterceptor interceptor, ClientRequestInfo ri)
        throws ForwardRequest
    {
        super.send_request( interceptor, ri );

        try {
            testGetRequestSC( "send_request", ri );
            //testGetReplySC( "send_request", ri );
            testAddRequestSC( "send_request", ri );
        }
        catch( Exception e ) {
            failException( "send_request", e );
        }
    }

    public void send_poll (
        SampleClientRequestInterceptor interceptor, ClientRequestInfo ri)
    {
        super.send_poll( interceptor, ri );
        // Never called in our ORB.
    }

    public void receive_reply(
        SampleClientRequestInterceptor interceptor, ClientRequestInfo ri)
    {
        super.receive_reply( interceptor, ri );

        try {
            testGetRequestSC( "receive_reply", ri );
            //testGetReplySC( "receive_reply", ri );
            testAddRequestSC( "receive_reply", ri );
        }
        catch( Exception e ) {
            failException( "receive_reply", e );
        }
    }

    public void receive_exception(
        SampleClientRequestInterceptor interceptor, ClientRequestInfo ri)
        throws ForwardRequest
    {
        super.receive_exception( interceptor, ri );

        try {
            testGetRequestSC( "receive_exception", ri );
            //testGetReplySC( "receive_exception", ri );
            testAddRequestSC( "receive_exception", ri );
        }
        catch( Exception e ) {
            failException( "receive_exception", e );
        }
    }

    public void receive_other (
        SampleClientRequestInterceptor interceptor, ClientRequestInfo ri)
        throws ForwardRequest
    {
        super.receive_other( interceptor, ri );

        try {
            testGetRequestSC( "receive_other", ri );
            //testGetReplySC( "receive_other", ri );
            testAddRequestSC( "receive_other", ri );
        }
        catch( Exception e ) {
            failException( "receive_other", e );
        }
    }

    private void testGetRequestSC( String methodName, ClientRequestInfo ri ) {
        // Test to ensure valid and invalid IDs work properly:
        testSC( "request", methodName, ri );
    }

    private void testGetReplySC( String methodName, ClientRequestInfo ri ) {
        if( methodName.equals( "send_request" ) ) {
            // get_reply_service_context is invalid here.
            try {
                ri.get_reply_service_context( 0 );
                fail( methodName + "(): get_reply_service_context() " +
                      "did not raise BAD_INV_ORDER." );
            }
            catch( BAD_INV_ORDER e ) {
                log( methodName + "(): get_reply_service_context() " +
                     "raised BAD_INV_ORDER (ok)" );
            }
        }
        else {
            // Test to ensure valid and invalid IDs work properly:
            testSC( "reply", methodName, ri );
        }
    }

    private void testSC( String reqOrRep, String methodName, 
                         ClientRequestInfo ri ) 
    {
        String header = methodName + "(): get_" + reqOrRep + 
            "_service_context";

        // Test to ensure an invalid ID raises a BAD_PARAM:
        try {
            if( reqOrRep.equals( "request" ) ) {
                ri.get_request_service_context( INVALID_ID );
            }
            else {
                ri.get_reply_service_context( INVALID_ID );
            }
            fail( header + "( INVALID_ID ) did not raise BAD_PARAM." );
        }
        catch( BAD_PARAM e ) {
            log( header + "( INVALID_ID ) raised BAD_PARAM (ok)" );
        }

        // Test to ensure valid ID works properly:
        int id;
        if( reqOrRep.equals( "request" ) ) {
            id = ORBVersionServiceContext.SERVICE_CONTEXT_ID;
        }
        else {
            // _REVISIT_ What do we do here?
            id = 6;
        }

        try {
            ServiceContext sc;
            if( reqOrRep.equals( "request" ) ) {
                sc = ri.get_request_service_context( id );
            }
            else {
                sc = ri.get_reply_service_context( id );
            }

            log( header + "( " + id + " ) exists (ok)." );

            // Commenting out copy test due to "good citizen" assumption.
            /* 
            // Ensure this is a copy and not the real thing:
            byte altered = ++sc.context_data[0];

            if( reqOrRep.equals( "request" ) ) {
                sc = ri.get_request_service_context( id );
            }
            else {
                sc = ri.get_reply_service_context( id );
            }

            if( sc.context_data[0] == altered ) {
                fail( header + "( " + id + " ) is not a copy.  " +
                      "It is the original!" );
            }
            else {
                log( header + "( " + id + " ) is a copy, " +
                     "not the original (ok)" );
            }
            */
        }
        catch( BAD_PARAM e ) {
            fail( header + "( " + id + " ) raised BAD_PARAM" );
        }
    }

    private void testAddRequestSC( String methodName, ClientRequestInfo ri ) {
        String header = methodName + "(): ";

        if( methodName.equals( "send_request" ) ) {
            try {
                // Add a service context
                ServiceContext sc = new ServiceContext( FAKEID2, FAKEDATA2 );
                ri.add_request_service_context( sc, true );
                log( header + "added service context 1." );

                // Check to make sure it's there.
                sc = ri.get_request_service_context( FAKEID2 );
                if( Arrays.equals( sc.context_data, FAKEDATA2 ) ) {
                    log( header + "service context data is valid." );
                }
                else {
                    fail( header + "service context data is invalid." );
                }

                // Try to replace it with replace = true:
                try {
                    sc = new ServiceContext( FAKEID2, FAKEDATA3 );
                    ri.add_request_service_context( sc, true );
                    log( header + "was able to replace context with " +
                         "replace=true." );

                    // Check to make sure it was changed:
                    sc = ri.get_request_service_context( FAKEID2 );
                    if( Arrays.equals( sc.context_data, FAKEDATA3 ) ) {
                        log( header + "service context data is valid." );
                    }
                    else {
                        fail( header + "service context data is invalid." );
                    }
                }
                catch( BAD_INV_ORDER e ) {
                    fail( header + "was not able to replace context with " +
                          "replace=true." );
                }

                // Try to replace it with replace = false.
                try {
                    sc = new ServiceContext( FAKEID2, FAKEDATA1 );
                    ri.add_request_service_context( sc, false );
                    fail( header + "was able to replace context with " +
                          "replace=false." );
                }
                catch( BAD_INV_ORDER e ) {
                    log( header + "was not able to replace context with " +
                         "replace=false." );

                    // Check to make sure it was not changed:
                    sc = ri.get_request_service_context( FAKEID2 );
                    if( Arrays.equals( sc.context_data, FAKEDATA3 ) ) {
                        log( header + "service context data is valid." );
                    }
                    else {
                        fail( header + "service context data is invalid." );
                    }
                }
            }
            catch( BAD_INV_ORDER e ) {
                fail( header + 
                      "denied access to add_request_service_context." );
            }
        }
        else {
            // Ensure we are not able to call add_request_service_context
            // anywhere but in send_request:
            try {
                ServiceContext sc = new ServiceContext( FAKEID1, FAKEDATA1 );
                ri.add_request_service_context( sc, true );
                fail( header + 
                      "invalid access to add_request_service_context." );
            }
            catch( BAD_INV_ORDER e ) {
                log( header + 
                      "denied access to add_request_service_context (ok)." );
            }
        }

        // Test to ensure valid and invalid IDs work properly:
        testSC( "request", methodName, ri );


        // Check that we are being returned a *copy* of the service context.
    }

}
