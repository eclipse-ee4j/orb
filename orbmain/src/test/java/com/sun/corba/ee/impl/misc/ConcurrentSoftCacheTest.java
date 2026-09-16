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

package com.sun.corba.ee.impl.misc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.Test;

public class ConcurrentSoftCacheTest {

    @Test
    public void storesAndReturnsValues() {
        ConcurrentSoftCache<String, String> cache = new ConcurrentSoftCache<>();
        assertNull(cache.get("absent"));

        String value = "a value";
        cache.put("key", value);
        assertSame(value, cache.get("key"));
        assertEquals(1, cache.size());
    }

    @Test
    public void aLaterPutReplacesTheEarlierOne() {
        ConcurrentSoftCache<String, String> cache = new ConcurrentSoftCache<>();
        cache.put("key", "first");
        cache.put("key", "second");

        assertEquals("second", cache.get("key"));
        assertEquals(1, cache.size());
    }

    @Test
    public void purgeLeavesLiveEntriesAlone() {
        ConcurrentSoftCache<String, String> cache = new ConcurrentSoftCache<>();
        String held = "still referenced";
        cache.put("key", held);

        cache.purge();

        assertSame(held, cache.get("key"));
    }

    /**
     * The cache is read with no lock while other threads write it, which is
     * exactly what its predecessor could not survive.
     */
    @Test
    public void concurrentReadersAndWritersAgree() throws Exception {
        final ConcurrentSoftCache<Integer, String> cache = new ConcurrentSoftCache<>();
        final int keys = 200;
        final List<String> values = new ArrayList<>();
        for (int i = 0; i < keys; i++) {
            values.add("value " + i);
        }

        int threads = Math.max(4, Runtime.getRuntime().availableProcessors());
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        try {
            List<Callable<Boolean>> tasks = new ArrayList<>();
            for (int t = 0; t < threads; t++) {
                tasks.add(new Callable<Boolean>() {
                    @Override
                    public Boolean call() {
                        for (int round = 0; round < 500; round++) {
                            for (int i = 0; i < keys; i++) {
                                cache.put(i, values.get(i));
                                String seen = cache.get(i);
                                // Never a value belonging to another key.
                                if (seen != null && seen != values.get(i)) {
                                    return false;
                                }
                            }
                        }
                        return true;
                    }
                });
            }
            for (Future<Boolean> result : pool.invokeAll(tasks, 60, TimeUnit.SECONDS)) {
                assertTrue("a reader saw a value that did not belong to its key", result.get());
            }
            assertEquals(keys, cache.size());
        } finally {
            pool.shutdownNow();
        }
    }
}
