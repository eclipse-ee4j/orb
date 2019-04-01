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

import java.util.Queue;
import java.util.Collection;
import java.util.Collections;

import java.util.concurrent.LinkedBlockingQueue;

import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;

import org.glassfish.gmbal.ManagedData;
import org.glassfish.gmbal.ManagedAttribute;
import org.glassfish.gmbal.Description;

import com.sun.corba.ee.spi.transport.connection.Connection;
import java.util.ArrayList;

// Represents an entry in the outbound connection cache.
// This version handles normal shareable ContactInfo
// (we also need to handle no share).
@ManagedData
public class OutboundCacheEntry<C extends Connection> {
    private ReentrantLock lock;
    private final Condition waitForPendingConnections;

    public OutboundCacheEntry(ReentrantLock lock) {
        this.lock = lock;
        waitForPendingConnections = lock.newCondition();
    }

    final Queue<C> idleConnections = new LinkedBlockingQueue<C>();
    final Collection<C> idleConnectionsView = Collections.unmodifiableCollection(idleConnections);

    final Queue<C> busyConnections = new LinkedBlockingQueue<C>();
    final Collection<C> busyConnectionsView = Collections.unmodifiableCollection(busyConnections);

    private int pendingConnections = 0;

    @Override
    public String toString() {
        lock.lock();
        try {
            return "OutboundCacheEntry[numIdle=" + idleConnections.size() + ",numBusy=" + busyConnections.size() + ",numPending=" + pendingConnections + "]";
        } finally {
            lock.unlock();
        }
    }

    @ManagedAttribute
    @Description("list of idle connections")
    private Collection<C> idleConnections() {
        lock.lock();
        try {
            return new ArrayList<C>(idleConnections);
        } finally {
            lock.unlock();
        }
    }

    @ManagedAttribute
    @Description("list of idle connections")
    private Collection<C> busyConnections() {
        lock.lock();
        try {
            return new ArrayList<C>(busyConnections);
        } finally {
            lock.unlock();
        }
    }

    @ManagedAttribute(id = "numIdleConnections")
    @Description("Number of idle connections")
    private int numIdleConnectionsAttribute() {
        lock.lock();
        try {
            return idleConnections.size();
        } finally {
            lock.unlock();
        }
    }

    @ManagedAttribute(id = "numPendingConnections")
    @Description("Number of pending connections")
    private int numPendingConnectionsAttribute() {
        lock.lock();
        try {
            return pendingConnections;
        } finally {
            lock.unlock();
        }
    }

    @ManagedAttribute(id = "numBusyConnections")
    @Description("Number of busy connections")
    private int numBusyConnectionsAttribute() {
        lock.lock();
        try {
            return busyConnections.size();
        } finally {
            lock.unlock();
        }
    }

    public int totalConnections() {
        return idleConnections.size() + busyConnections.size() + pendingConnections;
    }

    public void startConnect() {
        pendingConnections++;
    }

    public void finishConnect() {
        pendingConnections--;
        waitForPendingConnections.signal();
    }

    public void waitForConnection() {
        waitForPendingConnections.awaitUninterruptibly();
    }
}
