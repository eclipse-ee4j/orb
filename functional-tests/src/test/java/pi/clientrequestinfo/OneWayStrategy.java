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
