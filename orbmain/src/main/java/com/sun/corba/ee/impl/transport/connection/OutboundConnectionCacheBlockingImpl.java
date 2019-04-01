/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.transport.connection;

import java.io.IOException;

import java.util.Map;
import java.util.HashMap;

import java.util.concurrent.locks.ReentrantLock;

import com.sun.corba.ee.spi.transport.connection.Connection;
import com.sun.corba.ee.spi.transport.connection.ConnectionFinder;
import com.sun.corba.ee.spi.transport.connection.ContactInfo;
import com.sun.corba.ee.spi.transport.connection.OutboundConnectionCache;

import com.sun.corba.ee.spi.transport.concurrent.ConcurrentQueueFactory;
import com.sun.corba.ee.spi.trace.Transport;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.gmbal.Description;
import org.glassfish.gmbal.ManagedObject;
import org.glassfish.gmbal.ManagedAttribute;
import org.glassfish.pfl.tf.spi.annotation.InfoMethod;

@Transport
@ManagedObject
@Description("Outbound connection cache for connections opened by the client")
public final class OutboundConnectionCacheBlockingImpl<C extends Connection> extends ConnectionCacheBlockingBase<C> implements OutboundConnectionCache<C> {

    private ReentrantLock lock = new ReentrantLock();

    // Configuration data
    // XXX we may want this data to be dynamically re-configurable
    private final int maxParallelConnections; // Maximum number of
                                              // connections we will open
                                              // to the same endpoint

    @ManagedAttribute
    public int maxParallelConnections() {
        return maxParallelConnections;
    }

    private Map<ContactInfo<C>, OutboundCacheEntry<C>> entryMap;

    @ManagedAttribute(id = "cacheEntries")
    private Map<ContactInfo<C>, OutboundCacheEntry<C>> entryMap() {
        return new HashMap<ContactInfo<C>, OutboundCacheEntry<C>>(entryMap);
    }

    private Map<C, OutboundConnectionState<C>> connectionMap;

    @ManagedAttribute(id = "connections")
    private Map<C, OutboundConnectionState<C>> connectionMap() {
        return new HashMap<C, OutboundConnectionState<C>>(connectionMap);
    }

    protected String thisClassName() {
        return "OutboundConnectionCacheBlockingImpl";
    }

    public OutboundConnectionCacheBlockingImpl(final String cacheType, final int highWaterMark, final int numberToReclaim, final int maxParallelConnections,
            final long ttl) {

        super(cacheType, highWaterMark, numberToReclaim, ttl);

        if (maxParallelConnections < 1)
            throw new IllegalArgumentException("maxParallelConnections must be > 0");

        this.maxParallelConnections = maxParallelConnections;

        this.entryMap = new HashMap<ContactInfo<C>, OutboundCacheEntry<C>>();
        this.connectionMap = new HashMap<C, OutboundConnectionState<C>>();
        this.reclaimableConnections = ConcurrentQueueFactory.<C>makeConcurrentQueue(ttl);
    }

    public boolean canCreateNewConnection(ContactInfo<C> cinfo) {
        lock.lock();
        try {
            OutboundCacheEntry<C> entry = entryMap.get(cinfo);
            if (entry == null)
                return true;

            return internalCanCreateNewConnection(entry);
        } finally {
            lock.unlock();
        }
    }

    private boolean internalCanCreateNewConnection(final OutboundCacheEntry<C> entry) {
        lock.lock();
        try {
            final boolean createNewConnection = (entry.totalConnections() == 0)
                    || ((numberOfConnections() < highWaterMark()) && (entry.totalConnections() < maxParallelConnections));

            return createNewConnection;
        } finally {
            lock.unlock();
        }
    }

    public C get(final ContactInfo<C> cinfo) throws IOException {
        return get(cinfo, null);
    }

    @InfoMethod
    private void msg(String m) {
    }

    @InfoMethod
    private void display(String m, Object value) {
    }

    @Transport
    public C get(final ContactInfo<C> cinfo, final ConnectionFinder<C> finder) throws IOException {
        lock.lock();
        C result = null;

        try {
            while (true) {
                final OutboundCacheEntry<C> entry = getEntry(cinfo);

                if (finder != null) {
                    msg("calling finder to get a connection");

                    entry.startConnect();
                    // Finder may block, especially on opening a new
                    // connection, so we can't hold the lock during the
                    // finder call.
                    lock.unlock();
                    try {
                        result = finder.find(cinfo, entry.idleConnectionsView, entry.busyConnectionsView);
                    } finally {
                        lock.lock();
                        entry.finishConnect();
                    }

                    if (result != null) {
                        display("finder got connection", result);
                    }
                }

                if (result == null) {
                    result = entry.idleConnections.poll();
                }
                if (result == null) {
                    result = tryNewConnection(entry, cinfo);
                }
                if (result == null) {
                    result = entry.busyConnections.poll();
                }

                if (result == null) {
                    msg("No connection available: " + "awaiting a pending connection");
                    entry.waitForConnection();
                    continue;
                } else {
                    OutboundConnectionState<C> cs = getConnectionState(cinfo, entry, result);

                    if (cs.isBusy()) {
                        // Nothing to do in this case
                    } else if (cs.isIdle()) {
                        totalBusy++;
                        decrementTotalIdle();
                    } else { // state is NEW
                        totalBusy++;
                    }

                    cs.acquire();
                    break;
                }
            }
        } finally {
            display("totalIdle", totalIdle);
            display("totalBusy", totalBusy);
            lock.unlock();
        }

        return result;
    }

