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
