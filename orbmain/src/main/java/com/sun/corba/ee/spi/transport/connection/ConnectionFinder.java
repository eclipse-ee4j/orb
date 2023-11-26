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
     * 
     * @param cinfo info to match in the search
     * @param idleConnections idle connections to search
     * @param busyConnections busy connections to search
     * @throws IOException if an error occurred
     * @return the best connection
     */
    C find(ContactInfo<C> cinfo, Collection<C> idleConnections, Collection<C> busyConnections) throws IOException;
}
