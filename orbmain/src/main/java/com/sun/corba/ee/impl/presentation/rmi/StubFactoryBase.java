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

import javax.rmi.CORBA.Tie;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

import com.sun.corba.ee.spi.presentation.rmi.PresentationManager;
import com.sun.corba.ee.spi.presentation.rmi.DynamicStub;
import com.sun.corba.ee.spi.presentation.rmi.StubAdapter;

public abstract class StubFactoryBase implements PresentationManager.StubFactory {
    private String[] typeIds = null;

    protected final PresentationManager.ClassData classData;

    protected StubFactoryBase(PresentationManager.ClassData classData) {
        this.classData = classData;
    }

    public synchronized String[] getTypeIds() {
        if (typeIds == null) {
            if (classData == null) {
                org.omg.CORBA.Object stub = makeStub();
                typeIds = StubAdapter.getTypeIds(stub);
            } else {
                typeIds = classData.getTypeIds();
            }
        }

        return typeIds;
    }
}
