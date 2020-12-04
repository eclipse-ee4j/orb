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

import org.omg.PortableInterceptor.ForwardRequest;
import org.omg.PortableInterceptor.ServerRequestInfo;
import org.omg.PortableInterceptor.ServerRequestInterceptor;

/**
 * Sample ServerRequestInterceptor for use in testing.  This interceptor
 * is dynamically configurable via an InterceptorStrategy.  It assumes
 * three interceptors have been registered, and most operations will be
 * performed on interceptor number 2 and only if the target() is not
 * helloRefForward.
 */
public class SampleServerRequestInterceptor 
    extends org.omg.CORBA.LocalObject 
    implements ServerRequestInterceptor
{
    // Valid message for exceptions
    public static final String VALID_MESSAGE = "Valid test result.";

    // The dyanmic strategy that will be used for this round.
    public static InterceptorStrategy strategy = null;
    
    // The name of this interceptor
    private String name;

    // Ensure this call was intercepted.  This is set to true when at
    // least one interceptor was called, and is reset before the server
    // requests an invocation from the client.  This helps detect a faulty
    // test run.
    public static boolean intercepted = false;

    // True if enabled, false if all interception points must 
    // return immediately.
    public static boolean enabled = false;

    // Selective enabling and disabling of interception points.  These
    // are only applicable if enabled is true:
    public static boolean receiveRequestServiceContextsEnabled = true;
    public static boolean receiveRequestEnabled = true;
    public static boolean sendReplyEnabled = true;
    public static boolean sendExceptionEnabled = true;
    public static boolean sendOtherEnabled = true;

    // Special flags to override the strategy behavior of this interceptor.

    // Throw ForwardRequest during receive_exception so that receive_other
    // is called.
    public static boolean exceptionRedirectToOther = false;

    // Allow interceptors to be invoked for forwarded object as well.
    public static boolean invokeOnForwardedObject = false;

    // Normally, _is_a invocations are not recorded.  This is because _is_a
    // is invoked often by RMI lookup code.  If this flag is set,
    // _is_a invocations are recorded at most once.  This is useful for
    // when we actually want to record interception on the _is_a "special op".
    static boolean dontIgnoreIsA = false;
    

    // Records the method invocation order
    public static String methodOrder = "";

    private static int invokeCount = 0;

    public SampleServerRequestInterceptor( String name ) {
        this.name = name;
    }

    public String name() {
        return name;
    }

    public void destroy() {
    }

    public void receive_request_service_contexts( ServerRequestInfo ri )
        throws ForwardRequest 
    {
        // Only execute if the interceptor is enabled, this interception
        // point is enabled, we are the second interceptor, and we are 
        // executing on hello, not helloForward.
        if( ri.operation().equals( "syncWithServer" ) ) return;
        if( !dontIgnoreIsA && ri.operation().equals( "_is_a" ) ) {
            if( name.equals( "1" ) ) {
                System.out.println( 
                    "    - Interceptor: Ingoring _is_a call..." );
            }
            return;
        }
        if( !enabled ) return;
        if( !receiveRequestServiceContextsEnabled ) return;
        if( !name.equals( "2" ) ) return;

        intercepted = true;
        strategy.receive_request_service_contexts( this, ri );
    }

    public void receive_request (ServerRequestInfo ri) 
        throws ForwardRequest
    {
        // Only execute if the interceptor is enabled, this interception
        // point is enabled, we are the second interceptor, and we are 
        // executing on hello, not helloForward.
        if( ri.operation().equals( "syncWithServer" ) ) return;
        if( !dontIgnoreIsA && ri.operation().equals( "_is_a" ) ) return;
        if( !enabled ) return;
        if( !receiveRequestEnabled ) return;
        if( !name.equals( "2" ) ) return;

        strategy.receive_request( this, ri );
    }

    public void send_reply (ServerRequestInfo ri) {
        // Only execute if the interceptor is enabled, this interception
        // point is enabled, we are the second interceptor, and we are 
        // executing on hello, not helloForward.
        if( ri.operation().equals( "syncWithServer" ) ) return;
        if( !dontIgnoreIsA && ri.operation().equals( "_is_a" ) ) return;
        if( !enabled ) return;
        if( !sendReplyEnabled ) return;
        if( !name.equals( "2" ) ) return;

        strategy.send_reply( this, ri );
    }

    public void send_exception (ServerRequestInfo ri) 
        throws ForwardRequest
    {
        // Only execute if the interceptor is enabled, this interception
        // point is enabled, we are the second interceptor, and we are 
        // executing on hello, not helloForward.
        if( ri.operation().equals( "syncWithServer" ) ) return;
        if( !dontIgnoreIsA && ri.operation().equals( "_is_a" ) ) return;
        if( !enabled ) return;
        if( !sendExceptionEnabled ) return;
        if( exceptionRedirectToOther && name.equals( "3" ) ) {
            exceptionRedirectToOther = false;
            throw new ForwardRequest( TestInitializer.helloRefForward );
        }
        if( !name.equals( "2" ) ) return;

        strategy.send_exception( this, ri );
    }

    public void send_other (ServerRequestInfo ri) 
        throws ForwardRequest 
    {
        // Only execute if the interceptor is enabled, this interception
        // point is enabled, we are the second interceptor, and we are 
        // executing on hello, not helloForward.
        if( ri.operation().equals( "syncWithServer" ) ) return;
        if( !dontIgnoreIsA && ri.operation().equals( "_is_a" ) ) return;
        if( !enabled ) return;
        if( !sendOtherEnabled ) return;
        if( !name.equals( "2" ) ) return;

        strategy.send_other( this, ri );

        // Make sure we do not invoke any more strategy methods.
        // This will effectively ignore invoking interceptors on forwards.
        if( !invokeOnForwardedObject ) {
            enabled = false;
        }
    }

}
