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

package com.sun.corba.ee.impl.resolver ;

import com.sun.corba.ee.spi.resolver.Resolver ;

import com.sun.corba.ee.spi.orb.Operation ;
import java.util.Set;
import org.glassfish.pfl.basic.contain.Pair;

public class ORBInitRefResolverImpl implements Resolver {
    Operation urlHandler ;
    java.util.Map orbInitRefTable ;

    /**
     * Creates a new Resolver
     * @param urlHandler operation to get reference from URL
     * @param initRefs an array of Pairs of &lt;name of CORBA object, URL to get reference with&gt;
     */
    public ORBInitRefResolverImpl( Operation urlHandler, Pair<String,String>[] initRefs ) 
    {
        this.urlHandler = urlHandler ;
        orbInitRefTable = new java.util.HashMap() ;

        for( int i = 0; i < initRefs.length ; i++ ) {
            Pair<String,String> sp = initRefs[i] ;
            orbInitRefTable.put( sp.first(), sp.second() ) ;
        }
    }

    @Override
    public org.omg.CORBA.Object resolve( String ident )
    {
        String url = (String)orbInitRefTable.get( ident ) ;
        if (url == null)
            return null ;

        org.omg.CORBA.Object result = 
            (org.omg.CORBA.Object)urlHandler.operate( url ) ;
        return result ;
    }

    @Override
    public Set<String> list()
    {
        return orbInitRefTable.keySet() ;
    }
}
