/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.copyobject;

import java.rmi.Remote;

import org.omg.CORBA.ORB;

import com.sun.corba.ee.impl.util.Utility;

import org.glassfish.pfl.dynamic.copyobject.impl.JavaStreamObjectCopierImpl;

public class JavaStreamORBObjectCopierImpl extends JavaStreamObjectCopierImpl {
    private ORB orb;

    public JavaStreamORBObjectCopierImpl(ORB orb) {
        this.orb = orb;
    }

    public Object copy(Object obj, boolean debug) {
        return copy(obj);
    }

    @Override
    public Object copy(Object obj) {
        if (obj instanceof Remote) {
            // Yes, so make sure it is connected and converted
            // to a stub (if needed)...
            return Utility.autoConnect(obj, orb, true);
        }

        return super.copy(obj);
    }
}
