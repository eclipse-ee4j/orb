/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.presentation.rmi.codegen;

import com.sun.corba.ee.spi.presentation.rmi.PresentationManager;

import com.sun.corba.ee.impl.presentation.rmi.StubFactoryFactoryDynamicBase;

public class StubFactoryFactoryCodegenImpl extends StubFactoryFactoryDynamicBase {
    public StubFactoryFactoryCodegenImpl() {
        super();
    }

    public PresentationManager.StubFactory makeDynamicStubFactory(PresentationManager pm, PresentationManager.ClassData classData, ClassLoader classLoader) {
        return new StubFactoryCodegenImpl(pm, classData, classLoader);
    }
}
