/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.oa.rfm;

import org.omg.CORBA.LocalObject ;

import com.sun.corba.ee.spi.oa.rfm.ReferenceFactory ;
import com.sun.corba.ee.spi.oa.rfm.ReferenceFactoryManager ;

public class ReferenceFactoryImpl extends LocalObject implements ReferenceFactory
{
    private ReferenceFactoryManagerImpl manager ;
    private String name ;
    private String repositoryId ;

    public ReferenceFactoryImpl( ReferenceFactoryManagerImpl manager, 
        String name, String repositoryId ) {
        this.manager = manager ;
        this.name = name ;
        this.repositoryId = repositoryId ;
    }

    public org.omg.CORBA.Object createReference( byte[] key ) {
        return manager.createReference( name, key, repositoryId ) ;
    }

    public void destroy() {
        manager.destroy( name ) ;
    }

    public String toString()
    {
        return "ReferenceFactoryImpl["
            + name
            + ", "
            + repositoryId
            + "]";
    }
}
