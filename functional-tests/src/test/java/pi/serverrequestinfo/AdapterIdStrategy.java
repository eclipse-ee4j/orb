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
import com.sun.corba.ee.impl.misc.HexOutputStream;

import java.util.*;
import java.io.*;

/**
 * Strategy to further test adapter_id() (only executed for POA)
 */
public class AdapterIdStrategy
    extends InterceptorStrategy
{

    // The request count. We should be calling:
    //   0 - sayHello
    //   1 - saySystemException
    //   2 - saySystemException.
    private int count = 0;

    // Set from POAServer.  This is the adapter ID to test against.
    private byte[] expectedAdapterId;

    public AdapterIdStrategy( byte[] expectedAdapterId ) {
        this.expectedAdapterId = expectedAdapterId;
    }

    public void receive_request_service_contexts (
        SampleServerRequestInterceptor interceptor, ServerRequestInfo ri)
        throws ForwardRequest
    {
        // We already checked that adapter_id is invalid in rrsc.
        try {
            super.receive_request_service_contexts( interceptor, ri );
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
            checkAdapterId( "receive_request", ri.adapter_id() );
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
            checkAdapterId( "send_reply", ri.adapter_id() );
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
            checkAdapterId( "send_exception", ri.adapter_id() );
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
            checkAdapterId( "send_other", ri.adapter_id() );
        }
        catch( Exception ex ) {
            failException( "send_other", ex );
        }
    }

    private void checkAdapterId( String method, byte[] adapterId ) {
        log( method + "(): Actual adapter id = " + dumpHex( adapterId ) );
        if( Arrays.equals( adapterId, expectedAdapterId ) ) {
            log( method + "(): Adapter id compares." );
        }
        else {
            fail( method + "(): Adapter id does not compare.  " +
                "(expected id: " + dumpHex( expectedAdapterId ) + ")" );
        }
    }

    private String dumpHex( byte[] bytes ) {
        StringWriter sw = new StringWriter();
        HexOutputStream out = new HexOutputStream( sw );
        try {
            out.write( bytes );
        }
        catch( IOException e ) {}
        return sw.toString();
    }

}

