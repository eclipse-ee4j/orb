/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.presentation.rmi;

import java.lang.reflect.InvocationHandler ;

import com.sun.corba.ee.spi.presentation.rmi.PresentationManager;

public class StubFactoryStaticImpl extends StubFactoryBase
{
    private Class stubClass ;

    public StubFactoryStaticImpl(Class cls)
    {
        super( null ) ;
        this.stubClass = cls;
    }
    
    public org.omg.CORBA.Object makeStub()
    {
        org.omg.CORBA.Object stub = null;
        try {
            stub = (org.omg.CORBA.Object) stubClass.newInstance();
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return stub ;
    }
}
