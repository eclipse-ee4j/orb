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

import com.sun.corba.ee.spi.transport.connection.Connection;
import com.sun.corba.ee.spi.transport.connection.ConnectionCache;

import com.sun.corba.ee.spi.transport.concurrent.ConcurrentQueue;
import com.sun.corba.ee.spi.transport.concurrent.ConcurrentQueue.Handle;
import com.sun.corba.ee.spi.trace.Transport;
import org.glassfish.pfl.tf.spi.annotation.InfoMethod;

@Transport
public abstract class ConnectionCacheBase<C extends Connection> implements ConnectionCache<C> {

    private boolean flag;

    // A name for this instance, provided for convenience.
    private final String cacheType;

    // Configuration data
    // XXX we may want this data to be dynamically re-configurable
    private final int highWaterMark; // Maximum number of
                                     // connections before we start
                                     // closing idle connections
    private final int numberToReclaim; // How many connections to
                                       // reclaim at once

    // MUST be initialized in a subclass
    protected ConcurrentQueue<C> reclaimableConnections = null;

    public final String getCacheType() {
        return cacheType;
    }

    public final int numberToReclaim() {
        return numberToReclaim;
    }

    public final int highWaterMark() {
        return highWaterMark;
    }

    // The name of this class, which is implemented in the subclass.
    // I could derive this from this.getClass().getClassName(), but
    // this is easier.
    protected abstract String thisClassName();

    ConnectionCacheBase(final String cacheType, final int highWaterMark, final int numberToReclaim) {

        if (cacheType == null)
            throw new IllegalArgumentException("cacheType must not be null");

        if (highWaterMark < 0)
            throw new IllegalArgumentException("highWaterMark must be non-negative");

        if (numberToReclaim < 1)
            throw new IllegalArgumentException("numberToReclaim must be at least 1");

        this.cacheType = cacheType;
        this.highWaterMark = highWaterMark;
        this.numberToReclaim = numberToReclaim;
    }

    @Override
    public String toString() {
        return thisClassName() + "[" + getCacheType() + "]";
    }

    @InfoMethod
    private void display(String msg, Object value) {
    }

    /**
     * Reclaim some idle cached connections. Will never close a connection that is busy.
     */
    @Transport
    protected boolean reclaim() {
        int ctr = 0;
        while (ctr < numberToReclaim()) {
            Handle<C> candidate = reclaimableConnections.poll();
            if (candidate == null)
                // If we have closed all idle connections, we must stop
                // reclaiming.
                break;

            try {
                display("closing connection", candidate);
                close(candidate.value());
            } catch (RuntimeException exc) {
                display("exception on close", exc);
                throw exc;
            }

            ctr++;
        }

        display("number of connections reclaimed", ctr);
        return ctr > 0;
    }
}
