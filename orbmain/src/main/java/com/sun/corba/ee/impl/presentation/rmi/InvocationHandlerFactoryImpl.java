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

package com.sun.corba.ee.impl.presentation.rmi;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

import java.io.ObjectStreamException;
import java.io.Serializable;

import com.sun.corba.ee.spi.presentation.rmi.PresentationManager;
import com.sun.corba.ee.spi.presentation.rmi.DynamicStub;
import org.glassfish.pfl.basic.proxy.CompositeInvocationHandler;
import org.glassfish.pfl.basic.proxy.CompositeInvocationHandlerImpl;
import org.glassfish.pfl.basic.proxy.DelegateInvocationHandlerImpl;
import org.glassfish.pfl.basic.proxy.InvocationHandlerFactory;
import org.glassfish.pfl.basic.proxy.LinkedInvocationHandler;

public class InvocationHandlerFactoryImpl implements InvocationHandlerFactory {
    private final PresentationManager.ClassData classData;
    private final PresentationManager pm;
    private Class<?>[] proxyInterfaces;

    public InvocationHandlerFactoryImpl(PresentationManager pm, PresentationManager.ClassData classData) {
        this.classData = classData;
        this.pm = pm;

        Class<?>[] remoteInterfaces = classData.getIDLNameTranslator().getInterfaces();
        proxyInterfaces = new Class<?>[remoteInterfaces.length + 1];
        System.arraycopy(remoteInterfaces, 0, proxyInterfaces, 0, remoteInterfaces.length);

        proxyInterfaces[remoteInterfaces.length] = DynamicStub.class;
    }

    private static class CustomCompositeInvocationHandlerImpl extends CompositeInvocationHandlerImpl
            implements LinkedInvocationHandler, Serializable {
        private transient DynamicStub stub;

        public void setProxy(Proxy proxy) {
            if (proxy instanceof DynamicStub) {
                ((DynamicStubImpl) stub).setSelf((DynamicStub) proxy);
            } else {
                throw new RuntimeException("Proxy not instance of DynamicStub");
            }
        }

        public Proxy getProxy() {
            return (Proxy) ((DynamicStubImpl) stub).getSelf();
        }

        public CustomCompositeInvocationHandlerImpl(DynamicStub stub) {
            this.stub = stub;
        }

        /**
         * Return the stub, which will actually be written to the stream. It will be custom marshaled, with the actual writing
         * done in StubIORImpl. There is a corresponding readResolve method on DynamicStubImpl which will re-create the full
         * invocation handler on read, and return the invocation handler on the readResolve method.
         */
        public Object writeReplace() throws ObjectStreamException {
            return stub;
        }
    }

    public InvocationHandler getInvocationHandler() {
        final DynamicStub stub = new DynamicStubImpl(classData.getTypeIds());

        return getInvocationHandler(stub);
    }

    // This is also used in DynamicStubImpl to implement readResolve.
    InvocationHandler getInvocationHandler(DynamicStub stub) {
        // Create an invocation handler for the methods defined on DynamicStub,
        // which extends org.omg.CORBA.Object. This handler delegates all
        // calls directly to a DynamicStubImpl, which extends
        // org.omg.CORBA.portable.ObjectImpl.
        InvocationHandler dynamicStubHandler = DelegateInvocationHandlerImpl.create(stub);

        // Create an invocation handler that handles any remote interface
        // methods.
        InvocationHandler stubMethodHandler = new StubInvocationHandlerImpl(pm, classData, stub);

        // Create a composite handler that handles the DynamicStub interface
        // as well as the remote interfaces.
        final CompositeInvocationHandler handler = new CustomCompositeInvocationHandlerImpl(stub);
        handler.addInvocationHandler(DynamicStub.class, dynamicStubHandler);
        handler.addInvocationHandler(org.omg.CORBA.Object.class, dynamicStubHandler);
        handler.addInvocationHandler(Object.class, dynamicStubHandler);

        // If the method passed to invoke is not from DynamicStub or its superclasses,
        // it must be from an implemented interface, so we just handle
        // all of these with the stubMethodHandler. This used to be
        // done be adding explicit entries for stubMethodHandler for
        // each remote interface, but that does not work correctly
        // for abstract interfaces, since the graph analysis ignores
        // abstract interfaces in order to compute the type ids
        // correctly (see PresentationManagerImpl.NodeImpl.getChildren).
        // Rather than produce more graph traversal code to handle this
        // problem, we simply use a default.
        // This also points to a possible optimization: just use explict
        // checks for the three special classes, rather than a general
        // table lookup that usually fails.
        handler.setDefaultHandler(stubMethodHandler);

        return handler;
    }

    public Class[] getProxyInterfaces() {
        return proxyInterfaces;
    }
}
