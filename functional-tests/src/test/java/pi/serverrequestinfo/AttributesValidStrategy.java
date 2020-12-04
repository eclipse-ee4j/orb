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

    // The most receive object id received
    private byte[] objectId;

    // The most receive adapter id received
    private byte[] adapterId;

    // The Hello interface repository id to check against.
    private String validRepId;

    // Fake Hello interface repository id to check against.
    private String invalidRepId;

    public AttributesValidStrategy( String validRepId, String invalidRepId ) {
        this.validRepId = validRepId;
        this.invalidRepId = invalidRepId;
    }

    public void receive_request_service_contexts (
        SampleServerRequestInterceptor interceptor, ServerRequestInfo ri)
        throws ForwardRequest
    {
        try {
            super.receive_request_service_contexts( interceptor, ri );

            String validName = (count == 0) ?  
                "sayHello" : "saySystemException";
            this.operationName = ri.operation();
            log( "rrsc(): Expected operation name = " + validName );
            log( "rrsc(): Actual operation name = " + 
                this.operationName );

            if( !this.operationName.equals( validName ) ) {
                fail( "Operation name not equal to expected name." );
            }

            checkSyncScope( "rrsc", ri );

            // Check that within rrsc, reply_status 
            // throws BAD_INV_ORDER:
            try {
                short replyStatus = ri.reply_status();
                fail( "rrsc(): Should not be able to execute " +
                      "reply_status() here" );
            }
            catch( BAD_INV_ORDER e ) {
                log( "rrsc(): Tried reply_status() and received " +
                     "BAD_INV_ORDER (ok)" );
            }

            // Check than within rrsc, object_id throws BAD_INV_ORDER:
            try {
                byte[] objectId = ri.object_id();
                fail( "rrsc(): Should not be able to execute " +
                      "object_id() here" );
            }
            catch( BAD_INV_ORDER e ) {
                log( "rrsc(): Tried object_id() and received " +
                     "BAD_INV_ORDER (ok)" );
            }

            // Check than within rrsc, adapter_id throws BAD_INV_ORDER:
            try {
                byte[] adapterId = ri.adapter_id();
                fail( "rrsc(): Should not be able to execute " +
                      "adapter_id() here" );
            }
            catch( BAD_INV_ORDER e ) {
                log( "rrsc(): Tried adapter_id() and received " +
                     "BAD_INV_ORDER (ok)" );
            }

            checkTMDI( "rrsc", ri );
            checkTargetIsA( "rrsc", ri );

            count++;
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
            checkOperation( "receive_request", ri.operation() );
            checkSyncScope( "receive_request", ri );

            // Check that within receive_request, reply_status 
            // throws BAD_INV_ORDER:
            try {
                short replyStatus = ri.reply_status();
                fail( "rr(): Should not be able to execute " +
                      "reply_status() here" );
            }
            catch( BAD_INV_ORDER e ) {
                log( "receive_request(): Tried reply_status() and received " +
                     "BAD_INV_ORDER (ok)" );
            }

            // Check that, object_id is valid in receive_request:
            this.objectId = ri.object_id();
            if( this.objectId == null ) {
                fail( "receive_request(): Object id is null" );
            }
            else if( Arrays.equals( this.objectId, "".getBytes() ) ) {
                fail( "receive_request(): Object id is empty string" );
            }
            else {
                log( "receive_request(): Object id is " + this.objectId );
            }

            // Check that, adapter_id is valid in receive_request:
            this.adapterId = ri.adapter_id();
            if( this.adapterId == null ) {
                fail( "receive_request(): Adapter id is null" );
            }
            else if( Arrays.equals( this.adapterId, "".getBytes() ) ) {
                fail( "receive_request(): Adapter id is empty string" );
            }
            else {
                log( "receive_request(): Adapter id is " + this.adapterId );
            }

            checkTMDI( "receive_request", ri );
            checkTargetIsA( "receive_request", ri );
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
            checkOperation( "send_reply", ri.operation() );
            checkSyncScope( "send_reply", ri );

            // Check that within send_reply, reply_status is SUCCESSFUL.
            boolean[] validValues = { true, false, false, false, false };
            checkReplyStatus( "send_reply", ri, validValues );

            checkObjectId( "send_reply", ri );
            checkAdapterId( "send_reply", ri );
            checkTMDI( "send_reply", ri );
            checkTargetIsA( "send_reply", ri );
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
            checkOperation( "send_exception", ri.operation() );
            checkSyncScope( "send_exception", ri );

            // Check that within send_exception, reply_status is 
            // SYSTEM_EXCEPTION or USER_EXCEPTION:
            boolean[] validValues = { false, true, true, false, false };
            checkReplyStatus( "send_exception", ri, validValues );

            checkObjectId( "send_exception", ri );
            checkAdapterId( "send_exception", ri );
            checkTMDI( "send_exception", ri );
            checkTargetIsA( "send_exception", ri );
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
            checkOperation( "send_other", ri.operation() );
            checkSyncScope( "send_other", ri );

            // Check that within send_other, reply_status is 
            // SUCCESSFUL, LOCATION_FORWARD, or TRANSPORT_RETRY.
            boolean[] validValues = { true, false, false, true, true };
            checkReplyStatus( "send_other", ri, validValues );

            checkObjectId( "send_other", ri );
            checkAdapterId( "send_other", ri );
            checkTMDI( "send_other", ri );
            checkTargetIsA( "send_other", ri );
        }
        catch( Exception ex ) {
            failException( "send_other", ex );
        }
    }

    private void checkOperation( String method, String opName ) {
        log( method + "(): Actual operation name = " + opName );
        if( !opName.equals( this.operationName ) ) {
            fail( "Operation name in " + method + " not equal to " + 
                  "operation name in send_request()" );
        }
    }

    private void checkSyncScope( String method, ServerRequestInfo ri ) {
        short syncScope = ri.sync_scope();
        log( method + "(): sync_scope() returns " + syncScope );
        if( syncScope != SYNC_WITH_TRANSPORT.value ) {
            fail( "sync_scope() is not SYNC_WITH_TRANSPORT" );
        }
    }

    private void checkReplyStatus( String method, ServerRequestInfo ri,
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

    private void checkObjectId( String method, ServerRequestInfo ri ) {
        // Check that object_id is valid 
        byte[] objectId = ri.object_id();
        if( Arrays.equals( this.objectId, objectId ) ) {
            log( method + "(): Object id is valid" );
        }
        else {
            fail( method + "(): Object id is invalid (" + 
                objectId + ")" );
        }
    }

    private void checkAdapterId( String method, ServerRequestInfo ri ) {
        // Check that adapter_id is valid
        byte[] adatperId = ri.adapter_id();
        if( Arrays.equals( this.adapterId, adapterId ) ) {
            log( method + "(): Adapter id is valid" );
        }
        else {
            fail( method + "(): Adapter id is invalid (" + 
                adapterId + ")" );
        }
    }

    // TMDI = Target Most Dervied Interface
    private void checkTMDI( String method, ServerRequestInfo ri ) {
        if( !method.equals( "receive_request" ) ) {
            // everything but receive_request should disallow access to 
            // target_most_derived_interface
            try {
                ri.target_most_derived_interface();
                fail( method + "(): Should not be able to execute " +
                      "target_most_derived_interface() here" );
            }
            catch( BAD_INV_ORDER e ) {
                log( method + "(): Tried target_most_derived_interface() and "+
                     "received BAD_INV_ORDER (ok)" );
            }

        }
        else {
            // Check that target_most_dervied_interface is valid
            String targetMostDerivedInterface = 
                ri.target_most_derived_interface();
            if( targetMostDerivedInterface == null ) {
                fail( method + "(): Target most derived interface " + 
                    "is null" );
            }
            else if( targetMostDerivedInterface.equals( "" ) ) {
                fail( method + "(): Target most derived interface " +
                    "is empty string" );
            }
            else if( targetMostDerivedInterface.equals( validRepId ) ) {
                log( method + "(): Target most derived interface " + 
                    "is " + targetMostDerivedInterface );
            }
            else {
                fail( method + "(): Target most derived interface " + 
                    "is " + targetMostDerivedInterface + ".  Expecting " +
                    validRepId + " instead." );
            }
        }
    }

    private void checkTargetIsA( String method, ServerRequestInfo ri ) {
        if( !method.equals( "receive_request" ) ) {
            // everything but receive_request should disallow access to 
            // target_is_a.
            try {
                ri.target_is_a( validRepId );
                fail( method + "(): Should not be able to execute " +
                      "target_is_a() here" );
            }
            catch( BAD_INV_ORDER e ) {
                log( method + "(): Tried target_is_a() and " +
                     "received BAD_INV_ORDER (ok)" );
            }

        }
        else {
            // Check that target_is_a is valid
            if( ri.target_is_a( validRepId ) ) {
                log( method + "(): target_is_a(" + 
                    validRepId + ") is true (ok)" );
            }
            else {
                fail( method + "(): target_is_a(" + validRepId + 
                    ") is false." );
            }

            if( ri.target_is_a( invalidRepId ) ) {
                fail( method + "(): target_is_a(" + 
                    invalidRepId + ") is true" );
            }
            else {
                log( method + "(): target_is_a(" + 
                    invalidRepId + ") is false (ok)." );
            }
        }
    }

}
