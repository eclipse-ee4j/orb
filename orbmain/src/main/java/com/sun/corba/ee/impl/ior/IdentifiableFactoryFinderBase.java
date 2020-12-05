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

package com.sun.corba.ee.impl.ior ;

import org.omg.CORBA_2_3.portable.InputStream ;

import java.util.Map ;
import java.util.HashMap ;

import com.sun.corba.ee.spi.orb.ORB ;

import com.sun.corba.ee.spi.ior.Identifiable ;
import com.sun.corba.ee.spi.ior.IdentifiableFactory ;
import com.sun.corba.ee.spi.ior.IdentifiableFactoryFinder ;

import com.sun.corba.ee.spi.logging.IORSystemException ;

public abstract class IdentifiableFactoryFinderBase<E extends Identifiable> 
    implements IdentifiableFactoryFinder<E>
{
    protected static final IORSystemException wrapper =
        IORSystemException.self ;

    private ORB orb ;
    private Map<Integer,IdentifiableFactory<E>> map ;

    protected IdentifiableFactoryFinderBase( ORB orb )
    {
        map = new HashMap<Integer,IdentifiableFactory<E>>() ;
        this.orb = orb ;
    }

    protected IdentifiableFactory<E> getFactory(int id) 
    {
        return map.get( id ) ;
    }

    public abstract E handleMissingFactory( int id, 
        InputStream is ) ;
        
    public E create(int id, InputStream is) 
    {
        IdentifiableFactory<E> factory = getFactory( id ) ;

        if (factory != null) {
            return factory.create(orb, is);
        } else {
            return handleMissingFactory(id, is);
        }
    }
    
    public void registerFactory(IdentifiableFactory<E> factory) 
    {
        map.put( factory.getId(), factory ) ;
    }
}
