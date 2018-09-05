/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.copyobject ;

import com.sun.corba.ee.spi.copyobject.CopierManager;
import org.glassfish.pfl.basic.contain.DenseIntMapImpl;
import org.glassfish.pfl.dynamic.copyobject.spi.ObjectCopierFactory;

public class CopierManagerImpl implements CopierManager
{
    private int defaultId ;
    private DenseIntMapImpl<ObjectCopierFactory> map ;

    public CopierManagerImpl()
    {
        defaultId = 0 ;
        map = new DenseIntMapImpl<ObjectCopierFactory>() ;
    }

    public void setDefaultId( int id ) 
    {
        defaultId = id ;
    }

    public int getDefaultId() 
    {
        return defaultId ;
    }

    public ObjectCopierFactory getObjectCopierFactory( int id ) 
    {
        return map.get( id ) ;
    }

    public ObjectCopierFactory getDefaultObjectCopierFactory()
    {
        return map.get( defaultId ) ;
    }

    public void registerObjectCopierFactory( ObjectCopierFactory factory, int id ) 
    {
        map.set( id, factory ) ;
    }
}

