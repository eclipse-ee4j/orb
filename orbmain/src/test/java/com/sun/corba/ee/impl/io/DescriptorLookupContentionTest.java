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

package com.sun.corba.ee.impl.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.sun.corba.ee.impl.util.RepositoryId;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Test;

/**
 * Asserts that looking up a class descriptor no longer serializes every
 * thread in the process behind one monitor.
 *
 * <p>Throughput would be the obvious way to show this and the wrong way to
 * assert it: a timing threshold is a flaky test on a shared build machine.
 * What is checked instead is the property itself - that the fast path does
 * not need the lock - by holding that lock and watching a lookup succeed
 * anyway. Before the change the same test blocks until the timeout.
 */
public class DescriptorLookupContentionTest {

    private static final long TIMEOUT_SECONDS = 10;

    /** A class of our own, so no other test can have warmed it up first. */
    private static final class Marshalled implements Serializable {

        private static final long serialVersionUID = 1L;

        @SuppressWarnings("unused")
        private int field;
    }

    private static Object descriptorCache() throws Exception {
        Field field = ObjectStreamClass.class.getDeclaredField("descriptorFor");
        field.setAccessible(true);
        return field.get(null);
    }

    @Test
    public void aLookupOfAnInitializedDescriptorDoesNotNeedTheGlobalLock() throws Exception {
        // Warm up: after this the descriptor is cached and initialized, which
        // is the state every class reaches within moments of a server starting.
        ObjectStreamClass warmed = ObjectStreamClass.lookup(Marshalled.class);
        assertNotNull(warmed);

        final Object cache = descriptorCache();
        final CountDownLatch lockHeld = new CountDownLatch(1);
        final CountDownLatch lookupDone = new CountDownLatch(1);
        final AtomicReference<ObjectStreamClass> found = new AtomicReference<>();

        Thread reader = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    lockHeld.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
                    found.set(ObjectStreamClass.lookup(Marshalled.class));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    lookupDone.countDown();
                }
            }
        }, "descriptor-lookup");
        reader.setDaemon(true);
        reader.start();

        synchronized (cache) {
            lockHeld.countDown();
            assertTrue("a lookup of an already initialized descriptor blocked on the"
                            + " cache monitor; the lock free fast path is not being taken",
                    lookupDone.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
        }

        reader.join(TimeUnit.SECONDS.toMillis(TIMEOUT_SECONDS));
        assertSame("the fast path must return the same descriptor as the slow path",
                warmed, found.get());
    }

    @Test
    public void concurrentLookupsAgreeOnOneDescriptorPerClass() throws Exception {
        final Class<?>[] classes = {
            Marshalled.class, String.class, Integer.class, java.util.ArrayList.class,
            java.util.HashMap.class, java.math.BigDecimal.class, java.util.Date.class,
        };

        int threads = Math.max(4, Runtime.getRuntime().availableProcessors());
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        try {
            List<Callable<ObjectStreamClass[]>> tasks = new ArrayList<>();
            for (int t = 0; t < threads; t++) {
                tasks.add(new Callable<ObjectStreamClass[]>() {
                    @Override
                    public ObjectStreamClass[] call() {
                        ObjectStreamClass[] seen = new ObjectStreamClass[classes.length];
                        for (int round = 0; round < 200; round++) {
                            for (int i = 0; i < classes.length; i++) {
                                seen[i] = ObjectStreamClass.lookup(classes[i]);
                            }
                        }
                        return seen;
                    }
                });
            }

            ObjectStreamClass[] reference = null;
            for (Future<ObjectStreamClass[]> result : pool.invokeAll(tasks, 60, TimeUnit.SECONDS)) {
                ObjectStreamClass[] seen = result.get();
                if (reference == null) {
                    reference = seen;
                } else {
                    for (int i = 0; i < seen.length; i++) {
                        assertSame("two threads got different descriptors for " + classes[i],
                                reference[i], seen[i]);
                    }
                }
            }
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    public void concurrentRepositoryIdLookupsIntern() throws Exception {
        final String[] ids = new String[64];
        for (int i = 0; i < ids.length; i++) {
            ids[i] = "RMI:com.acme.Type" + i + ":0123456789ABCDEF";
        }

        int threads = Math.max(4, Runtime.getRuntime().availableProcessors());
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        try {
            List<Callable<RepositoryId[]>> tasks = new ArrayList<>();
            for (int t = 0; t < threads; t++) {
                tasks.add(new Callable<RepositoryId[]>() {
                    @Override
                    public RepositoryId[] call() {
                        RepositoryId[] seen = new RepositoryId[ids.length];
                        for (int round = 0; round < 200; round++) {
                            for (int i = 0; i < ids.length; i++) {
                                seen[i] = RepositoryId.cache.getId(ids[i]);
                            }
                        }
                        return seen;
                    }
                });
            }

            RepositoryId[] reference = null;
            for (Future<RepositoryId[]> result : pool.invokeAll(tasks, 60, TimeUnit.SECONDS)) {
                RepositoryId[] seen = result.get();
                assertEquals(ids.length, seen.length);
                if (reference == null) {
                    reference = seen;
                } else {
                    for (int i = 0; i < seen.length; i++) {
                        assertSame("the cache handed out two instances for " + ids[i],
                                reference[i], seen[i]);
                    }
                }
            }
        } finally {
            pool.shutdownNow();
        }
    }
}
