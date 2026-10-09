/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
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

package com.sun.corba.ee.impl.transport;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.sun.corba.ee.spi.transport.Connection;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

/**
 * The write lock is a compare-and-set when nobody holds it and a wait on writeEvent when somebody does. Either way it
 * must let one writer in at a time, and a writer that waits must get in once the holder lets go.
 */
public class ConnectionWriteLockTest extends TransportTestBase {

    private int unguarded; // incremented only while holding the write lock

    @Test
    public void one_writer_at_a_time_under_contention() throws Exception {
        useNio();
        ConnectionImpl connection = getConnection();
        int threads = 8;
        int rounds = 20_000;
        AtomicInteger inside = new AtomicInteger();
        AtomicInteger overlaps = new AtomicInteger();
        CyclicBarrier start = new CyclicBarrier(threads);
        List<Thread> writers = new ArrayList<>();
        List<Throwable> failures = new ArrayList<>();
        for (int t = 0; t < threads; t++) {
            Thread writer = new Thread(() -> {
                try {
                    start.await();
                    for (int i = 0; i < rounds; i++) {
                        connection.writeLock();
                        try {
                            if (inside.incrementAndGet() != 1) {
                                overlaps.incrementAndGet();
                            }
                            unguarded++;
                            inside.decrementAndGet();
                        } finally {
                            connection.writeUnlock();
                        }
                    }
                } catch (Throwable e) {
                    synchronized (failures) {
                        failures.add(e);
                    }
                }
            });
            writers.add(writer);
            writer.start();
        }
        for (Thread writer : writers) {
            writer.join(TimeUnit.MINUTES.toMillis(2));
            assertTrue("a writer is stuck", !writer.isAlive());
        }
        assertTrue(failures.toString(), failures.isEmpty());
        assertEquals(0, overlaps.get());
        assertEquals(threads * rounds, unguarded);
    }

    @Test
    public void a_waiting_writer_gets_in_when_the_holder_lets_go() throws Exception {
        useNio();
        ConnectionImpl connection = getConnection();
        connection.writeLock();
        CountDownLatch waiting = new CountDownLatch(1);
        CountDownLatch acquired = new CountDownLatch(1);
        Thread writer = new Thread(() -> {
            waiting.countDown();
            connection.writeLock();
            acquired.countDown();
            connection.writeUnlock();
        });
        writer.start();
        waiting.await();
        // Not while the lock is held.
        assertEquals(false, acquired.await(200, TimeUnit.MILLISECONDS));
        connection.writeUnlock();
        assertTrue(acquired.await(10, TimeUnit.SECONDS));
        writer.join();
    }

    @Test(expected = org.omg.CORBA.SystemException.class)
    public void an_aborted_connection_refuses_the_lock() throws IOException {
        useNio();
        getConnection().setState(Connection.ABORT);
        getConnection().writeLock();
    }
}
