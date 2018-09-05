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

import org.omg.PortableInterceptor.ForwardRequest;
import org.omg.PortableInterceptor.ClientRequestInfo;
import org.omg.PortableInterceptor.ClientRequestInterceptor;

/**
 * Sample ClientRequestInterceptor for use in testing.  This interceptor
 * is dynamically configurable via an InterceptorStrategy.  It assumes
 * three interceptors have been registered, and most operations will be
 * performed on interceptor number 2 and only if the target() is not
 * helloRefForward.
 */
public class SampleClientRequestInterceptor 
    extends org.omg.CORBA.LocalObject 
    implements ClientRequestInterceptor
{
    // The dyanmic strategy that will be used for this round.
    public static InterceptorStrategy strategy = null;
    
    // The name of this interceptor
    private String name;

    // True if enabled, false if all interception points must 
    // return immediately.
    public static boolean enabled = false;

    // Selective enabling and disabling of interception points.  These
    // are only applicable if enabled is true:
    public static boolean sendRequestEnabled = true;
    public static boolean sendPollEnabled = true;
    public static boolean receiveReplyEnabled = true;
    public static boolean receiveExceptionEnabled = true;
    public static boolean receiveOtherEnabled = true;

    // Special flags to override the strategy behavior of this interceptor.

    // Throw ForwardRequest during receive_exception so that receive_other
    // is called.
    public static boolean exceptionRedirectToOther = false;

    // Cause send_request to recursively invoke another method.
    public static boolean recursiveInvoke = false;

    // Allow interceptors to be invoked for forwarded object as well.
    public static boolean invokeOnForwardedObject = false;

    private static int invokeCount = 0;
    
    public SampleClientRequestInterceptor( String name ) {
        this.name = name;
    }

    public String name() {
        return name;
    }

    public void destroy() {
    }

    public void send_request (ClientRequestInfo ri) 
        throws ForwardRequest 
    {
        // Only execute if the interceptor is enabled, this interception
        // point is enabled, we are the second interceptor, and we are 
        // executing on hello, not helloForward.
        if( !enabled ) return;
        if( !sendRequestEnabled ) return;
        if( !name.equals( "2" ) ) return;
        if( !invokeOnForwardedObject && 
            TestInitializer.helloRefForward._is_equivalent(
            ri.effective_target() ) ) return;

        strategy.send_request( this, ri );

        if( recursiveInvoke ) {
            if( invokeCount == 0 ) {
                invokeCount++;
                try {
                    ClientCommon.client.invokeMethod( "sayHello" );
                }
                catch( Exception e ) {
                    // If this throws an exception, convert it into a 
                    // SystemException.
                    throw new BAD_OPERATION( e.getMessage() );
                }
                invokeCount--;
            }
        }
    }

    public void send_poll (ClientRequestInfo ri) {
        // Only execute if the interceptor is enabled, this interception
        // point is enabled, we are the second interceptor, and we are 
        // executing on hello, not helloForward.
        if( !enabled ) return;
        if( !sendPollEnabled ) return;
        if( !name.equals( "2" ) ) return;
        if( !invokeOnForwardedObject &&
            TestInitializer.helloRefForward._is_equivalent(
            ri.effective_target() ) ) return;

        strategy.send_poll( this, ri );
    }

    public void receive_reply (ClientRequestInfo ri) {
        // Only execute if the interceptor is enabled, this interception
        // point is enabled, we are the second interceptor, and we are 
        // executing on hello, not helloForward.
        if( !enabled ) return;
        if( !receiveReplyEnabled ) return;
        if( !name.equals( "2" ) ) return;
        if( !invokeOnForwardedObject &&
            TestInitializer.helloRefForward._is_equivalent(
            ri.effective_target() ) ) return;

        strategy.receive_reply( this, ri );
    }

    public void receive_exception (ClientRequestInfo ri) 
        throws ForwardRequest
    {
        // Only execute if the interceptor is enabled, this interception
        // point is enabled, we are the second interceptor, and we are 
        // executing on hello, not helloForward.
        if( !enabled ) return;
        if( !receiveExceptionEnabled ) return;
        if( !name.equals( "2" ) ) return;
        if( !invokeOnForwardedObject &&
            TestInitializer.helloRefForward._is_equivalent(
            ri.effective_target() ) ) return;

        if( exceptionRedirectToOther &&
            !TestInitializer.helloRefForward._is_equivalent(
            ri.effective_target() ) ) 
        {
            // Override strategy, and cause this exception to redirect to
            // a receive_other on interceptor number 1.
            throw new ForwardRequest( TestInitializer.helloRefForward );
        }
        else {
            strategy.receive_exception( this, ri );
        }
    }

    public void receive_other (ClientRequestInfo ri) 
        throws ForwardRequest 
    {
        // Only execute if the interceptor is enabled, this interception
        // point is enabled, we are the second interceptor, and we are 
        // executing on hello, not helloForward.
        if( !enabled ) return;
        if( !receiveOtherEnabled ) return;
        if( !name.equals( "1" ) ) return;
        if( !invokeOnForwardedObject &&
            TestInitializer.helloRefForward._is_equivalent(
            ri.effective_target() ) ) return;

        strategy.receive_other( this, ri );
    }

}
