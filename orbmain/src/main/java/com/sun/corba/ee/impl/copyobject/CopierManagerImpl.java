/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates.
 * Copyright (c) 2021 Payara Services Ltd.
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

package com.sun.corba.ee.impl.copyobject ;

import com.sun.corba.ee.spi.copyobject.CopierManager;
import org.glassfish.pfl.basic.contain.DenseIntMapImpl;
import org.glassfish.pfl.dynamic.copyobject.spi.ObjectCopierFactory;

public class CopierManagerImpl implements CopierManager
{
    private int defaultId ;
    private final DenseIntMapImpl<ObjectCopierFactory> map ;

    public CopierManagerImpl()
    {
        defaultId = 0 ;
        map = new DenseIntMapImpl<>() ;
    }

    @Override
    public void setDefaultId( int id ) {
        defaultId = id ;
    }

    @Override
    public int getDefaultId() {
        return defaultId ;
    }

    @Override
    public ObjectCopierFactory getObjectCopierFactory(int id) {
        return map.get( id ) ;
    }

    @Override
    public ObjectCopierFactory getDefaultObjectCopierFactory() {
        return map.get( defaultId ) ;
    }

    @Override
    public void registerObjectCopierFactory(ObjectCopierFactory factory, int id) {
        map.set( id, factory ) ;
    }
}

