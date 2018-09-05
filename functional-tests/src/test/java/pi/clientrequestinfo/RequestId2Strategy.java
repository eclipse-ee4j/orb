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
 * Strategy to test request_id.2
 * <p>
 * A recursive call will be made.  We will ensure the inner-most call has a 
 * different requestId than the outer-most call.
 */
public class RequestId2Strategy
    extends InterceptorStrategy
{

    // The request id for the outer-most call:
    private int outerId = -1;

    // The request id for the inner-most call:
    private int innerId = -1;

    // The request id count:
    private int count = 0;

    public void send_request (
        SampleClientRequestInterceptor interceptor, ClientRequestInfo ri)
        throws ForwardRequest
    {
        super.send_request( interceptor, ri );

        try {
            if( count == 0 ) {
                outerId = ri.request_id();
                log( "send_request(): outer-most id is " + outerId );
                count++;
            }
            else if( count == 1 ) {
                innerId = ri.request_id();
                log( "send_request(): inner-most id is " + innerId );
                count++;

                if( innerId == outerId ) {
                    fail( "outer and inner requests ids are the same." );
                }
            }
        }
        catch( Exception e ) {
            failException( "send_request", e );
        }
    }

    public void receive_reply(
        SampleClientRequestInterceptor interceptor, ClientRequestInfo ri)
    {
        super.receive_reply( interceptor, ri );

        try {
            // check to make sure inner ids match.
            count--;

            if( count == 1 ) {
                int id = ri.request_id();
                log( "receive_reply(): inner-most id is " + id );
                if( id != innerId ) {
                    fail( "inner id is not the same in receive_reply() as " +
                          "it was in send_request()" );
                }
            }
            else if( count == 0 ) {
                int id = ri.request_id();
                log( "receive_reply(): outer-most id is " + id );
                if( id != outerId ) {
                    fail( "outer id is not the same in receive_reply() as " +
                          "it was in send_request()" );
                }
            }
        }
        catch( Exception e ) {
            failException( "receive_reply", e );
        }
    }

}
