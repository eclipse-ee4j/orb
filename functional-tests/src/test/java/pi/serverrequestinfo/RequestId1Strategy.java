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

/**
 * Strategy to test request_id.1
 */
public class RequestId1Strategy
    extends InterceptorStrategy
{

    // The id received in receive_request_service_contexts:
    private int requestId;

    public void receive_request_service_contexts (
        SampleServerRequestInterceptor interceptor, ServerRequestInfo ri)
        throws ForwardRequest
    {
        super.receive_request_service_contexts( interceptor, ri );
        
        try {
            this.requestId = ri.request_id();
            log( "receive_request_service_contexts(): request_id = " + 
                requestId );
        }
        catch( Exception ex ) {
            failException( "receive_request_service_contexts", ex );
        }
    }

    public void receive_request (
        SampleServerRequestInterceptor interceptor, ServerRequestInfo ri)
    {
        super.receive_request( interceptor, ri );
        try {
            testId( "receive_request", ri.request_id() );
        }
        catch( Exception e ) {
            failException( "send_reply", e );
        }
    }

    public void send_reply (
        SampleServerRequestInterceptor interceptor, ServerRequestInfo ri)
    {
        super.send_reply( interceptor, ri );
        try {
            testId( "send_reply", ri.request_id() );
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
            testId( "send_exception", ri.request_id() );
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
            testId( "send_other", ri.request_id() );
        }
        catch( Exception e ) {
            failException( "send_other", e );
        }
    }

    /**
     * Tests the given id after send_request
     */
    private void testId( String method, int id ) {
        log( method + "(): request_id = " + id );
        if( id != this.requestId ) {
            fail( "Request ID in " + method + " did not match request " +
                  "id in " + method );
        }
    }

}
