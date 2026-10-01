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

package com.sun.corba.ee.impl.threadpool;

import com.sun.corba.ee.spi.threadpool.ThreadPool;
import com.sun.corba.ee.spi.threadpool.Work;
import com.sun.corba.ee.spi.threadpool.WorkQueue;

import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;

import org.glassfish.gmbal.Description;
import org.glassfish.gmbal.ManagedAttribute;
import org.glassfish.gmbal.NameValue;

/**
 * The queue between the threads that accept work - the selector, a
 * connection's reader - and the pool's worker threads.
 *
 * <p>Every request the ORB dispatches passes through here, usually twice.
 * This used to be a LinkedList guarded by the queue's monitor, with
 * wait/notify for the hand-off, and the pool's thread counters were guarded
 * by the same monitor; under load the producers and every idle worker
 * contended for it, and a profile of small remote calls put some sixteen
 * percent of server CPU time in that monitor. It is now a
 * LinkedTransferQueue - lock free, and a worker that finds it empty spins
 * briefly before parking - with atomic counters, which is also the rewrite
 * the old requestWork asked for (issue 7722).
 *
 * <p>Measured on GitHub Actions against the monitor, with the rest of this
 * branch in place: small remote calls 9 percent faster, a 50 node object
 * graph 18 percent, and with 64 KB fragments 11 and 49 percent.
 *
 * <p>This queue hands an item to a parked worker when there is one, rather
 * than leaving it for the next worker to come back for. That is why the
 * change belongs with processing the next fragment of a message on the
 * thread that processed the previous one: while every fragment went through
 * here, and each one was waited for by the thread assembling the message,
 * handing them to parked threads cost 21 percent of throughput on 64 KB
 * messages in 1 KB fragments, and the monitor's barging was worth more than
 * its contention. With the fragments off this path that is 2 percent, and
 * everything else is faster.
 *
 * <p>Waking parked workers by hand was tried instead - a ConcurrentLinkedQueue
 * with the idle workers on a Treiber stack, woken as ForkJoinPool does it -
 * and measured slower than both this and the monitor, on every scenario. The
 * counters it had to keep, and the node each wait allocates, cost more than
 * the wake-ups they save.
 *
 * <p>The pool's policy is unchanged: a worker thread is added when a work
 * item arrives and fewer threads are waiting than there are items queued,
 * up to the maximum; a worker that waits a whole inactivity timeout for
 * nothing ends, unless that would leave no more idle threads than the
 * minimum.
 *
 * <p>The monitor also made one race impossible, and without it the order of
 * operations has to. A worker whose wait times out must not end while an
 * item it could have taken sits in the queue, or that item waits for some
 * other thread to finish. So a timed-out worker first stops counting itself
 * as waiting - by winning a compare-and-set that keeps the idle threads
 * above the minimum - and then looks at the queue; addWork first counts the
 * item and then looks at the waiting threads. With both steps on atomic variables at
 * least one side sees the other: either the worker finds the item and stays,
 * or addWork finds no waiting thread and starts one.
 */
public class WorkQueueImpl implements WorkQueue {
    public static final String WORKQUEUE_DEFAULT_NAME = "default-workqueue";

    final private LinkedTransferQueue<Work> queue = new LinkedTransferQueue<>();

    // LinkedTransferQueue.size() walks the queue; the pool policy needs the
    // count on every addWork.
    final private AtomicInteger queued = new AtomicInteger();

    private volatile ThreadPool workerThreadPool;
    final private LongAdder workItemsAdded = new LongAdder();
    final private LongAdder workItemsDequeued = new LongAdder();
    final private LongAdder totalTimeInQueue = new LongAdder();

    // Name of the work queue
    final private String name;

    // Test seam: run by a worker whose wait has just timed out, before it
    // decides whether to end. Only the timeout path reads it.
    volatile Runnable afterTimeoutForTesting;

    public WorkQueueImpl() {
        this.name = WORKQUEUE_DEFAULT_NAME;
    }

