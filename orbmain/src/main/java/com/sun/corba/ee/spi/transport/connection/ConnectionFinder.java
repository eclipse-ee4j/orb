/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.spi.transport.connection;

import java.util.Collection;

import java.io.IOException;

/**
 * An instance of a ConnectionFinder may be supplied to the OutboundConnectionCache.get method.
 */
public interface ConnectionFinder<C extends Connection> {
    /**
     * Method that searches idleConnections and busyConnections for the best connection. May return null if no best
     * connection exists. May create a new connection and return it.
     */
    C find(ContactInfo<C> cinfo, Collection<C> idleConnections, Collection<C> busyConnections) throws IOException;
}
