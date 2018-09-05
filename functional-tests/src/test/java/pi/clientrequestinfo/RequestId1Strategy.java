/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package pi.clientrequestinfo;

import com.sun.corba.ee.impl.interceptors.*;
import org.omg.PortableInterceptor.*;

/**
 * Strategy to test request_id.1
 */
public class RequestId1Strategy
    extends InterceptorStrategy
{

    // The id received in send_request:
    private int requestId;

    public void send_request (
        SampleClientRequestInterceptor interceptor, ClientRequestInfo ri)
        throws ForwardRequest
    {
        super.send_request( interceptor, ri );
        
        try {
            this.requestId = ri.request_id();
            log( "send_request(): request_id = " + requestId );
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
            testId( "receive_reply", ri.request_id() );
        }
        catch( Exception e ) {
            failException( "receive_reply", e );
        }
    }


    public void receive_exception (
        SampleClientRequestInterceptor interceptor, ClientRequestInfo ri) 
        throws ForwardRequest
    {
        super.receive_exception( interceptor, ri );
        try {
            testId( "receive_exception", ri.request_id() );
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
            testId( "receive_other", ri.request_id() );
        }
        catch( Exception e ) {
            failException( "receive_other", e );
        }
    }

    /**
     * Tests the given id after send_request
     */
    private void testId( String method, int id ) {
        log( method + "(): request_id = " + id );
        if( id != this.requestId ) {
            fail( "Request ID in " + method + " did not match request " +
                  "id in send_request." );
        }
    }

}
