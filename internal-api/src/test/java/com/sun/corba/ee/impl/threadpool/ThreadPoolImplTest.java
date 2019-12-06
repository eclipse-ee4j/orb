package com.sun.corba.ee.impl.threadpool;

import com.sun.corba.ee.spi.threadpool.Work;
import com.sun.corba.ee.spi.threadpool.WorkQueue;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test ThreadPoolImpl and the associated class WorkQueueImpl.
 */
public class ThreadPoolImplTest {
    /**
     * Test that a single work item is executed correctly.
     */
    @Test
    public void executeSingle() throws IOException, InterruptedException {
        try (ThreadPoolImpl threadPool = new ThreadPoolImpl(0, 1, 2000L, "the-pool")) {
            WorkQueue workQueue = threadPool.getAnyWorkQueue();

            WorkImpl work = new WorkImpl();
            workQueue.addWork(work);

            assertTrue(work.started.await(1, TimeUnit.SECONDS));
            work.finish.countDown();
        }
    }

    /**
     * Test that two work items can be executed in parallel.
     * Also check that the thread pool is eventually stopping all but the
     * configured minimum number of threads, but leaves that number of
     * threads alive.
     */
    @Test
    public void executeInParallelAndStop() throws IOException, InterruptedException {
        try (ThreadPoolImpl threadPool = new ThreadPoolImpl(1, 2, 2000L, "the-pool")) {
            WorkQueue workQueue = threadPool.getAnyWorkQueue();

            WorkImpl first = new WorkImpl();
            workQueue.addWork(first);
            WorkImpl second = new WorkImpl();
            workQueue.addWork(second);

            // Check that the two work items are executed in parallel.
            assertTrue(first.started.await(1, TimeUnit.SECONDS));
            assertTrue(second.started.await(1, TimeUnit.SECONDS));
            assertEquals(2, threadPool.workers.size());
            first.finish.countDown();
            second.finish.countDown();

            // Give the thread pool time to end as many threads as it considers appropriate.
            // Be generous here.
            Thread.sleep(5000L);

            // Pool should end one worker thread, but not both.
            assertEquals(1, threadPool.workers.size());
        }
    }

    /**
     * Test that work items have to wait when the maximum pool size is reached.
     */
    @Test
    public void waitingWorkItem() throws IOException, InterruptedException {
        try (ThreadPoolImpl threadPool = new ThreadPoolImpl(0, 1, 2000L, "the-pool")) {
            WorkQueue workQueue = threadPool.getAnyWorkQueue();

            WorkImpl first = new WorkImpl();
            workQueue.addWork(first);
            WorkImpl second = new WorkImpl();
            workQueue.addWork(second);

            // Give the pool the chance to start the second work item in parallel.
            Thread.sleep(1000L);

            // Only the first work item should have been started.
            assertTrue(first.started.await(1, TimeUnit.SECONDS));
            assertEquals(1, second.started.getCount());
            first.finish.countDown();

            // The seconds work item should get its turn eventually.
            assertTrue(second.started.await(1, TimeUnit.SECONDS));
            second.finish.countDown();
        }
    }

    /**
     * Test that the thread count in the pool goes down after a while,
     * but goes up again as needed.
     */
    @Test
    public void endThread() throws IOException, InterruptedException {
        try (ThreadPoolImpl threadPool = new ThreadPoolImpl(0, 1, 2000L, "the-pool")) {
            WorkQueue workQueue = threadPool.getAnyWorkQueue();

            // Do work.
            WorkImpl first = new WorkImpl();
            first.finish.countDown();
            workQueue.addWork(first);

            // The pool should end the worker thread eventually.
            Thread.sleep(5000L);
            assertTrue(threadPool.workers.isEmpty());

            // Do work again.
            WorkImpl work = new WorkImpl();
            workQueue.addWork(work);
            assertTrue(work.started.await(1, TimeUnit.SECONDS));
            work.finish.countDown();
        }
    }

    /**
     * Test that a worker thread can distinguish correctly between a notification
     * about a new work item and a timeout after a wait period.
     */
    @Test
    public void notifyDuringTimeout() throws IOException, InterruptedException {
        try (ThreadPoolImpl threadPool = new ThreadPoolImpl(0, 1, 2000L, "the-pool")) {
            WorkQueue workQueue = threadPool.getAnyWorkQueue();

            // Make sure one work item is processed, leaving one worker thread idle.
            WorkImpl first = new WorkImpl();
            workQueue.addWork(first);
            assertTrue(first.started.await(1, TimeUnit.SECONDS));
            first.finish.countDown();

            // Give the thread some time to starting waiting, but not enough time to terminate.
            Thread.sleep(1000L);

            // Suspend the worker thread, so that its next wakeup is guaranteed to
            // coincide with the end of the timeout period. This is normally only possible
            // in case of a race condition, but suspending reproduces the situation
            // reliably. Note that a long garbage collection acts effectively like a suspend
            // as far as Java code is concerned, so that this is a realistic test.
            assertEquals(1, threadPool.workers.size());
            Thread workerThread = threadPool.workers.get(0);
            workerThread.suspend();
            Thread.sleep(2000L);

            // Add new work, notifying the worker thread.
            WorkImpl second = new WorkImpl();
            workQueue.addWork(second);
            // Only now resume the worker thread.
            workerThread.resume();
            assertTrue(second.started.await(1, TimeUnit.SECONDS));
            second.finish.countDown();
        }
    }

    /**
     * A test work item whose behavior can be controlled externally.
     */
    @SuppressWarnings("JUnitTestCaseWithNoTests")
    private static class WorkImpl implements Work {
        /** triggered by the work item once the work has started */
        CountDownLatch started = new CountDownLatch(1);
        /** triggered by the test to let the execution continue */
        CountDownLatch finish = new CountDownLatch(1);

        @Override
        public void doWork() {
            started.countDown();

            try {
                finish.await();
            } catch (InterruptedException e) {
                throw new IllegalStateException("unexpected interruption", e);
            }
        }

        @Override
        public void setEnqueueTime(long timeInMillis) {
        }

        @Override
        public long getEnqueueTime() {
            return 0;
        }

        @Override
        public String getName() {
            return "the-name";
        }
    }
}
