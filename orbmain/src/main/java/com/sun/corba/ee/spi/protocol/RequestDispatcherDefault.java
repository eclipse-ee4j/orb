/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.spi.protocol ;


import com.sun.corba.ee.spi.protocol.LocalClientRequestDispatcherFactory ;
import com.sun.corba.ee.spi.protocol.ServerRequestDispatcher ;

import com.sun.corba.ee.spi.orb.ORB ;

// Used only in the implementation: no client of this class ever needs these
import com.sun.corba.ee.spi.ior.IOR ;

import com.sun.corba.ee.impl.protocol.ClientRequestDispatcherImpl ;
import com.sun.corba.ee.impl.protocol.ServerRequestDispatcherImpl ;
import com.sun.corba.ee.impl.protocol.MinimalServantCacheLocalCRDImpl ;
import com.sun.corba.ee.impl.protocol.InfoOnlyServantCacheLocalCRDImpl ;
import com.sun.corba.ee.impl.protocol.FullServantCacheLocalCRDImpl ;
import com.sun.corba.ee.impl.protocol.JIDLLocalCRDImpl ;
import com.sun.corba.ee.impl.protocol.POALocalCRDImpl ;
import com.sun.corba.ee.impl.protocol.INSServerRequestDispatcher ;
import com.sun.corba.ee.impl.protocol.BootstrapServerRequestDispatcher ;

public final class RequestDispatcherDefault {
    private RequestDispatcherDefault() {}

    public static ClientRequestDispatcher makeClientRequestDispatcher()
    {
        return new ClientRequestDispatcherImpl() ;
    }

    public static ServerRequestDispatcher makeServerRequestDispatcher( ORB orb )
    {
        return new ServerRequestDispatcherImpl( (com.sun.corba.ee.spi.orb.ORB)orb ) ;
    }

    public static ServerRequestDispatcher makeBootstrapServerRequestDispatcher( ORB orb )
    {
        return new BootstrapServerRequestDispatcher( orb ) ;
    }

    public static ServerRequestDispatcher makeINSServerRequestDispatcher( ORB orb )
    {
        return new INSServerRequestDispatcher( orb ) ;
    }

    public static LocalClientRequestDispatcherFactory makeMinimalServantCacheLocalClientRequestDispatcherFactory( final ORB orb ) 
    {
        return new LocalClientRequestDispatcherFactory() {
            public LocalClientRequestDispatcher create( int id, IOR ior ) {
                return new MinimalServantCacheLocalCRDImpl( orb, id, ior ) ;
            }
        } ;
    }

    public static LocalClientRequestDispatcherFactory makeInfoOnlyServantCacheLocalClientRequestDispatcherFactory( final ORB orb ) 
    {
        return new LocalClientRequestDispatcherFactory() {
            public LocalClientRequestDispatcher create( int id, IOR ior ) {
                return new InfoOnlyServantCacheLocalCRDImpl( orb, id, ior ) ;
            }
        } ;
    }

    public static LocalClientRequestDispatcherFactory makeFullServantCacheLocalClientRequestDispatcherFactory( final ORB orb ) 
    {
        return new LocalClientRequestDispatcherFactory() {
            public LocalClientRequestDispatcher create( int id, IOR ior ) {
                return new FullServantCacheLocalCRDImpl( orb, id, ior ) ;
            }
        } ;
    }

    public static LocalClientRequestDispatcherFactory makeJIDLLocalClientRequestDispatcherFactory( final ORB orb ) 
    {
        return new LocalClientRequestDispatcherFactory() {
            public LocalClientRequestDispatcher create( int id, IOR ior ) {
                return new JIDLLocalCRDImpl( orb, id, ior ) ;
            }
        } ;
    }

    public static LocalClientRequestDispatcherFactory makePOALocalClientRequestDispatcherFactory( final ORB orb ) 
    {
        return new LocalClientRequestDispatcherFactory() {
            public LocalClientRequestDispatcher create( int id, IOR ior ) {
                return new POALocalCRDImpl( orb, id, ior ) ;
            }
        } ;
    }
}
