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

import org.omg.CORBA.*;
import com.sun.corba.ee.impl.interceptors.*;
import org.omg.PortableInterceptor.*;
import org.omg.Messaging.*;

/**
 * Strategy to test operations()
 */
public class AttributesValidStrategy
    extends InterceptorStrategy
{

    // The request count. We should be calling:
    //   0 - sayHello
    //   1 - saySystemException
    //   2 - saySystemException.
    private int count = 0;

    // The most recent operation name received.
    private String operationName;

    public void send_request (
        SampleClientRequestInterceptor interceptor, ClientRequestInfo ri)
        throws ForwardRequest
    {
        try {
            super.send_request( interceptor, ri );

            String validName = (count == 0) ?  
                "sayHello" : "saySystemException";
            this.operationName = ri.operation();
            log( "send_request(): Expected operation name = " + validName );
            log( "send_request(): Actual operation name = " + 
                this.operationName );

            if( !this.operationName.equals( validName ) ) {
                fail( "Operation name not equal to expected name." );
            }

            checkSyncScope( "send_request", ri );

            // Check that within send_request, reply_status 
            // throws BAD_INV_ORDER:
            try {
                short replyStatus = ri.reply_status();
                fail( "send_request(): Should not be able to execute " +
                      "reply_status() here" );
            }
            catch( BAD_INV_ORDER e ) {
                log( "send_request(): Tried reply_status() and received " +
                     "BAD_INV_ORDER (ok)" );
            }

            count++;
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
        try {
            super.receive_reply( interceptor, ri );
            checkOperation( "receive_reply", ri.operation() );
            checkSyncScope( "receive_reply", ri );

            // Check that within receive_reply, reply_status is SUCCESSFUL.
            boolean[] validValues = { true, false, false, false, false };
            checkReplyStatus( "receive_reply", ri, validValues );
        }
        catch( Exception ex ) {
            failException( "receive_reply", ex );
        }
    }


    public void receive_exception (
        SampleClientRequestInterceptor interceptor, ClientRequestInfo ri) 
        throws ForwardRequest
    {
        try {
            super.receive_exception( interceptor, ri );
            checkOperation( "receive_exception", ri.operation() );
            checkSyncScope( "receive_exception", ri );

            // Check that within receive_exception, reply_status is 
            // SYSTEM_EXCEPTION or USER_EXCEPTION:
            boolean[] validValues = { false, true, true, false, false };
            checkReplyStatus( "receive_exception", ri, validValues );
        }
        catch( Exception ex ) {
            failException( "receive_exception", ex );
        }
    }

    public void receive_other (
        SampleClientRequestInterceptor interceptor, ClientRequestInfo ri) 
        throws ForwardRequest
    {
        try {
            super.receive_other( interceptor, ri );
            checkOperation( "receive_other", ri.operation() );
            checkSyncScope( "receive_other", ri );

            // Check that within receive_other, reply_status is 
            // SUCCESSFUL, LOCATION_FORWARD, or TRANSPORT_RETRY.
            boolean[] validValues = { true, false, false, true, true };
            checkReplyStatus( "receive_other", ri, validValues );
        }
        catch( Exception ex ) {
            failException( "receive_other", ex );
        }
    }

    private void checkOperation( String method, String opName ) {
        log( method + "(): Actual operation name = " + opName );
        if( !opName.equals( this.operationName ) ) {
            fail( "Operation name in " + method + " not equal to " + 
                  "operation name in send_request()" );
        }
    }

    private void checkSyncScope( String method, ClientRequestInfo ri ) {
        short syncScope = ri.sync_scope();
        log( method + "(): sync_scope() returns " + syncScope );
        if( syncScope != SYNC_WITH_TRANSPORT.value ) {
            fail( "sync_scope() is not SYNC_WITH_TRANSPORT" );
        }
    }

    private void checkReplyStatus( String method, ClientRequestInfo ri,
        boolean[] validValues )
    {
        int i;

        // Describe to user which values are valid:
        String validDesc = "{ ";
        for( i = 0; i < validValues.length; i++ ) {
            validDesc += "" + validValues[i] + " ";
        }
        validDesc += "}";
        log( method + "(): Valid values: " + validDesc );

        short replyStatus = ri.reply_status();
        log( method + "(): Actual value: " + replyStatus );

        if( !validValues[replyStatus] ) {
            fail( method + "(): Not a valid reply status." );
        }
    }

}
