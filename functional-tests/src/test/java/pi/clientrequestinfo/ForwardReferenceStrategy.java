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
import org.omg.CORBA.*;

/**
 * Strategy to test forward_reference
 * <p>
 * A call will be made, and the interceptor will forward the request to
 * another object to handle it.  forward_reference will be checked when
 * reply status is LOCATION_FORWARD and when it is not LOCATION_FORWARD.
 * <p>
 * Should be called as follows:
 *     count = 1
 *       send_request
 *       receive_reply
 *     count = 2
 *       send_request
 *       receive_exception
 *     count = 3
 *       send_request
 *       receive_other
 *     count = 4, effective_target is now helloRefForward
 *       send_request
 *       receive_exception
 */
public class ForwardReferenceStrategy
    extends InterceptorStrategy
{

    private int count = 0;

    public ForwardReferenceStrategy() {
    }

    public void send_request (
        SampleClientRequestInterceptor interceptor, ClientRequestInfo ri)
        throws ForwardRequest
    {
        super.send_request( interceptor, ri );

        try {
            count++;
            log( "send_request(): count is " + count );

            // Try calling forward_reference().  Should fail.
            try {
                ri.forward_reference();
                fail( "send_request(): forward_reference() did not " +
                      "raise BAD_INV_ORDER" );
            }
            catch( BAD_INV_ORDER e ) {
                log( "send_request(): forward_reference() " +
                     "raised BAD_INV_ORDER (ok)" );
            }

            // Try target()
            org.omg.CORBA.Object obj = ri.target();
            org.omg.CORBA.Object correctObject;
            correctObject = TestInitializer.helloRef;

            if( correctObject._is_equivalent( obj ) ) {
                log( "send_request(): target() is valid." );
            }
            else {
                fail( "send_request(): target() is invalid." );
            }

            // Try effective_target()
            obj = ri.effective_target();
            if( count < 4 ) {
                // This is before we are invoking on helloRefForward.
                correctObject = TestInitializer.helloRef;
            }
            else {
                // This is after we are invoking on helloRefForward.
                correctObject = TestInitializer.helloRefForward;
            }
            if( correctObject._is_equivalent( obj ) ) {
                log( "send_request(): effective_target() is valid." );
            }
            else {
                fail( "send_request(): effective_target() is invalid." );
            }
        }
        catch( Exception ex ) {
            failException( "send_request", ex );
        }
    }

    public void receive_reply(
        SampleClientRequestInterceptor interceptor, ClientRequestInfo ri)
    {
        super.receive_reply( interceptor, ri );

        try {
            // Try calling forward_reference().  Should fail.
            try {
                ri.forward_reference();
                fail( "receive_reply(): forward_reference() did not " +
                      "raise BAD_INV_ORDER" );
            }
            catch( BAD_INV_ORDER e ) {
                log( "receive_reply(): forward_reference() " +
                     "raised BAD_INV_ORDER (ok)" );
            }

            // Try target()
            org.omg.CORBA.Object obj = ri.target();
            if( TestInitializer.helloRef._is_equivalent( obj ) ) {
                log( "send_request(): target() is valid." );
            }
            else {
                fail( "send_request(): target() is invalid." );
            }

            // Try effective_target()
            obj = ri.effective_target();
            if( TestInitializer.helloRef._is_equivalent( obj ) ) {
                log( "send_request(): effective_target() is valid." );
            }
            else {
                fail( "send_request(): effective_target() is invalid." );
            }
        }
        catch( Exception ex ) {
            failException( "send_request", ex );
        }
    }

    public void receive_exception (
        SampleClientRequestInterceptor interceptor, ClientRequestInfo ri)
        throws ForwardRequest
    {
        super.receive_exception( interceptor, ri );

        try {
            // Try calling forward_reference().  Should fail.
            try {
                ri.forward_reference();
                fail( "receive_exception(): forward_reference() did not " +
                      "raise BAD_INV_ORDER" );
            }
            catch( BAD_INV_ORDER e ) {
                log( "receive_exception(): forward_reference() " +
                     "raised BAD_INV_ORDER (ok)" );
            }

            // Try target()
            org.omg.CORBA.Object obj = ri.target();
            org.omg.CORBA.Object correctObject;
            correctObject = TestInitializer.helloRef;

            if( correctObject._is_equivalent( obj ) ) {
                log( "receive_exception(): target() is valid." );
            }
            else {
                fail( "receive_exception(): target() is invalid." );
            }

            // Try effective_target()
            obj = ri.effective_target();
            if( count < 4 ) {
                // This is before we are invoking on helloRefForward.
                correctObject = TestInitializer.helloRef;
            }
            else {
                // This is after we are invoking on helloRefForward.
                correctObject = TestInitializer.helloRefForward;
            }
            if( correctObject._is_equivalent( obj ) ) {
                log( "receive_exception(): effective_target() is valid." );
            }
            else {
                fail( "receive_exception(): effective_target() is invalid." );
            }
        }
        catch( Exception ex ) {
            failException( "receive_exception", ex );
        }
    }

    public void receive_other (
        SampleClientRequestInterceptor interceptor, ClientRequestInfo ri)
        throws ForwardRequest
    {
        super.receive_other( interceptor, ri );

        try {
            try {
                // Try calling forward_reference().  Should not fail.
                org.omg.CORBA.Object obj = ri.forward_reference();
                if( TestInitializer.helloRefForward._is_equivalent( obj ) ) {
                    log( "receive_other(): forward_reference() is valid." );
                }
                else {
                    fail( "receive_other(): forward_reference() is " +
                          "invalid." );
                }
            }
            catch( BAD_INV_ORDER e ) {
                fail( "receive_other(): forward_reference() raised " +
                      "BAD_INV_ORDER");
            }

            // Try target()
            org.omg.CORBA.Object obj = ri.target();
            if( TestInitializer.helloRef._is_equivalent( obj ) ) {
                log( "receive_other(): target() is valid." );
            }
            else {
                fail( "receive_other(): target() is invalid." );
            }

            // Try effective_target()
            obj = ri.effective_target();
            if( TestInitializer.helloRef._is_equivalent( obj ) ) {
                log( "receive_other(): effective_target() is valid." );
            }
            else {
                fail( "receive_other(): effective_target() is invalid." );
            }
        }
        catch( Exception ex ) {
            failException( "receive_other", ex );
        }
    }

}
