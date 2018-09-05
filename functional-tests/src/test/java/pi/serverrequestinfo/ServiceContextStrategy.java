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
    private final static int FAKEID_RRSC = 2126;
    private final static int FAKEID_RR = 2127;

    private final static byte[] FAKEDATA1 = { 
        (byte)1, (byte)2, (byte)3, (byte)4 };
    private final static byte[] FAKEDATA2 = { 
        (byte)5, (byte)6, (byte)7 };
    private final static byte[] FAKEDATA3 = { 
        (byte)8 };
    private final static byte[] FAKEDATA4 = { 
        (byte)2 };
    private final static byte[] FAKEDATA_RRSC = { 
        (byte)1, (byte)8, (byte)2 };
    private final static byte[] FAKEDATA_RR = { 
        (byte)9, (byte)8, (byte)9 };


    public void receive_request_service_contexts (
        SampleServerRequestInterceptor interceptor, ServerRequestInfo ri)
        throws ForwardRequest
    {
        super.receive_request_service_contexts( interceptor, ri );

        try {
            testGetRequestSC( "rrsc", ri );
            //testGetReplySC( "rrsc", ri );
            testAddReplySC( "rrsc", ri );
        }
        catch( Exception e ) {
            failException( "rrsc", e );
        }
    }

    public void receive_request (
        SampleServerRequestInterceptor interceptor, ServerRequestInfo ri)
    {
        super.receive_request( interceptor, ri );

        try {
            testGetRequestSC( "receive_request", ri );
            //testGetReplySC( "receive_request", ri );
            testAddReplySC( "receive_request", ri );
        }
        catch( Exception e ) {
            failException( "receive_request", e );
        }
    }

    public void send_reply (
        SampleServerRequestInterceptor interceptor, ServerRequestInfo ri)
    {
        super.send_reply( interceptor, ri );

        try {
            testGetRequestSC( "send_reply", ri );
            //testGetReplySC( "send_reply", ri );
            testAddReplySC( "send_reply", ri );
        }
        catch( Exception e ) {
            failException( "send_reply", e );
        }
    }

    public void send_exception (
        SampleServerRequestInterceptor interceptor, ServerRequestInfo ri)
        throws ForwardRequest
    {
        super.send_exception( interceptor, ri );

        try {
            testGetRequestSC( "send_exception", ri );
            //testGetReplySC( "send_exception", ri );
            testAddReplySC( "send_exception", ri );
        }
        catch( Exception e ) {
            failException( "send_exception", e );
        }
    }

    public void send_other (
        SampleServerRequestInterceptor interceptor, ServerRequestInfo ri)
        throws ForwardRequest
    {
        super.send_other( interceptor, ri );

        try {
            testGetRequestSC( "send_other", ri );
            //testGetReplySC( "send_other", ri );
            testAddReplySC( "send_other", ri );
        }
        catch( Exception e ) {
            failException( "send_other", e );
        }
    }

    private void testGetRequestSC( String methodName, ServerRequestInfo ri ) {
        // Test to ensure valid and invalid IDs work properly:
        testSC( "request", methodName, ri );
    }

    private void testGetReplySC( String methodName, ServerRequestInfo ri ) {
        if( methodName.equals( "rrsc" ) ||
            methodName.equals( "receive_request" ) ) 
        {
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
                         ServerRequestInfo ri ) 
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

            // Commenting out "copy" test due to "good citizen" assumption.
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

    private void testAddReplySC( String methodName, ServerRequestInfo ri ) {
        String header = methodName + "(): ";

        // For RRSC: Add FAKEID_RRSC, FAKE_DATA_RRSC
        // For RR:   Add FAKEID_RR, FAKE_DATA1
        //           Replace FAKEID_RR, FAKE_DATA_RR, true
        //           Replace FAKEID_RR, FAKE_DATA2, false
        // else:     Check FAKEID_RRSC, FAKE_DATA_RRSC
        //           Check FAKEID_RR, FAKE_DATA_RR
        //           Add FAKEID1, FAKE_DATA3
        //           Replace FAKEID1, FAKE_DATA4, true
        //           Check FAKEID1, FAKE_DATA4
        //           Replace FAKEID1, FAKE_DATA3, false
        //           Check FAKEID1, FAKE_DATA4

        if( methodName.equals( "rrsc" ) ) {
            try {
                // Add FAKEID_RRSC, FAKE_DATA_RRSC
                ServiceContext sc = new ServiceContext( FAKEID_RRSC, 
                                                        FAKEDATA_RRSC );
                ri.add_reply_service_context( sc, true );
                log( header + "added service context RRSC." );
            }
            catch( BAD_INV_ORDER e ) {
                fail( header + 
                      "denied access to add_reply_service_context." );
            }
        }
        else if( methodName.equals( "receive_request" ) ) {
            try {
                // Add FAKEID_RR, FAKE_DATA1
                ServiceContext sc = new ServiceContext( FAKEID_RR, 
                                                        FAKEDATA1 );
                ri.add_reply_service_context( sc, true );
                log( header + "added service context RR." );

                // Replace FAKEID_RR, FAKE_DATA_RR, true
                try {
                    sc = new ServiceContext( FAKEID_RR, FAKEDATA_RR );
                    ri.add_reply_service_context( sc, true );
                    log( header + "was able to replace context with " +
                         "replace=true." );
                }
                catch( BAD_INV_ORDER e ) {
                    fail( header + "was not able to replace context with " +
                          "replace=true." );
                }

                // Replace FAKEID_RR, FAKE_DATA2, false
                try {
                    sc = new ServiceContext( FAKEID_RR, FAKEDATA2 );
                    ri.add_reply_service_context( sc, false );
                    fail( header + "was able to replace context with " +
                          "replace=false." );
                }
                catch( BAD_INV_ORDER e ) {
                    log( header + "was not able to replace context with " +
                         "replace=false." );

                }
            }
            catch( BAD_INV_ORDER e ) {
                fail( header + 
                      "denied access to add_reply_service_context." );
            }
        }
        else {

            // Check FAKEID_RRSC, FAKE_DATA_RRSC
            ServiceContext sc = ri.get_reply_service_context( FAKEID_RRSC );
            if( Arrays.equals( sc.context_data, FAKEDATA_RRSC ) ) {
                log( header + "service context data for rrsc is valid." );
            }
            else {
                fail( header + "service context data for rrsc is invalid." );
            }

            // Check FAKEID_RR, FAKE_DATA_RR
            sc = ri.get_reply_service_context( FAKEID_RR );
            if( Arrays.equals( sc.context_data, FAKEDATA_RR ) ) {
                log( header + "service context data for rr is valid." );
            }
            else {
                fail( header + "service context data for rr is invalid." );
            }

            // Add FAKEID1, FAKE_DATA3
            sc = new ServiceContext( FAKEID1,
                                                    FAKEDATA3 );
            ri.add_reply_service_context( sc, true );
            log( header + "added service context 1." );

            // Replace FAKEID1, FAKE_DATA4, true
            try {
                sc = new ServiceContext( FAKEID1, FAKEDATA4 );
                ri.add_reply_service_context( sc, true );
                log( header + "was able to replace context with " +
                     "replace=true." );
            }
            catch( BAD_INV_ORDER e ) {
                fail( header + "was not able to replace context with " +
                      "replace=true." );
            }

            // Check FAKEID1, FAKE_DATA4
            sc = ri.get_reply_service_context( FAKEID1 );
            if( Arrays.equals( sc.context_data, FAKEDATA4 ) ) {
                log( header + "service context data for 1 is valid." );
            }
            else {
                fail( header + "service context data for 1 is invalid." );
            }

            // Replace FAKEID1, FAKE_DATA3, false
            try {
                sc = new ServiceContext( FAKEID1, FAKEDATA3 );
                ri.add_reply_service_context( sc, false );
                fail( header + "was able to replace context with " +
                      "replace=false." );
            }
            catch( BAD_INV_ORDER e ) {
                log( header + "was not able to replace context with " +
                     "replace=false (ok)." );
            }

            // Check FAKEID1, FAKE_DATA4
            sc = ri.get_reply_service_context( FAKEID1 );
            if( Arrays.equals( sc.context_data, FAKEDATA4 ) ) {
                log( header + "service context data for 1 is valid." );
            }
            else {
                fail( header + "service context data for 1 is invalid." );
            }

        }
    }

}
