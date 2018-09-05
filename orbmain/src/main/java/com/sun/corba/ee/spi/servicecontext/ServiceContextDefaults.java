/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.spi.servicecontext;

import org.omg.CORBA_2_3.portable.InputStream ;

import com.sun.corba.ee.spi.orb.ORB ;
import com.sun.corba.ee.spi.orb.ORBVersion ;

import com.sun.corba.ee.spi.ior.IOR ;

import com.sun.corba.ee.spi.ior.iiop.GIOPVersion ;

import com.sun.corba.ee.spi.servicecontext.ServiceContexts ;
import com.sun.corba.ee.spi.servicecontext.ServiceContext ;
import com.sun.corba.ee.spi.servicecontext.ServiceContextFactoryRegistry ;
import com.sun.corba.ee.spi.servicecontext.CodeSetServiceContext ;
import com.sun.corba.ee.spi.servicecontext.ORBVersionServiceContext ;
import com.sun.corba.ee.spi.servicecontext.MaxStreamFormatVersionServiceContext ;
import com.sun.corba.ee.spi.servicecontext.UEInfoServiceContext ;
import com.sun.corba.ee.spi.servicecontext.UnknownServiceContext ;

import com.sun.corba.ee.impl.servicecontext.ServiceContextsImpl ;
import com.sun.corba.ee.impl.servicecontext.ServiceContextFactoryRegistryImpl ;
import com.sun.corba.ee.impl.servicecontext.CodeSetServiceContextImpl ;
import com.sun.corba.ee.impl.servicecontext.ORBVersionServiceContextImpl ;
import com.sun.corba.ee.impl.servicecontext.MaxStreamFormatVersionServiceContextImpl ;
import com.sun.corba.ee.impl.servicecontext.UEInfoServiceContextImpl ;
import com.sun.corba.ee.impl.servicecontext.UnknownServiceContextImpl ;
import com.sun.corba.ee.impl.servicecontext.SendingContextServiceContextImpl ;

import com.sun.corba.ee.impl.encoding.CodeSetComponentInfo ;

import com.sun.corba.ee.spi.orb.ORBVersionFactory;


public abstract class ServiceContextDefaults {

    private static ORBVersion orbVersion = ORBVersionFactory.getORBVersion();
    private static ORBVersionServiceContext orbVersionContext = 
                        new ORBVersionServiceContextImpl( orbVersion );

    private ServiceContextDefaults() {}

    public static ServiceContexts makeServiceContexts( ORB orb )
    {
        return new ServiceContextsImpl( orb ) ;
    }

    public static ServiceContexts makeServiceContexts( InputStream is ) 
    {
        return new ServiceContextsImpl( is ) ;
    }

    public static ServiceContextFactoryRegistry makeServiceContextFactoryRegistry( 
        ORB orb ) 
    {
        return new ServiceContextFactoryRegistryImpl( orb ) ;
    }

    public static CodeSetServiceContext makeCodeSetServiceContext( 
        CodeSetComponentInfo.CodeSetContext csc ) 
    {
        return new CodeSetServiceContextImpl( csc ) ;
    }

    public static ServiceContext.Factory makeCodeSetServiceContextFactory()
    {
        return new ServiceContext.Factory() {
            public int getId()
            {
                return CodeSetServiceContext.SERVICE_CONTEXT_ID ;
            }

            public ServiceContext create( InputStream s, GIOPVersion gv )
            {
                return new CodeSetServiceContextImpl( s, gv ) ;
            }
        } ;
    }

    public static ServiceContext.Factory 
        makeMaxStreamFormatVersionServiceContextFactory()
    {
        return new ServiceContext.Factory() {
            public int getId()
            {
                return MaxStreamFormatVersionServiceContext.SERVICE_CONTEXT_ID ;
            }

            public ServiceContext create( InputStream s, GIOPVersion gv )
            {
                return new MaxStreamFormatVersionServiceContextImpl( s, gv ) ;
            }
        } ;
    }

    public static MaxStreamFormatVersionServiceContext 
        getMaxStreamFormatVersionServiceContext()
    {
        return MaxStreamFormatVersionServiceContextImpl.singleton ;
    }

    public static MaxStreamFormatVersionServiceContext 
        makeMaxStreamFormatVersionServiceContext( byte version )
    {
        return new MaxStreamFormatVersionServiceContextImpl( version ) ;
    }

    public static ServiceContext.Factory makeORBVersionServiceContextFactory()
    {
        return new ServiceContext.Factory() {
            public int getId()
            {
                return ORBVersionServiceContext.SERVICE_CONTEXT_ID ;
            }

            public ServiceContext create( InputStream s, GIOPVersion gv )
            {
                return new ORBVersionServiceContextImpl( s, gv ) ;
            }
        } ;
    }

    public static ORBVersionServiceContext getORBVersionServiceContext()
    {
        return ORBVersionServiceContextImpl.singleton ;
    }

    public static ORBVersionServiceContext makeORBVersionServiceContext()
    {
        return orbVersionContext ;
    }

    public static ServiceContext.Factory makeSendingContextServiceContextFactory()
    {
        return new ServiceContext.Factory() {
            public int getId()
            {
                return SendingContextServiceContext.SERVICE_CONTEXT_ID ;
            }

            public ServiceContext create( InputStream s, GIOPVersion gv )
            {
                return new SendingContextServiceContextImpl( s, gv ) ;
            }
        } ;
    }

    public static SendingContextServiceContext 
        makeSendingContextServiceContext( IOR ior )
    {
        return new SendingContextServiceContextImpl( ior ) ;
    }

    public static ServiceContext.Factory makeUEInfoServiceContextFactory()
    {
        return new ServiceContext.Factory() {
            public int getId()
            {
                return UEInfoServiceContext.SERVICE_CONTEXT_ID ;
            }

            public ServiceContext create( InputStream s, GIOPVersion gv )
            {
                return new UEInfoServiceContextImpl( s, gv ) ;
            }
        } ;
    }

    public static UEInfoServiceContext 
        makeUEInfoServiceContext( Throwable thr )
    {
        return new UEInfoServiceContextImpl( thr ) ;
    }

    public static UnknownServiceContext 
        makeUnknownServiceContext( int id, byte[] data )
    {
        return new UnknownServiceContextImpl( id, data ) ;
    }

    public static UnknownServiceContext 
        makeUnknownServiceContext( int id, InputStream str )
    {
        return new UnknownServiceContextImpl( id, str ) ;
    }
}

