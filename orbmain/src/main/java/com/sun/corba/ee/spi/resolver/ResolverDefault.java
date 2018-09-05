/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.spi.resolver ;

import java.io.File ;

import com.sun.corba.ee.impl.resolver.LocalResolverImpl ;
import com.sun.corba.ee.impl.resolver.ORBInitRefResolverImpl ;
import com.sun.corba.ee.impl.resolver.ORBDefaultInitRefResolverImpl ;
import com.sun.corba.ee.impl.resolver.BootstrapResolverImpl ;
import com.sun.corba.ee.impl.resolver.CompositeResolverImpl ;
import com.sun.corba.ee.impl.resolver.INSURLOperationImpl ;
import com.sun.corba.ee.impl.resolver.SplitLocalResolverImpl ;
import com.sun.corba.ee.impl.resolver.FileResolverImpl ;

import com.sun.corba.ee.spi.orb.ORB ;
import com.sun.corba.ee.spi.orb.Operation ;
import org.glassfish.pfl.basic.contain.Pair;

/** Utility class that provides factory methods for all of the 
 * standard resolvers that we provide.
 */
public class ResolverDefault {
    /** Return a local resolver that simply stores bindings in a map.
    */
    public static LocalResolver makeLocalResolver( ) 
    {
        return new LocalResolverImpl() ;
    }

    /** Return a resolver that relies on configured values of ORBInitRef for data.
    */
    public static Resolver makeORBInitRefResolver( Operation urlOperation,
        Pair<String,String>[] initRefs ) 
    {
        return new ORBInitRefResolverImpl( urlOperation, initRefs ) ;
    }

    public static Resolver makeORBDefaultInitRefResolver( Operation urlOperation,
        String defaultInitRef ) 
    {
        return new ORBDefaultInitRefResolverImpl( urlOperation,
            defaultInitRef ) ;
    }

    /** Return a resolver that uses the proprietary bootstrap protocol 
    * to implement a resolver.  Obtains the necessary host and port 
    * information from the ORB.
    */
    public static Resolver makeBootstrapResolver( ORB orb, String host, int port ) 
    {
        return new BootstrapResolverImpl( orb, host, port ) ;
    }

    /** Return a resolver composed of the two given resolvers.  result.list() is the 
    * union of first.list() and second.list().  result.resolve( name ) returns
    * first.resolve( name ) if that is not null, otherwise returns the result of
    * second.resolve( name ).
    */
    public static Resolver makeCompositeResolver( Resolver first, Resolver second ) 
    {
        return new CompositeResolverImpl( first, second ) ;
    }

    public static Operation makeINSURLOperation( ORB orb )
    {
        return new INSURLOperationImpl( orb ) ;
    }

    public static LocalResolver makeSplitLocalResolver( Resolver resolver,
        LocalResolver localResolver ) 
    {
        return new SplitLocalResolverImpl( resolver, localResolver ) ;
    }

    public static Resolver makeFileResolver( ORB orb, File file ) 
    {
        return new FileResolverImpl( orb, file ) ;
    }
}

