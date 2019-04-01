/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.presentation.rmi.proxy;

import java.security.AccessController;
import java.security.PrivilegedAction;

import com.sun.corba.ee.impl.presentation.rmi.*;
import com.sun.corba.ee.impl.presentation.rmi.proxy.StubFactoryProxyImpl;
import com.sun.corba.ee.spi.presentation.rmi.PresentationManager;
import com.sun.corba.ee.spi.presentation.rmi.PresentationManager.StubFactory;

public class StubFactoryFactoryProxyImpl extends StubFactoryFactoryDynamicBase {
    public PresentationManager.StubFactory makeDynamicStubFactory(PresentationManager pm, final PresentationManager.ClassData classData,
            final ClassLoader classLoader) {
        return AccessController.doPrivileged(new PrivilegedAction<PresentationManager.StubFactory>() {

            @Override
            public StubFactory run() {
                return new StubFactoryProxyImpl(classData, classLoader);
            }

        });
    }
}
