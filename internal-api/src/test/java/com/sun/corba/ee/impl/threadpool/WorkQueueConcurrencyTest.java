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

package com.sun.corba.ee.impl.threadpool;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import com.sun.corba.ee.spi.threadpool.Work;
import com.sun.corba.ee.spi.threadpool.WorkQueue;

/**
 * The work queue and the pool counters no longer share a lock, so the races
 * that lock used to rule out are exercised here directly.
 */
public class WorkQueueConcurrencyTest {

    /**
     * With a 20 ms inactivity timeout threads time out and end all the time,
     * while several producers add work in bursts. An item left in the queue
     * with no thread to take it would make this time out.
     */
    @Test
    public void no_work_is_stranded_while_threads_come_and_go() throws Exception {
        final int producers = 4;
        final int perProducer = 5_000;
        CountDownLatch done = new CountDownLatch(producers * perProducer);
        AtomicInteger runs = new AtomicInteger();

        try (ThreadPoolImpl pool = new ThreadPoolImpl(0, 8, 20L, "stress")) {
            WorkQueue queue = pool.getAnyWorkQueue();
            Thread[] threads = new Thread[producers];
            for (int p = 0; p < producers; p++) {
                threads[p] = new Thread(() -> {
                    ThreadLocalRandom random = ThreadLocalRandom.current();
                    for (int i = 0; i < perProducer; i++) {
                        queue.addWork(new Task(() -> {
                            runs.incrementAndGet();
                            done.countDown();
                        }));
                        if (random.nextInt(200) == 0) {
                            // Long enough, now and then, for idle threads to time out.
                            sleep(random.nextInt(40));
                        }
                    }
                });
                threads[p].start();
            }
            for (Thread t : threads) {
                t.join();
            }

            assertTrue("every work item must run", done.await(30, TimeUnit.SECONDS));
            assertEquals("and run once", producers * perProducer, runs.get());
            assertEquals(0, ((WorkQueueImpl) queue).workItemsInQueue());
        }
    }

    /**
     * The narrow case, made deterministic: work arrives after the only worker's
     * wait has timed out but before it has decided to end. addWork still counts
     * that worker as waiting and starts no thread, so the worker must see the
     * item and stay.
     */
    @Test
    public void a_worker_that_times_out_as_work_arrives_takes_the_work() throws Exception {
        try (ThreadPoolImpl pool = new ThreadPoolImpl(0, 1, 50L, "narrow")) {
            WorkQueueImpl queue = (WorkQueueImpl) pool.getAnyWorkQueue();
            CountDownLatch first = new CountDownLatch(1);
            CountDownLatch second = new CountDownLatch(1);
            AtomicInteger fired = new AtomicInteger();

            queue.afterTimeoutForTesting = () -> {
                if (fired.getAndIncrement() == 0) {
                    queue.addWork(new Task(second::countDown));
                }
            };
            queue.addWork(new Task(first::countDown));

            assertTrue(first.await(5, TimeUnit.SECONDS));
            assertTrue("the item added in the window must run", second.await(5, TimeUnit.SECONDS));
        }
    }

    @Test
    public void concurrent_producers_never_take_the_pool_past_its_maximum() throws Exception {
        final int max = 4;
        AtomicInteger running = new AtomicInteger();
        AtomicInteger highest = new AtomicInteger();
        CountDownLatch release = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(200);

        try (ThreadPoolImpl pool = new ThreadPoolImpl(0, max, 2000L, "bounded")) {
            WorkQueue queue = pool.getAnyWorkQueue();
            Thread[] producers = new Thread[8];
            for (int p = 0; p < producers.length; p++) {
                producers[p] = new Thread(() -> {
                    for (int i = 0; i < 25; i++) {
                        queue.addWork(new Task(() -> {
                            highest.accumulateAndGet(running.incrementAndGet(), Math::max);
                            await(release);
                            running.decrementAndGet();
                            done.countDown();
                        }));
                    }
                });
                producers[p].start();
            }
            for (Thread t : producers) {
                t.join();
            }
            sleep(500);
            assertTrue("the pool must stay within its maximum", pool.currentNumberOfThreads() <= max);
            assertEquals(max, highest.get());

            release.countDown();
            assertTrue(done.await(30, TimeUnit.SECONDS));
        }
    }

    /**
     * Work items that can only finish together: the pool must grow to run
     * them all at once, as it did when every hand-off took the lock.
     */
    @Test
    public void the_pool_grows_while_every_thread_is_busy() throws Exception {
        final int parties = 6;
        CyclicBarrier together = new CyclicBarrier(parties);
        CountDownLatch done = new CountDownLatch(parties);

        try (ThreadPoolImpl pool = new ThreadPoolImpl(0, parties, 2000L, "grows")) {
            WorkQueue queue = pool.getAnyWorkQueue();
            for (int i = 0; i < parties; i++) {
                queue.addWork(new Task(() -> {
                    try {
                        together.await(10, TimeUnit.SECONDS);
                    } catch (Exception e) {
                        throw new IllegalStateException(e);
                    }
                    done.countDown();
                }));
            }
            assertTrue(done.await(15, TimeUnit.SECONDS));
        }
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static void await(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

    private static final class Task implements Work {
        private final Runnable body;
        private long enqueueTime;

        Task(Runnable body) {
            this.body = body;
        }

        @Override
        public void doWork() {
            body.run();
        }

        @Override
        public void setEnqueueTime(long timeInMillis) {
            enqueueTime = timeInMillis;
        }

        @Override
        public long getEnqueueTime() {
            return enqueueTime;
        }

        @Override
        public String getName() {
            return "task";
        }
    }
}
