/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.resolver ;

import org.omg.CORBA.portable.InputStream ;
import org.omg.CORBA.portable.OutputStream ;
import org.omg.CORBA.portable.ApplicationException ;
import org.omg.CORBA.portable.RemarshalException ;

import com.sun.corba.ee.spi.ior.IOR ;
import com.sun.corba.ee.spi.ior.IORFactories ;
import com.sun.corba.ee.spi.ior.IORTemplate ;
import com.sun.corba.ee.spi.ior.ObjectKey ;
import com.sun.corba.ee.spi.ior.iiop.IIOPAddress ;
import com.sun.corba.ee.spi.ior.iiop.IIOPProfileTemplate ;
import com.sun.corba.ee.spi.ior.iiop.IIOPFactories ;
import com.sun.corba.ee.spi.ior.iiop.GIOPVersion ;
import com.sun.corba.ee.spi.orb.ORB ;
import com.sun.corba.ee.spi.resolver.Resolver ;

import com.sun.corba.ee.impl.ior.ObjectIdImpl;
import com.sun.corba.ee.impl.ior.ObjectKeyImpl;
import com.sun.corba.ee.spi.logging.ORBUtilSystemException ;
import com.sun.corba.ee.impl.misc.ORBUtility ;
import java.util.Set;

public class BootstrapResolverImpl implements Resolver {
    private org.omg.CORBA.portable.Delegate bootstrapDelegate ;
    private static final ORBUtilSystemException wrapper =
        ORBUtilSystemException.self ;

    public BootstrapResolverImpl(ORB orb, String host, int port) {
        // Create a new IOR with the magic of INIT
        byte[] initialKey = "INIT".getBytes();
        ObjectKey okey = new ObjectKeyImpl(orb.getWireObjectKeyTemplate(),
                                           new ObjectIdImpl(initialKey));

        IIOPAddress addr = IIOPFactories.makeIIOPAddress( host, port ) ;
        IIOPProfileTemplate ptemp = IIOPFactories.makeIIOPProfileTemplate(
            orb, GIOPVersion.V1_0, addr);
            
        IORTemplate iortemp = IORFactories.makeIORTemplate( okey.getTemplate() ) ;
        iortemp.add( ptemp ) ;

        IOR initialIOR = iortemp.makeIOR( orb, "", okey.getId() ) ;

        bootstrapDelegate = ORBUtility.makeClientDelegate( initialIOR ) ;       
    }

    /**
     * For the BootStrap operation we do not expect to have more than one 
     * parameter. We do not want to extend BootStrap protocol any further,
     * as INS handles most of what BootStrap can handle in a portable way.
     *
     * @return InputStream which contains the response from the 
     * BootStrapOperation.
     */
    private InputStream invoke( String operationName, String parameter )
    { 
        boolean remarshal = true;

        // Invoke.

        InputStream inStream = null;

        // If there is a location forward then you will need
        // to invoke again on the updated information.
        // Just calling this same routine with the same host/port
        // does not take the location forward info into account.

        while (remarshal) {
            org.omg.CORBA.Object objref = null ;
            remarshal = false;

            OutputStream os = bootstrapDelegate.request(objref, operationName,
                true);

            if ( parameter != null ) {
                os.write_string( parameter );
            }

            try {
                // The only reason a null objref is passed is to get the version of
                // invoke used by streams.  Otherwise the PortableInterceptor
                // call stack will become unbalanced since the version of
                // invoke which only takes the stream does not call 
                // PortableInterceptor ending points.
                // Note that the first parameter is ignored inside invoke.

                inStream = bootstrapDelegate.invoke( objref, os);
            } catch (ApplicationException e) {
                throw wrapper.bootstrapApplicationException( e ) ;
            } catch (RemarshalException e) {
                wrapper.bootstrapRemarshalException( e ) ;
                remarshal = true;
            }
        }

        return inStream;
    }

    public org.omg.CORBA.Object resolve( String identifier ) 
    {
        InputStream inStream = null ;
        org.omg.CORBA.Object result = null ;

        try { 
            inStream = invoke( "get", identifier ) ;

            result = inStream.read_Object();

            // NOTE: do note trap and ignore errors.
            // Let them flow out.
        } finally {
            bootstrapDelegate.releaseReply( null, inStream ) ;
        }

        return result ;
    }

    public Set<String> list()
    {
        InputStream inStream = null ;
        java.util.Set result = new java.util.HashSet() ;

        try {
            inStream = invoke( "list", null ) ;

            int count = inStream.read_long();
            for (int i=0; i < count; i++) {
                result.add(inStream.read_string());
            }

            // NOTE: do note trap and ignore errors.
            // Let them flow out.
        } finally {
            bootstrapDelegate.releaseReply( null, inStream ) ;
        }

        return result ;
    }
}
