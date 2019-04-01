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

import java.io.IOException;

/**
 * A concurrent mostly non-blocking connection cache. Here a Connection is an abstraction of a Socket or SocketChannel:
 * basically some sort of resource that is expensive to acquire, and can be re-used freely. The cache maintains a loose
 * upper bound on the number of cached connections, and reclaims connections as needed.
 * <P>
 * This cache places minimal requirements on the Connections that it contains:
 * <ol>
 * <li>A Connection must implement a close() method. This is called when idle connections are reclaimed.
 * <li>A Connection must be usable as a HashMap key.
 * <li>There must be a ContactInfo class that is used to create Connection instances. The ContactInfo class must support
 * a create() method that returns a Connection.
 * <li>The ContactInfo must be usable as a HashMap key.
 * <li>All instances created from a ContactInfo are equal; that is, any request sent to a particular ContactInfo can
 * used an instance created from that ContactInfo. For example, in the CORBA case, IP host and port is not always
 * sufficient: we may also need the Codeset type that indicates how Strings are encoded. Basically, protocols (like
 * GIOP) that bind session state to a Connection may need more than transport information in the ContactInfo.
 * </ol>
 * <P>
 * Some simple methods are provided for monitoring the state of the cache: numbers of busy and idle connections, and the
 * total number of connections in the cache.
 */
public interface OutboundConnectionCache<C extends Connection> extends ConnectionCache<C> {
    /**
     * Configured maximum number of connections supported per ContactInfo.
     */
    int maxParallelConnections();

    /**
     * Determine whether a new connection could be created by the ConnectionCache or not.
     */
    boolean canCreateNewConnection(ContactInfo<C> cinfo);

    /**
     * Return a Connection corresponding to the given ContactInfo. This works as follows:
     * <ul>
     * <li>Call the finder. If it returns non-null, use that connection; (Note that this may be a new connection, created in
     * the finder). The finder SHOULD NOT create a new connection if canCreateNewConnection returns false, but this is
     * advisory.
     * <li>otherwise, Use an idle connection, if one is available;
     * <li>otherwise, create a new connection, if not too many connections are open;
     * <li>otherwise, use a busy connection.
     * </ul>
     * Note that creating a new connection requires EITHER:
     * <ul>
     * <li>there is no existing connection for the ContactInfo
     * <li>OR the total number of connections in the cache is less than the HighWaterMark and the number of connections for
     * this ContactInfo is less than MaxParallelConnections.
     * </ul>
     * We will always return a Connection for a get call UNLESS we have no existing connection and an attempt to create a
     * new connection fails. In this case, the IOException thrown by ContactInfo.create is propagated out of this method.
     * <P>
     * It is possible that the cache contains connections that no longer connect to their destination. In this case, it is
     * the responsibility of the client of the cache to close the broken connection as they are detected. Connection
     * reclamation may also handle the cleanup, but note that a broken connection with pending responses will never be
     * reclaimed.
     * <P>
     * Note that the idle and busy connection collections that are passed to the finder are unmodifiable collections. They
     * have iterators that return connections in LRU order, with the least recently used connection first. This is done to
     * aid a finder that wishes to consider load balancing in its determination of an appropriate connection.
     * <P>
     */
    C get(ContactInfo<C> cinfo, ConnectionFinder<C> finder) throws IOException;

    /**
     * Behaves the same as get( ContactInfo<C>, ConnectionFinder<C> ) except that no connection finder is provided, so that
     * step is ignored.
     */
    C get(ContactInfo<C> cinfo) throws IOException;

    /**
     * Release a Connection previously obtained from get. Connections that have been released as many times as they have
     * been returned by get are idle; otherwise a Connection is busy. Some number of responses (usually 0 or 1) may be
     * expected ON THE SAME CONNECTION even for an idle connection. We maintain a count of the number of outstanding
     * responses we expect for protocols that return the response on the same connection on which the request was received.
     * This is necessary to prevent reclamation of a Connection that is idle, but still needed to send responses to old
     * requests.
     */
    void release(C conn, int numResponseExpected);

    /**
     * Inform the cache that a response has been received on a particular connection. This must also be called in the event
     * that no response is received, but the client times out waiting for a response, and decides to abandon the request.
     * <P>
     * When a Connection is idle, and has no pending responses, it is eligible for reclamation.
     */
    void responseReceived(C conn);
}
