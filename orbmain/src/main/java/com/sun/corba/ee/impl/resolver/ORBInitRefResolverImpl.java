/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.resolver;

import com.sun.corba.ee.spi.resolver.Resolver;

import com.sun.corba.ee.spi.orb.Operation;
import java.util.Set;
import org.glassfish.pfl.basic.contain.Pair;

public class ORBInitRefResolverImpl implements Resolver {
    Operation urlHandler;
    java.util.Map orbInitRefTable;

    public ORBInitRefResolverImpl(Operation urlHandler, Pair<String, String>[] initRefs) {
        this.urlHandler = urlHandler;
        orbInitRefTable = new java.util.HashMap();

        for (int i = 0; i < initRefs.length; i++) {
            Pair<String, String> sp = initRefs[i];
            orbInitRefTable.put(sp.first(), sp.second());
        }
    }

    public org.omg.CORBA.Object resolve(String ident) {
        String url = (String) orbInitRefTable.get(ident);
        if (url == null)
            return null;

        org.omg.CORBA.Object result = (org.omg.CORBA.Object) urlHandler.operate(url);
        return result;
    }

    public Set<String> list() {
        return orbInitRefTable.keySet();
    }
}