    public WorkQueueImpl(ThreadPool workerThreadPool) {
        this(workerThreadPool, WORKQUEUE_DEFAULT_NAME);
    }

    public WorkQueueImpl(ThreadPool workerThreadPool, String name) {
        this.workerThreadPool = workerThreadPool;
        this.name = name;
    }

    public void addWork(Work work) {
        workItemsAdded.increment();
        work.setEnqueueTime(System.currentTimeMillis());

        queue.offer(work);
        int waitingForWork = queued.incrementAndGet();

        ThreadPool pool = workerThreadPool;
        if (pool.numberOfAvailableThreads() < waitingForWork) {
            // NOTE: It is possible that the Work that was just added may unblock
            //       Worker Threads waiting on the Work just added and all Worker
            //       Threads are busy, (blocked & waiting for a response). This
            //       situation can lead to a deadlock.  The solution to such a
            //       a problem should it occur is to increase the maximum number
            //       of threads.
            ((ThreadPoolImpl) pool).createWorkerThreadIfBelowMaximum();
        }
    }

    /**
     * Waits up to waitTime milliseconds for a work item.
     *
     * @return the work item, or null when the wait timed out and this thread
     *         should wait again
     * @throws WorkerThreadNotNeededException when the wait timed out and the
     *         pool has idle threads to spare; the thread must end
     */
    Work requestWork(long waitTime) throws WorkerThreadNotNeededException,
        InterruptedException {

        ThreadPoolImpl pool = (ThreadPoolImpl) workerThreadPool;
        pool.incrementNumberOfAvailableThreads();
        Work work;
        try {
            work = queue.poll(waitTime, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            pool.decrementNumberOfAvailableThreads();
            throw e;
        }

        if (work != null) {
            pool.decrementNumberOfAvailableThreads();
            queued.decrementAndGet();
            workItemsDequeued.increment();
            totalTimeInQueue.add(System.currentTimeMillis() - work.getEnqueueTime());
            return work;
        }

        Runnable hook = afterTimeoutForTesting;
        if (hook != null) {
            hook.run();
        }

        // Timed out, and still counted as available while deciding whether
        // to end: two threads timing out together must not both conclude
        // that the other one keeps the pool above its minimum. Ending takes
        // a successful compare-and-set on the available count, which only
        // one of them can win while the count is just above the minimum.
        if (pool.tryRetireAvailableThread()) {
            if (queued.get() == 0) {
                // This thread has timed out and can die because
                // we have enough available idle threads.
                // NOTE: It is expected that the WorkerThread calling this
                //       method will gracefully exit as a result of
                //       catching the WorkerThreadNotNeededException.
                pool.decrementCurrentNumberOfThreads();
                throw new WorkerThreadNotNeededException();
            }
            // Work arrived as the wait ran out; see the class comment. The
            // retirement already took this thread off the available count.
            return null;
        }

        pool.decrementNumberOfAvailableThreads();
        return null;
    }

    public void setThreadPool(ThreadPool workerThreadPool) {
        this.workerThreadPool = workerThreadPool;
    }

    public ThreadPool getThreadPool() {
        return workerThreadPool;
    }

    /**
     * Returns the total number of Work items added to the Queue.
     */
    @ManagedAttribute
    @Description("Total number of items added to the queue")
    public long totalWorkItemsAdded() {
        return workItemsAdded.sum();
    }

    /**
     * Returns the total number of Work items in the Queue to be processed.
     */
    @ManagedAttribute
    @Description("Total number of items in the queue to be processed")
    public int workItemsInQueue() {
        return queued.get();
    }

    /**
     * Returns the average amount Work items have spent in the Queue waiting
     * to be processed.
     */
    @ManagedAttribute
    @Description("Average time work items spend waiting in the queue in milliseconds")
    public long averageTimeInQueue() {
        long dequeued = workItemsDequeued.sum();
        if (dequeued == 0) {
            return 0;
        } else {
            return (totalTimeInQueue.sum()/dequeued);
        }
    }

    @NameValue
    public String getName() {
        return name;
    }
}

// End of file.
