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
import org.omg.IOP.*;

/**
 * Strategy to test effective_profile
 */
public class EffectiveProfileStrategy
    extends InterceptorStrategy
{

    public void send_request (
        SampleClientRequestInterceptor interceptor, ClientRequestInfo ri)
        throws ForwardRequest
    {
        super.send_request( interceptor, ri );

        try {
            testEffectiveProfile( "send_request", ri );
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
        super.receive_reply( interceptor, ri );

        try {
            testEffectiveProfile( "receive_reply", ri );
        }
        catch( Exception ex ) {
            failException( "receive_reply", ex );
        }
    }


    public void receive_exception (
        SampleClientRequestInterceptor interceptor, ClientRequestInfo ri) 
        throws ForwardRequest
    {
        super.receive_exception( interceptor, ri );

        try {
            testEffectiveProfile( "receive_exception", ri );
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
            testEffectiveProfile( "receive_other", ri );
        }
        catch( Exception ex ) {
            failException( "receive_other", ex ); 
        }
    }

    private void testEffectiveProfile( String methodName, 
                                       ClientRequestInfo ri ) 
    {
        String header = methodName + "(): ";
        TaggedProfile profile = ri.effective_profile();
        log( header + "effective_profile().tag = " + profile.tag );
        if( profile.tag != TAG_INTERNET_IOP.value ) {
            fail( header + "tag is not TAG_INTERNET_IOP." );
        }
    }

}
