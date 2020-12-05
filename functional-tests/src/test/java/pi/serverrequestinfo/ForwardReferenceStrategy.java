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
 *       receive_request_service_contexts
 *       receive_request
 *       send_reply
 *     count = 2
 *       receive_request_service_contexts
 *       receive_request
 *       send_exception
 *     count = 3
 *       receive_request_service_contexts
 *       receive_request
 *       send_other
 *     count = 4, effective_target is now helloRefForward
 *       receive_request_service_contexts
 *       receive_request
 *       send_reply
 */
public class ForwardReferenceStrategy
    extends InterceptorStrategy
{

    private int count = 0;

    public ForwardReferenceStrategy() {
    }

    public void receive_request_service_contexts (
        SampleServerRequestInterceptor interceptor, ServerRequestInfo ri)
        throws ForwardRequest
    {
        super.receive_request_service_contexts( interceptor, ri );

        try {
            count++;
            log( "rrsc(): count is " + count );

            // Try calling forward_reference().  Should fail.
            try {
                ri.forward_reference();
                fail( "rrsc(): forward_reference() did not " +
                      "raise BAD_INV_ORDER" );
            }
            catch( BAD_INV_ORDER e ) {
                log( "rrsc(): forward_reference() " +
                     "raised BAD_INV_ORDER (ok)" );
            }
        }
        catch( Exception ex ) {
            failException( "rrsc", ex );
        }
    }

    public void receive_request (
        SampleServerRequestInterceptor interceptor, ServerRequestInfo ri)
    {
        super.receive_request( interceptor, ri );

        try {
            // Try calling forward_reference().  Should fail.
            try {
                ri.forward_reference();
                fail( "send_reply(): forward_reference() did not " +
                      "raise BAD_INV_ORDER" );
            }                                               
            catch( BAD_INV_ORDER e ) {
                log( "send_reply(): forward_reference() " +
                     "raised BAD_INV_ORDER (ok)" );
            }
        }
        catch( Exception ex ) {
            failException( "send_reply", ex );
        }
    }

    public void send_reply (
        SampleServerRequestInterceptor interceptor, ServerRequestInfo ri)
    {
        super.send_reply( interceptor, ri );

        try {
            // Try calling forward_reference().  Should fail.
            try {
                ri.forward_reference();
                fail( "send_reply(): forward_reference() did not " +
                      "raise BAD_INV_ORDER" );
            }
            catch( BAD_INV_ORDER e ) {
                log( "send_reply(): forward_reference() " +
                     "raised BAD_INV_ORDER (ok)" );
            }
        }
        catch( Exception ex ) {
            failException( "send_reply", ex );
        }
    }


    public void send_exception (
        SampleServerRequestInterceptor interceptor, ServerRequestInfo ri)
        throws ForwardRequest
    {
        super.send_exception( interceptor, ri );

        try {
            // Try calling forward_reference().  Should fail.
            try {
                ri.forward_reference();
                fail( "send_exception(): forward_reference() did not " +
                      "raise BAD_INV_ORDER" );
            }
            catch( BAD_INV_ORDER e ) {
                log( "send_exception(): forward_reference() " +
                     "raised BAD_INV_ORDER (ok)" );
            }
        }
        catch( Exception ex ) {
            failException( "send_exception", ex );
        }
    }

    public void send_other (
        SampleServerRequestInterceptor interceptor, ServerRequestInfo ri)
        throws ForwardRequest
    {
        super.send_other( interceptor, ri );

        try {
            try {
                // Try calling forward_reference().  Should not fail.
                org.omg.CORBA.Object obj = ri.forward_reference();
                if( TestInitializer.helloRefForward._is_equivalent( obj ) ) {
                    log( "send_other(): forward_reference() is valid." );
                }
                else {
                    fail( "send_other(): forward_reference() is " +
                          "invalid." );
                }
            }
            catch( BAD_INV_ORDER e ) {
                fail( "send_other(): forward_reference() raised " +
                      "BAD_INV_ORDER");
            }
        }
        catch( Exception ex ) {
            failException( "send_other", ex );
        }
    }

}
