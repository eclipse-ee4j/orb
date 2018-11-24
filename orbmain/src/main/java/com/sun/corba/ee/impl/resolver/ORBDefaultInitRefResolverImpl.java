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

public class ORBDefaultInitRefResolverImpl implements Resolver {
    Operation urlHandler;
    String orbDefaultInitRef;

    public ORBDefaultInitRefResolverImpl(Operation urlHandler, String orbDefaultInitRef) {
        this.urlHandler = urlHandler;

        this.orbDefaultInitRef = orbDefaultInitRef;
    }

    public org.omg.CORBA.Object resolve(String ident) {
        // If the ORBDefaultInitRef is not defined simply return null
        if (orbDefaultInitRef == null) {
            return null;
        }

        String urlString;
        // If the ORBDefaultInitDef is defined as corbaloc: then create the
        // corbaloc String in the format
        // <ORBInitDefaultInitDef Param>/<Identifier>
        // and resolve it using resolveCorbaloc method
        if (orbDefaultInitRef.startsWith("corbaloc:")) {
            urlString = orbDefaultInitRef + "/" + ident;
        } else {
            urlString = orbDefaultInitRef + "#" + ident;
        }

        return (org.omg.CORBA.Object) urlHandler.operate(urlString);
    }

    public Set<String> list() {
        return new java.util.HashSet();
    }
}
