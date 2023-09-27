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

/**
 * A connection cache manages a group of connections which may be re-used for sending and receiving messages.
 */
public interface ConnectionCache<C extends Connection> {
    /**
     * User-provided identifier for an instance of the OutboundConnectionCache.
     * 
     * @return identifier String
     */
    String getCacheType();

    /**
     * Total number of connections currently managed by the cache.
     * 
     * @return number of connections
     */
    long numberOfConnections();

    /**
     * Number of idle connections; that is, connections for which the number of get/release or
     * responseReceived/responseProcessed calls are equal.
     * 
     * @return number of idle connections
     */
    long numberOfIdleConnections();

    /**
     * Number of non-idle connections. Normally, busy+idle==total, but this may not be strictly true due to concurrent
     * updates to the connection cache.
     * 
     * @return number of non-idle connections
     */
    long numberOfBusyConnections();

    /**
     * Number of idle connections that are reclaimable. Such connections are not in use, and are not waiting to handle any
     * responses.
     * 
     * @return number of reclaimable idle connections
     */
    long numberOfReclaimableConnections();

    /**
     * Threshold at which connection reclamation begins.
     * 
     * @return threshold
     */
    int highWaterMark();

    /**
     * Number of connections to reclaim each time reclamation starts.
     * 
     * @return number to reclaim
     */
    int numberToReclaim();

    /**
     * Close a connection, regardless of its state. This may cause requests to fail to be sent, and responses to be lost.
     * Intended for handling serious errors, such as loss of framing on a TCP stream, that require closing the connection.
     * 
     * @param conn connection to close
     */
    void close(final C conn);
}
