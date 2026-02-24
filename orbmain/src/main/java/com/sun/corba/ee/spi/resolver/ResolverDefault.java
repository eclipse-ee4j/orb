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

package com.sun.corba.ee.spi.resolver ;

import com.sun.corba.ee.impl.resolver.BootstrapResolverImpl ;
import com.sun.corba.ee.impl.resolver.CompositeResolverImpl ;
import com.sun.corba.ee.impl.resolver.FileResolverImpl ;
import com.sun.corba.ee.impl.resolver.INSURLOperationImpl ;
import com.sun.corba.ee.impl.resolver.LocalResolverImpl ;
import com.sun.corba.ee.impl.resolver.ORBDefaultInitRefResolverImpl ;
import com.sun.corba.ee.impl.resolver.ORBInitRefResolverImpl ;
import com.sun.corba.ee.impl.resolver.SplitLocalResolverImpl ;
import com.sun.corba.ee.spi.orb.ORB ;
import com.sun.corba.ee.spi.orb.Operation ;

import java.io.File ;

import org.glassfish.pfl.basic.contain.Pair;

/** Utility class that provides factory methods for all of the 
 * standard resolvers that we provide.
 */
public class ResolverDefault {
    /** Return a local resolver that simply stores bindings in a map.
     * @return a new LocalResolverImpl
    */
    public static LocalResolver makeLocalResolver( ) 
    {
        return new LocalResolverImpl() ;
    }

    /** Return a resolver that relies on configured values of ORBInitRef for data.
     * @param urlOperation operation to get reference from URL
     * @param initRefs an array of Pairs oaf &lt;name of CORBA object, URL to get reference with&gt;
     * @return a new ORBInitRefResolver
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
    * @param orb ORB to use as resolver
    * @param host host of IOR
    * @param port port of IOR
    * @return a new BoostrapResolver
    */
    public static Resolver makeBootstrapResolver( ORB orb, String host, int port ) 
    {
        return new BootstrapResolverImpl( orb, host, port ) ;
    }

    /** Return a resolver composed of the two given resolvers.  result.list() is the 
    * union of first.list() and second.list().  result.resolve( name ) returns
    * first.resolve( name ) if that is not null, otherwise returns the result of
    * second.resolve( name ).
    * @param first first Resolver to try
    * @param second seconds Resolver to try
    * @return a new CompositeResolver
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

