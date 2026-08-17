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

public class StubFactoryStaticImpl extends StubFactoryBase {
    private Class stubClass;

    public StubFactoryStaticImpl(Class cls) {
        super(null);
        this.stubClass = cls;
    }

    @Override
    public org.omg.CORBA.Object makeStub() {
        org.omg.CORBA.Object stub = null;
        try {
            stub = (org.omg.CORBA.Object) stubClass.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException
                | java.lang.reflect.InvocationTargetException e) {
            // The last two arrived with getDeclaredConstructor().newInstance(), which replaced the
            // deprecated Class.newInstance(): a missing no-arg constructor and an exception thrown by
            // the constructor. Both are as fatal here as the two that were already handled.
            throw new RuntimeException(e);
        }
        return stub;
    }
}
