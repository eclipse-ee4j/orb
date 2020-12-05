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

import java.util.LinkedList;
import java.util.Queue;

import com.sun.corba.ee.spi.threadpool.ThreadPool;
import com.sun.corba.ee.spi.threadpool.Work;
import com.sun.corba.ee.spi.threadpool.WorkQueue;

import org.glassfish.gmbal.Description ;
import org.glassfish.gmbal.ManagedAttribute ;
import org.glassfish.gmbal.NameValue ;

public class WorkQueueImpl implements WorkQueue
{
    public static final String WORKQUEUE_DEFAULT_NAME = "default-workqueue";

    final private Queue<Work> queue;
    private ThreadPool workerThreadPool;

    private long workItemsAdded = 0;
    private long workItemsDequeued = 0;
    private long totalTimeInQueue = 0;

    // Name of the work queue
    final private String name;

    public WorkQueueImpl() {
        this.name = WORKQUEUE_DEFAULT_NAME;
        this.queue = new LinkedList<Work>();
    }

    public WorkQueueImpl(ThreadPool workerThreadPool) {
        this(workerThreadPool, WORKQUEUE_DEFAULT_NAME);
    }

    public WorkQueueImpl(ThreadPool workerThreadPool, String name) {
        this.workerThreadPool = workerThreadPool;
        this.name = name;
        this.queue = new LinkedList<Work>();
    }

    private synchronized int getWorkQueueSize() {
        return queue.size();
    }

    public synchronized void addWork(Work work) {
        workItemsAdded++;
        work.setEnqueueTime(System.currentTimeMillis());

        queue.offer(work);
        notify();

        int waitingThreads = workerThreadPool.numberOfAvailableThreads();
        int threadCount = workerThreadPool.currentNumberOfThreads();
        int maxThreads = workerThreadPool.maximumNumberOfThreads();
        if (threadCount < maxThreads && waitingThreads < getWorkQueueSize()) {
        // NOTE: It is possible that the Work that was just added may unblock
        //       Worker Threads waiting on the Work just added and all Worker
        //       Threads are busy, (blocked & waiting for a response). This
        //       situation can lead to a deadlock.  The solution to such a
        //       a problem should it occur is to increase the maximum number
        //       of threads.
        // REVISIT - A possible solution to the above issue is check the
        //           enqueued Work timestamp periodically by another thread
        //           and create a Worker Thread if a piece of Work sits on 
        //           Work Queue for longer than some threshold.
            // add a WorkerThread
            ((ThreadPoolImpl)workerThreadPool).createWorkerThread();
        }
    }

    // XXX Re-write this to use a simple poll( waitTime, TimeUnit.MILLISECONDS )
    // and avoid the race conditions.  The change is a little too large to make
    // right now (a few days before GFv3 HCF).  See issue 7722.
    synchronized Work requestWork(long waitTime) throws WorkerThreadNotNeededException,
        InterruptedException {

        try {
            ((ThreadPoolImpl)workerThreadPool).incrementNumberOfAvailableThreads();

            // Wait for the queue to become non-empty.
            // Loop in case the wait() call returns early. This ensures that the full wait time is spent.
            // If the queue is non-empty now, the loop exits immediately.
            long startTime = Long.MAX_VALUE;
            while (queue.isEmpty()) {
                long now = System.currentTimeMillis();
                // Guard against the system clock running backwards, which might otherwise cause long wait times.
                startTime = Math.min(now, startTime);
                long endTime = startTime + waitTime;
                long remainingWaitTime = endTime - now;
                if (remainingWaitTime <= 0) {
                    break;
                }
                wait(remainingWaitTime);
            }
        } finally {
            ((ThreadPoolImpl)workerThreadPool).decrementNumberOfAvailableThreads();
        }

        Work work = queue.poll();
        if (work == null) {
            // The other waiting threads and this thread are available.
            int availableThreads = workerThreadPool.numberOfAvailableThreads() + 1;
            int minThreads = workerThreadPool.minimumNumberOfThreads();
            if (availableThreads > minThreads) {
                // This thread has timed out and can die because
                // we have enough available idle threads.
                // NOTE: It is expected that the WorkerThread calling this
                //       method will gracefully exit as a result of
                //       catching the WorkerThreadNotNeededException.
                ((ThreadPoolImpl)workerThreadPool).
                        decrementCurrentNumberOfThreads();
                throw new WorkerThreadNotNeededException();
            }
        } else {
            workItemsDequeued++;
            totalTimeInQueue += System.currentTimeMillis() - work.getEnqueueTime();
        }

        return work;
    }

    public synchronized void setThreadPool(ThreadPool workerThreadPool) {
        this.workerThreadPool = workerThreadPool;
    }

    public synchronized ThreadPool getThreadPool() {
        return workerThreadPool;
    }

    /**
     * Returns the total number of Work items added to the Queue.
     */
    @ManagedAttribute
    @Description( "Total number of items added to the queue" )
    public synchronized long totalWorkItemsAdded() {
        return workItemsAdded;
    }

    /**
     * Returns the total number of Work items in the Queue to be processed.
     */
    @ManagedAttribute
    @Description( "Total number of items in the queue to be processed" )
    public synchronized int workItemsInQueue() {
        return queue.size();
    }

    /**
     * Returns the average amount Work items have spent in the Queue waiting
     * to be processed.
     */
    @ManagedAttribute
    @Description( "Average time work items spend waiting in the queue in milliseconds" )
    public synchronized long averageTimeInQueue() {
        if (workItemsDequeued == 0) {
            return 0 ;
        } else { 
            return (totalTimeInQueue/workItemsDequeued);
        }
    }

    @NameValue
    public String getName() {
        return name;
    }
}

// End of file.
