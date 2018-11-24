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

import com.sun.corba.ee.impl.presentation.rmi.*;
import java.lang.reflect.Proxy;

import com.sun.corba.ee.spi.presentation.rmi.PresentationManager;
import com.sun.corba.ee.spi.presentation.rmi.DynamicStub;
import org.glassfish.pfl.basic.proxy.InvocationHandlerFactory;
import org.glassfish.pfl.basic.proxy.LinkedInvocationHandler;

public class StubFactoryProxyImpl extends StubFactoryDynamicBase {
    public StubFactoryProxyImpl(PresentationManager.ClassData classData, ClassLoader loader) {
        super(classData, loader);
    }

    public org.omg.CORBA.Object makeStub() {
        // Construct the dynamic proxy that implements this stub
        // using the composite handler
        InvocationHandlerFactory factory = classData.getInvocationHandlerFactory();
        LinkedInvocationHandler handler = (LinkedInvocationHandler) factory.getInvocationHandler();
        Class[] interfaces = factory.getProxyInterfaces();
        DynamicStub stub = (DynamicStub) Proxy.newProxyInstance(loader, interfaces, handler);
        handler.setProxy((Proxy) stub);
        return stub;
    }
}
