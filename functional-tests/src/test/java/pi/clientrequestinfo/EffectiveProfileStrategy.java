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
