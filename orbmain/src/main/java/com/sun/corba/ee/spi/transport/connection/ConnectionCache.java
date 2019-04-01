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

/**
 * A connection cache manages a group of connections which may be re-used for sending and receiving messages.
 */
public interface ConnectionCache<C extends Connection> {
    /**
     * User-provided indentifier for an instance of the OutboundConnectionCache.
     */
    String getCacheType();

    /**
     * Total number of connections currently managed by the cache.
     */
    long numberOfConnections();

    /**
     * Number of idle connections; that is, connections for which the number of get/release or
     * responseReceived/responseProcessed calls are equal.
     */
    long numberOfIdleConnections();

    /**
     * Number of non-idle connections. Normally, busy+idle==total, but this may not be strictly true due to concurrent
     * updates to the connection cache.
     */
    long numberOfBusyConnections();

    /**
     * Number of idle connections that are reclaimable. Such connections are not in use, and are not waiting to handle any
     * responses.
     */
    long numberOfReclaimableConnections();

    /**
     * Threshold at which connection reclamation begins.
     */
    int highWaterMark();

    /**
     * Number of connections to reclaim each time reclamation starts.
     */
    int numberToReclaim();

    /**
     * Close a connection, regardless of its state. This may cause requests to fail to be sent, and responses to be lost.
     * Intended for handling serious errors, such as loss of framing on a TCP stream, that require closing the connection.
     */
    void close(final C conn);
}
