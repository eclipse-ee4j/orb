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

package com.sun.corba.ee.impl.util;

import static org.junit.Assert.assertEquals;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.Test;

/**
 * The repository IDs of Java types are computed once per class and then read without a lock. Threads asking for them
 * together, first time included, must all get the same, correct IDs.
 */
public class RepositoryIdJavaTypeTest {

    private static final Class<?>[] TYPES = { ArrayList.class, Date.class, InetAddress.class };
    private static final String[] EXPECTED = {
            "RMI:java.util.ArrayList:F655154F32815380:7881D21D99C7619D",
            "RMI:java.util.Date:AC117E28FE36587A:686A81014B597419",
            "RMI:java.net.InetAddress:C156A93A2ABC4FAF:2D9B57AF9FE3EBDB" };

    @Test
    public void threads_asking_together_get_the_same_ids() throws Exception {
        int threads = 8;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        try {
            CyclicBarrier start = new CyclicBarrier(threads);
            List<Future<String[]>> results = new ArrayList<>();
            for (int t = 0; t < threads; t++) {
                results.add(pool.submit(new Callable<String[]>() {
                    @Override
                    public String[] call() throws Exception {
                        start.await();
                        String[] ids = new String[TYPES.length];
                        for (int i = 0; i < TYPES.length; i++) {
                            ids[i] = RepositoryId.createForJavaType(TYPES[i]);
                        }
                        return ids;
                    }
                }));
            }
            for (Future<String[]> result : results) {
                String[] ids = result.get();
                for (int i = 0; i < TYPES.length; i++) {
                    assertEquals(EXPECTED[i], ids[i]);
                }
            }
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    public void the_id_of_a_value_is_the_id_of_its_class() throws Exception {
        assertEquals(EXPECTED[1], RepositoryId.createForJavaType(new Date()));
        assertEquals(EXPECTED[0], RepositoryId.createForJavaType(new ArrayList<String>()));
    }
}
