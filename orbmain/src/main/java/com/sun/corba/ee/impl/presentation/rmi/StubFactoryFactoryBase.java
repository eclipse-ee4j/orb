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

import com.sun.corba.ee.spi.presentation.rmi.PresentationManager;

import com.sun.corba.ee.impl.util.Utility ;

public abstract class StubFactoryFactoryBase implements
    PresentationManager.StubFactoryFactory
{
    /**
     * Returns the stub classname for the given interface name.
     *
     * @param fullName fully qualified name remote class
     */
    public String getStubName(String fullName) 
    {
        return Utility.stubName( fullName ) ;
    }
}
