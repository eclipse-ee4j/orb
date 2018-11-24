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

import java.util.Set;
import java.util.HashSet;

import com.sun.corba.ee.spi.resolver.Resolver;

public class CompositeResolverImpl implements Resolver {
    private Resolver first;
    private Resolver second;

    public CompositeResolverImpl(Resolver first, Resolver second) {
        this.first = first;
        this.second = second;
    }

    public org.omg.CORBA.Object resolve(String name) {
        org.omg.CORBA.Object result = first.resolve(name);
        if (result == null)
            result = second.resolve(name);
        return result;
    }

    public Set<String> list() {
        Set<String> result = new HashSet();
        result.addAll(first.list());
        result.addAll(second.list());
        return result;
    }
}
