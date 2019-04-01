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

import javax.rmi.CORBA.Tie;

import org.omg.CORBA.CompletionStatus;

import com.sun.corba.ee.spi.presentation.rmi.PresentationManager;

import com.sun.corba.ee.spi.orb.ORB;

import com.sun.corba.ee.spi.logging.ORBUtilSystemException;

import com.sun.corba.ee.impl.javax.rmi.CORBA.Util;

import com.sun.corba.ee.impl.misc.ClassInfoCache;

public abstract class StubFactoryFactoryDynamicBase extends StubFactoryFactoryBase {
    protected static final ORBUtilSystemException wrapper = ORBUtilSystemException.self;

    public StubFactoryFactoryDynamicBase() {
    }

    public PresentationManager.StubFactory createStubFactory(String className, boolean isIDLStub, String remoteCodeBase, Class expectedClass,
            ClassLoader classLoader) {
        Class cls = null;

        try {
            cls = Util.getInstance().loadClass(className, remoteCodeBase, classLoader);
        } catch (ClassNotFoundException exc) {
            throw wrapper.classNotFound3(exc, className);
        }

        ClassInfoCache.ClassInfo cinfo = ClassInfoCache.get(cls);
        PresentationManager pm = ORB.getPresentationManager();

        if (cinfo.isAIDLEntity(cls) && !cinfo.isARemote(cls)) {
            // IDL stubs must always use static factories.
            PresentationManager.StubFactoryFactory sff = pm.getStaticStubFactoryFactory();
            return sff.createStubFactory(className, true, remoteCodeBase, expectedClass, classLoader);
        } else {
            PresentationManager.ClassData classData = pm.getClassData(cls);
            return makeDynamicStubFactory(pm, classData, classLoader);
        }
    }

    public abstract PresentationManager.StubFactory makeDynamicStubFactory(PresentationManager pm, PresentationManager.ClassData classData,
            ClassLoader classLoader);

    public Tie getTie(Class cls) {
        PresentationManager pm = ORB.getPresentationManager();
        return new ReflectiveTie(pm);
    }

    public boolean createsDynamicStubs() {
        return true;
    }
}
