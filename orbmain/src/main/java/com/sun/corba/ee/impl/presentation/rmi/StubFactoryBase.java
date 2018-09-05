/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.presentation.rmi ;

import javax.rmi.CORBA.Tie ;

import java.lang.reflect.InvocationHandler ;
import java.lang.reflect.Proxy ;

import com.sun.corba.ee.spi.presentation.rmi.PresentationManager ;
import com.sun.corba.ee.spi.presentation.rmi.DynamicStub ;
import com.sun.corba.ee.spi.presentation.rmi.StubAdapter ;

public abstract class StubFactoryBase implements PresentationManager.StubFactory 
{
    private String[] typeIds = null ;

    protected final PresentationManager.ClassData classData ;

    protected StubFactoryBase( PresentationManager.ClassData classData ) 
    {
        this.classData = classData ;
    }

    public synchronized String[] getTypeIds()
    {
        if (typeIds == null) {
            if (classData == null) {
                org.omg.CORBA.Object stub = makeStub() ;
                typeIds = StubAdapter.getTypeIds( stub ) ;
            } else {
                typeIds = classData.getTypeIds() ;
            }
        }

        return typeIds ;
    }
}
