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
import com.sun.corba.ee.spi.resolver.LocalResolver;
import java.util.Set;
import org.glassfish.pfl.basic.func.NullaryFunction;

public class SplitLocalResolverImpl implements LocalResolver {
    private Resolver resolver;
    private LocalResolver localResolver;

    public SplitLocalResolverImpl(Resolver resolver, LocalResolver localResolver) {
        this.resolver = resolver;
        this.localResolver = localResolver;
    }

    public void register(String name, NullaryFunction<org.omg.CORBA.Object> closure) {
        localResolver.register(name, closure);
    }

    public org.omg.CORBA.Object resolve(String name) {
        return resolver.resolve(name);
    }

    public Set<String> list() {
        return resolver.list();
    }
}
