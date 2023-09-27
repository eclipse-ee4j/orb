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

import com.sun.corba.ee.impl.transport.connection.OutboundConnectionCacheBlockingImpl;
import com.sun.corba.ee.impl.transport.connection.OutboundConnectionCacheImpl;
import com.sun.corba.ee.impl.transport.connection.InboundConnectionCacheBlockingImpl;
import com.sun.corba.ee.impl.transport.connection.InboundConnectionCacheImpl;

/**
 * A factory class for creating connections caches. Note that a rather unusual syntax is needed for calling these
 * methods:
 *
 * {@code ConnectionCacheFactory.<V>makeXXXCache()}
 *
 * This is required because the type variable V is not used in the parameters of the factory method (there are no
 * parameters).
 */
public final class ConnectionCacheFactory {
    private ConnectionCacheFactory() {
    }

    public static <C extends Connection> OutboundConnectionCache<C> makeBlockingOutboundConnectionCache(String cacheType, int highWaterMark,
            int numberToReclaim, int maxParallelConnections, int ttl) {

        return new OutboundConnectionCacheBlockingImpl<C>(cacheType, highWaterMark, numberToReclaim, maxParallelConnections, ttl);
    }

    public static <C extends Connection> OutboundConnectionCache<C> makeNonBlockingOutboundConnectionCache(String cacheType,
            int highWaterMark, int numberToReclaim, int maxParallelConnections, int ttl) {

        return new OutboundConnectionCacheImpl<C>(cacheType, highWaterMark, numberToReclaim, maxParallelConnections, ttl);
    }

    public static <C extends Connection> InboundConnectionCache<C> makeBlockingInboundConnectionCache(String cacheType, int highWaterMark,
            int numberToReclaim, int ttl) {
        return new InboundConnectionCacheBlockingImpl<C>(cacheType, highWaterMark, numberToReclaim, ttl);
    }

    public static <C extends Connection> InboundConnectionCache<C> makeNonBlockingInboundConnectionCache(String cacheType,
            int highWaterMark, int numberToReclaim, int ttl) {
        return new InboundConnectionCacheImpl<C>(cacheType, highWaterMark, numberToReclaim, ttl);
    }
}
