/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.spi.protocol;

import com.sun.corba.ee.spi.resolver.Resolver;

/**
 * InitialServerRequestDispatcher is a specialized version of a ServerRequestDispatcher that provides an initialization
 * method. This delegate is used to implement bootstrapping of initial object references.
 */
public interface InitialServerRequestDispatcher extends ServerRequestDispatcher {
    /**
     * Plug in the resolver that this InitialServerRequestDispatcher should use in order to lookup or list initial name to
     * object reference bindings.
     */
    void init(Resolver resolver);
}
