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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Measures how descriptor lookup scales with thread count.
 *
 * <p>Deliberately a {@code main} and not a test: the number it reports is the
 * point, and a number is not something to assert on a shared build machine.
 * The correctness properties are asserted in
 * {@link DescriptorLookupContentionTest}; this is here so that a claim about
 * throughput in a review can be reproduced rather than believed.
 *
 * <pre>
 * mvn -pl orbmain test-compile
 * java -cp orbmain/target/classes:orbmain/target/test-classes:$(deps) \
 *      com.sun.corba.ee.impl.io.DescriptorLookupThroughput
 * </pre>
 *
 * <p>What to look for is not the absolute rate but the shape: with a global
 * monitor on the lookup path the total rate is flat as threads are added,
 * because the threads are queueing. Without it the rate should climb roughly
 * with core count until memory bandwidth or the allocator becomes the limit.
 */
public final class DescriptorLookupThroughput {

    /** Iterations per thread, each doing one pass over CLASSES. */
    private static final int ROUNDS = 2_000_000;
    private static final int WARMUP_ROUNDS = 200_000;

    private DescriptorLookupThroughput() {
    }

    private static final class Sample implements Serializable {

        private static final long serialVersionUID = 1L;

        @SuppressWarnings("unused")
        private int a;
        @SuppressWarnings("unused")
        private String b;
    }

    private static final Class<?>[] CLASSES = {
        Sample.class, String.class, Integer.class, Long.class,
        java.util.ArrayList.class, java.util.HashMap.class,
        java.math.BigDecimal.class, java.util.Date.class,
    };

    public static void main(String[] args) throws Exception {
        for (Class<?> cl : CLASSES) {
            ObjectStreamClass.lookup(cl);
        }
        run(1, WARMUP_ROUNDS);
        run(2, WARMUP_ROUNDS);

        int cores = Runtime.getRuntime().availableProcessors();
        System.out.println("cores: " + cores);
        System.out.printf("%8s %16s %12s%n", "threads", "lookups/sec", "vs 1 thread");

        double single = 0;
        for (int threads = 1; threads <= cores * 2; threads *= 2) {
            // Best of three: the interesting quantity is the ceiling, and a
            // shared machine only ever adds noise downwards.
            double best = 0;
            for (int attempt = 0; attempt < 3; attempt++) {
                best = Math.max(best, run(threads, ROUNDS));
            }
            if (threads == 1) {
                single = best;
            }
            System.out.printf("%8d %16.0f %11.2fx%n", threads, best, best / single);
        }
    }

    /**
     * Runs a fixed amount of work per thread and times the whole thing once.
     *
     * <p>A deadline checked inside the loop would be the obvious shape and is
     * wrong here: System.nanoTime is not necessarily a cheap userspace read,
     * and when it is not, every thread queues on the same clock source. That
     * turns the harness itself into the contended resource and reports the
     * same flat curve whatever the code under test does.
     *
     * @param threads number of threads to run
     * @param rounds passes over CLASSES per thread
     * @return lookups per second across all threads
     */
    private static double run(int threads, final int rounds) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        try {
            final java.util.concurrent.CyclicBarrier start =
                new java.util.concurrent.CyclicBarrier(threads);
            List<Callable<Long>> tasks = new ArrayList<>();
            for (int t = 0; t < threads; t++) {
                tasks.add(new Callable<Long>() {
                    @Override
                    public Long call() throws Exception {
                        start.await();
                        long seen = 0;
                        for (int round = 0; round < rounds; round++) {
                            for (int i = 0; i < CLASSES.length; i++) {
                                if (ObjectStreamClass.lookup(CLASSES[i]) != null) {
                                    seen++;
                                }
                            }
                        }
                        return seen;
                    }
                });
            }

            long began = System.nanoTime();
            long total = 0;
            for (Future<Long> result : pool.invokeAll(tasks)) {
                total += result.get();
            }
            long elapsed = System.nanoTime() - began;
            return total / (elapsed / 1_000_000_000.0);
        } finally {
            pool.shutdownNow();
        }
    }
}
