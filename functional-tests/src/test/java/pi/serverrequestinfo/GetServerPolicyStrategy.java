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
import org.omg.PortableServer.*;
import com.sun.corba.ee.impl.interceptors.*;
import org.omg.PortableInterceptor.*;
import org.omg.Messaging.*;

import java.util.*;

/**
 * Strategy to test get_server_policy.
 */
public class GetServerPolicyStrategy
    extends InterceptorStrategy
{

    // The request count. We should be calling:
    //   0 - sayHello
    //   1 - saySystemException
    //   2 - saySystemException.
    private int count = 0;

    private static final int INVALID_POLICY_TYPE = 101;

    public void receive_request_service_contexts (
        SampleServerRequestInterceptor interceptor, ServerRequestInfo ri)
        throws org.omg.PortableInterceptor.ForwardRequest
    {
        try {
            super.receive_request_service_contexts( interceptor, ri );
            count++;
            checkGetServerPolicy( "rrsc", ri );
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
            checkGetServerPolicy( "rr", ri );
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
            checkGetServerPolicy( "sr", ri );
        }
        catch( Exception ex ) {
            failException( "send_reply", ex );
        }
    }

    public void send_exception (
        SampleServerRequestInterceptor interceptor, ServerRequestInfo ri)
        throws org.omg.PortableInterceptor.ForwardRequest
    {
        try {
            super.send_exception( interceptor, ri );
            checkGetServerPolicy( "se", ri );
        }
        catch( Exception ex ) {
            failException( "send_exception", ex );
        }
    }

    public void send_other (
        SampleServerRequestInterceptor interceptor, ServerRequestInfo ri)
        throws org.omg.PortableInterceptor.ForwardRequest
    {
        try {
            super.send_other( interceptor, ri );
            checkGetServerPolicy( "so", ri );
        }
        catch( Exception ex ) {
            failException( "send_other", ex );
        }
    }

    private void checkGetServerPolicy( String method, ServerRequestInfo ri ) 
        throws Exception
    {
        // Try an invalid policy:
        try {
            Policy policy = ri.get_server_policy( INVALID_POLICY_TYPE );
            if( policy != null ) {
                fail( method + "(): get_server_policy( INVALID ) " +
                    "is not null!" );
            }
            else {
                log( method + "(): get_server_policy( INVALID ) " +
                    "is null (ok)" );
            }
        }
        catch( INV_POLICY e ) {
            fail( method + "(): get_server_policy( INVALID ) " +
                "throws INV_POLICY (error - should return null)" );
        }
        catch( Exception e ) {
            e.printStackTrace();
            fail( method + "(): get_server_policy( INVALID ) " +
                "throws incorrect exception: " + e );
        }

        // Try a standard policy:
        try {
            Policy policy = ri.get_server_policy( 
                ID_UNIQUENESS_POLICY_ID.value );
            if( policy instanceof IdUniquenessPolicy ) {
                log( method + "(): get_server_policy( STANDARD ) " +
                    "returns correct policy." );
            }
            else {
                fail( method + "(): get_server_policy( STANDARD ) " +
                    "returns incorrect policy: " + 
                    policy.getClass().getName() );
            }
        }
        catch( INV_POLICY e ) {
            fail( method + "(): get_server_policy( STANDARD ) " +
                "throws INV_POLICY" );
        }
        catch( Exception e ) {
            e.printStackTrace();
            fail( method + "(): get_server_policy( STANDARD ) " +
                "throws incorrect exception: " + e );
        }

        // Try a custom policy:
        try {
            Policy policy = ri.get_server_policy( 100 );
            if( policy instanceof PolicyHundred ) {
                log( method + "(): get_server_policy( CUSTOM ) " +
                    "returns correct policy." );
            }
            else {
                fail( method + "(): get_server_policy( CUSTOM ) " +
                    "returns incorrect policy: " + 
                    policy.getClass().getName() );
            }
        }
        catch( INV_POLICY e ) {
            fail( method + "(): get_server_policy( CUSTOM ) " +
                "throws INV_POLICY" );
        }
        catch( Exception e ) {
            e.printStackTrace();
            fail( method + "(): get_server_policy( CUSTOM ) " +
                "throws incorrect exception: " + e );
        }
    }

}
