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

package pi.serverrequestinfo;

import org.omg.CORBA.*;
import com.sun.corba.ee.impl.interceptors.*;
import org.omg.PortableInterceptor.*;
import org.omg.Messaging.*;

import java.util.*;

/**
 * Strategy to test that the server-sides request info stack is functioning.
 */
public class RequestInfoStackStrategy
    extends InterceptorStrategy
{

    // The request count. We should be calling:
    //   0 - idle
    //   1 - sayInvokeAgain
    //   2 - sayHello
    private int count = 0;

    // The test number.  We should be recursively calling:
    //   1 - sayHello
    //   2 - saySystemException
    //   3 - saySystemException
    private int testNum = 0;

    public void receive_request_service_contexts (
        SampleServerRequestInterceptor interceptor, ServerRequestInfo ri)
        throws ForwardRequest
    {
        try {
            super.receive_request_service_contexts( interceptor, ri );
            log( "rrsc(): count = " + count );
            log( "rrsc(): count = " + count + " to " + (count+1) );
            count++;
            if( count == 1 ) testNum++;
            checkOperationName( "rrsc", ri.operation() );
        }
        catch( Exception ex ) {
            failException( "rrsc", ex );
        }
    }

    public void receive_request (
        SampleServerRequestInterceptor interceptor, ServerRequestInfo ri)
    {
        try {
            super.receive_request( interceptor, ri );
            log( "rr(): count = " + count );
            checkOperationName( "rr", ri.operation() );
        }
        catch( Exception ex ) {
            failException( "receive_request", ex );
        }
    }

    public void send_reply (
        SampleServerRequestInterceptor interceptor, ServerRequestInfo ri)
    {
        try {
            super.send_reply( interceptor, ri );
            log( "sr(): count = " + count );
            checkOperationName( "sr", ri.operation() );
            log( "sr(): count = " + count + " to " + (count-1) );
            count--;
        }
        catch( Exception ex ) {
            failException( "send_reply", ex );
        }
    }

    public void send_exception (
        SampleServerRequestInterceptor interceptor, ServerRequestInfo ri)
        throws ForwardRequest
    {
        try {
            super.send_exception( interceptor, ri );
            log( "se(): count = " + count );
            checkOperationName( "se", ri.operation() );
            log( "se(): count = " + count + " to " + (count-1) );
            count--;
        }
        catch( Exception ex ) {
            failException( "send_exception", ex );
        }
    }

    public void send_other (
        SampleServerRequestInterceptor interceptor, ServerRequestInfo ri)
        throws ForwardRequest
    {
        try {
            super.send_other( interceptor, ri );
            log( "so(): count = " + count );
            checkOperationName( "so", ri.operation() );
            log( "so(): count = " + count + " to " + (count-1) );
            count--;
        }
        catch( Exception ex ) {
            failException( "send_other", ex );
        }
    }

    private void checkOperationName( String method, String operationName )
        throws Exception
    {
        switch( count ) {
        case 1:
            if( !operationName.equals( "sayInvokeAgain" ) ) {
                fail( method + "(): Incorrect operation name: " + 
                    operationName + " Count = 1" );
            }
            log( method + "(): sayInvokeAgain() invoked" );
            break;
        case 2:
            String expected = (testNum == 1) ? 
                "sayHello" : "saySystemException";
            if( !operationName.equals( expected ) ) {
                fail( method + "(): Incorrect operation name: " + 
                    operationName + " Count = 2" );
            }
            log( method + "(): " + expected + "() invoked" );
            break;
        default:
            // This will happen if our recursive depth is too high
            // from PI's standpoint.
            log( method + "(): count too high! count = " + count );
            break;
        }
    }
}
