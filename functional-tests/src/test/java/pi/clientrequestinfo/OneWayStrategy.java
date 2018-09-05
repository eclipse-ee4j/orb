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
 * Strategy to test response_expected()
 */
public class OneWayStrategy
    extends InterceptorStrategy
{

    public void send_request (
        SampleClientRequestInterceptor interceptor, ClientRequestInfo ri)
        throws ForwardRequest
    {
        super.send_request( interceptor, ri );
        
        try {
            checkResponseExpected( "send_request", ri );
        }
        catch( Exception e ) {
            failException( "send_request", e );
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
            checkResponseExpected( "receive_reply", ri );
        }
        catch( Exception e ) {
            failException( "receive_reply", e );
        }
    }


    private void checkResponseExpected( String method, ClientRequestInfo ri ) {
        String operationName = ri.operation();
        boolean responseExpected = ri.response_expected();
        boolean validExpected = !operationName.equals( "sayOneway" );

        log( method + "(): Operation " + operationName + 
             ", response expected = " + responseExpected );

        if( responseExpected != validExpected ) {
            fail( "response_expected() invalid for this operation." );
        }
    }

}
