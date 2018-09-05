/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.protocol ;

import java.util.Iterator ;

import org.omg.CORBA.SystemException ;

import com.sun.corba.ee.spi.ior.IOR ;
import com.sun.corba.ee.spi.ior.ObjectKey ;
import com.sun.corba.ee.spi.orb.ORB ;
import com.sun.corba.ee.spi.protocol.ServerRequestDispatcher ;
import com.sun.corba.ee.spi.protocol.MessageMediator;

import com.sun.corba.ee.impl.encoding.MarshalInputStream ;
import com.sun.corba.ee.impl.encoding.MarshalOutputStream ;

import com.sun.corba.ee.spi.logging.ORBUtilSystemException ;

/**
 * Class BootstrapServerRequestDispatcher handles the requests coming to the
 * BootstrapServer. It implements Server so that it can be registered
 * as a subcontract. It is passed a BootstrapServiceProperties object
 * which contains
 * the supported ids and their values for the bootstrap service. This
 * Properties object is only read from, never written to, and is shared
 * among all threads.
 * <p>
 * The BootstrapServerRequestDispatcher responds primarily to GIOP requests,
 * but LocateRequests are also handled for graceful interoperability.
 * The BootstrapServerRequestDispatcher handles one request at a time.
 */
public class BootstrapServerRequestDispatcher 
    implements ServerRequestDispatcher
{
    private ORB orb;

    static final ORBUtilSystemException wrapper =
        ORBUtilSystemException.self ;

    private static final boolean debug = false;

    public BootstrapServerRequestDispatcher(ORB orb )
    {
        this.orb = orb;
    }
    
    /**
     * Dispatch is called by the ORB and will serve get(key) and list()
     * invocations on the initial object key.
     */
    public void dispatch(MessageMediator messageMediator)
    {
        MessageMediator request = (MessageMediator) messageMediator;
        MessageMediator response = null;

        try {
            MarshalInputStream is = (MarshalInputStream) 
                request.getInputObject();
            String method = request.getOperationName();
            response = request.getProtocolHandler().createResponse(request, null);
            MarshalOutputStream os = (MarshalOutputStream) 
                response.getOutputObject();

            if (method.equals("get")) {
                // Get the name of the requested service
                String serviceKey = is.read_string();

                // Look it up
                org.omg.CORBA.Object serviceObject = 
                    orb.getLocalResolver().resolve( serviceKey ) ;

                // Write reply value
                os.write_Object(serviceObject);
            } else if (method.equals("list")) {
                java.util.Set keys = orb.getLocalResolver().list() ;
                os.write_long( keys.size() ) ;
                Iterator iter = keys.iterator() ;
                while (iter.hasNext()) {
                    String obj = (String)iter.next() ;
                    os.write_string( obj ) ;
                }
            } else {
                throw wrapper.illegalBootstrapOperation( method ) ;
            }

        } catch (org.omg.CORBA.SystemException ex) {
            // Marshal the exception thrown
            response = request.getProtocolHandler().createSystemExceptionResponse(
                request, ex, null);
        } catch (java.lang.RuntimeException ex) {
            // Unknown exception
            SystemException sysex = wrapper.bootstrapRuntimeException( ex ) ;
            response = request.getProtocolHandler().createSystemExceptionResponse(
                 request, sysex, null ) ;
        } catch (java.lang.Exception ex) {
            // Unknown exception
            SystemException sysex = wrapper.bootstrapException( ex ) ;
            response = request.getProtocolHandler().createSystemExceptionResponse(
                 request, sysex, null ) ;
        }

        return;
    }

    /**
     * Locates the object mentioned in the locate requests, and returns
     * object here iff the object is the initial object key. A SystemException
     * thrown if the object key is not the initial object key.
     */
    public IOR locate( ObjectKey objectKey) {
        return null;
    }

    /**
     * Not implemented
     */
    public int getId() {
        throw wrapper.genericNoImpl() ;
    }
}
