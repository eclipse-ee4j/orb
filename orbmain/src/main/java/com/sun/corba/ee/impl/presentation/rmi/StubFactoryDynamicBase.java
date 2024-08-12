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

import com.sun.corba.ee.spi.presentation.rmi.PresentationManager;

import java.io.SerializablePermission;

import com.sun.corba.ee.spi.misc.ORBClassLoader;

public abstract class StubFactoryDynamicBase extends StubFactoryBase {
    protected final ClassLoader loader;

    private static Void checkPermission() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new SerializablePermission("enableSubclassImplementation"));
        }

        return null;
    }

    private StubFactoryDynamicBase(Void unused, PresentationManager.ClassData classData, ClassLoader loader) {
        super(classData);

        // this.loader must not be null, or the newProxyInstance call
        // will fail.
        if (loader == null) {
            this.loader = ORBClassLoader.getClassLoader();
        } else {
            this.loader = loader;
        }
    }

    public StubFactoryDynamicBase(PresentationManager.ClassData classData, ClassLoader loader) {
        this(checkPermission(), classData, loader);
    }

    public abstract org.omg.CORBA.Object makeStub();
}