    @Transport
    private OutboundCacheEntry<C> getEntry(final ContactInfo<C> cinfo) throws IOException {

        OutboundCacheEntry<C> result = null;
        // This is the only place a OutboundCacheEntry is constructed.
        result = entryMap.get(cinfo);
        if (result == null) {
            result = new OutboundCacheEntry<C>(lock);
            display("creating new OutboundCacheEntry", result);
            entryMap.put(cinfo, result);
        } else {
            display("re-using existing OutboundCacheEntry", result);
        }

        return result;
    }

    // Note that tryNewConnection will ALWAYS create a new connection if
    // no connection currently exists.
    @Transport
    private C tryNewConnection(final OutboundCacheEntry<C> entry, final ContactInfo<C> cinfo) throws IOException {

        C conn = null;
        if (internalCanCreateNewConnection(entry)) {
            // If this throws an exception just let it
            // propagate: let a higher layer handle a
            // connection creation failure.
            entry.startConnect();
            lock.unlock();
            try {
                conn = cinfo.createConnection();
            } finally {
                lock.lock();
                entry.finishConnect();
            }
        }

        return conn;
    }

    @Transport
    private OutboundConnectionState<C> getConnectionState(ContactInfo<C> cinfo, OutboundCacheEntry<C> entry, C conn) {
        lock.lock();

        try {
            OutboundConnectionState<C> cs = connectionMap.get(conn);
            if (cs == null) {
                cs = new OutboundConnectionState<C>(cinfo, entry, conn);
                display("creating new OutboundConnectionState ", cs);
                connectionMap.put(conn, cs);
            } else {
                display("found OutboundConnectionState ", cs);
            }

            return cs;
        } finally {
            lock.unlock();
        }
    }

    @Transport
    public void release(final C conn, final int numResponsesExpected) {
        lock.lock();
        OutboundConnectionState<C> cs = null;

        try {
            cs = connectionMap.get(conn);
            if (cs == null) {
                msg("connection was already closed");
                return;
            } else {
                int numResp = cs.release(numResponsesExpected);
                display("numResponsesExpected", numResponsesExpected);

                if (!cs.isBusy()) {
                    boolean connectionClosed = false;
                    if (numResp == 0) {
                        connectionClosed = reclaimOrClose(cs, conn);
                    }

                    decrementTotalBusy();

                    if (!connectionClosed) {
                        msg("idle connection queued");
                        totalIdle++;
                    }
                }
            }
        } finally {
            display("cs", cs);
            display("totalIdle", totalIdle);
            display("totalBusy", totalBusy);
            lock.unlock();
        }
    }

    /**
     * Decrement the number of expected responses. When a connection is idle and has no expected responses, it can be
     * reclaimed.
     */
    @Transport
    public void responseReceived(final C conn) {
        lock.lock();
        try {
            final OutboundConnectionState<C> cs = connectionMap.get(conn);
            if (cs == null) {
                msg("response received on closed connection");
                return;
            }

            if (cs.responseReceived()) {
                reclaimOrClose(cs, conn);
            }
        } finally {
            lock.unlock();
        }
    }

    // If overflow, close conn and return true,
    // otherwise enqueue on reclaimable queue and return false.
    @Transport
    private boolean reclaimOrClose(OutboundConnectionState<C> cs, final C conn) {

        final boolean isOverflow = numberOfConnections() > highWaterMark();

        if (isOverflow) {
            msg("closing overflow connection");
            close(conn);
        } else {
            msg("queuing reclaimable connection");
            cs.setReclaimableHandle(reclaimableConnections.offer(conn));
        }

        return isOverflow;
    }

    /**
     * Close a connection, regardless of whether the connection is busy or not.
     */
    @Transport
    public void close(final C conn) {
        lock.lock();
        try {
            final OutboundConnectionState<C> cs = connectionMap.remove(conn);
            if (cs == null) {
                msg("connection was already closed");
                return;
            }
            display("cs", cs);

            if (cs.isBusy()) {
                msg("connection removed from busy connections");
                decrementTotalBusy();
            } else if (cs.isIdle()) {
                msg("connection removed from idle connections");
                decrementTotalIdle();
            }
            try {
                cs.close();
            } catch (IOException ex) {
                // ignore this
            }
        } finally {
            lock.unlock();
        }
    }

    @Transport
    private void decrementTotalIdle() {
        if (totalIdle > 0) {
            totalIdle--;
        } else {
            msg("ERROR: was already 0!");
        }
    }

    @Transport
    private void decrementTotalBusy() {
        if (totalBusy > 0) {
            totalBusy--;
        } else {
            msg("ERROR: count was already 0!");
        }
    }
}

// End of file.
