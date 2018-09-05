/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.threadpool;

import java.io.IOException ;
import java.io.Closeable ;

import java.security.AccessController;
import java.security.PrivilegedAction;

import java.util.List ;
import java.util.ArrayList ;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import com.sun.corba.ee.spi.threadpool.NoSuchWorkQueueException;
import com.sun.corba.ee.spi.threadpool.ThreadPool;
import com.sun.corba.ee.spi.threadpool.ThreadStateValidator;
import com.sun.corba.ee.spi.threadpool.Work;
import com.sun.corba.ee.spi.threadpool.WorkQueue;

import org.glassfish.gmbal.ManagedObject ;
import org.glassfish.gmbal.Description ;
import org.glassfish.gmbal.ManagedAttribute ;
import org.glassfish.gmbal.NameValue ;

@ManagedObject
@Description( "A ThreadPool used by the ORB" ) 
public class ThreadPoolImpl implements ThreadPool
{
    public static final int DEFAULT_INACTIVITY_TIMEOUT = 120000;

    // serial counter useful for debugging
    private static final AtomicInteger threadCounter = new AtomicInteger(0);

    // Any time currentThreadCount and/or availableWorkerThreads is updated
    // or accessed this ThreadPool's WorkQueue must be locked. And, it is 
    // expected that this ThreadPool's WorkQueue is the only object that
    // updates and accesses these values directly and indirectly though a
    // call to a method in this ThreadPool. If any call to update or access
    // those values must synchronized on this ThreadPool's WorkQueue.
    final private WorkQueue workQueue;
    
    // Stores the number of available worker threads
    private int availableWorkerThreads = 0;
    
    // Stores the number of threads in the threadpool currently
    private int currentThreadCount = 0;
    
    // Minimum number of worker threads created at instantiation of the threadpool
    final private int minWorkerThreads;
    
    // Maximum number of worker threads in the threadpool
    final private int maxWorkerThreads;
    
    // Inactivity timeout value for worker threads to exit and stop running
    final private long inactivityTimeout;
    
    // Running count of the work items processed
    // Set the value to 1 so that divide by zero is avoided in 
    // averageWorkCompletionTime()
    private AtomicLong processedCount = new AtomicLong(1);
    
    // Running aggregate of the time taken in millis to execute work items
    // processed by the threads in the threadpool
    private AtomicLong totalTimeTaken = new AtomicLong(0);

    // Name of the ThreadPool
    final private String name;

    // ThreadGroup in which threads should be created
    private ThreadGroup threadGroup ;

    final private ClassLoader workerThreadClassLoader ; 

    final Object workersLock = new Object() ;

    List<WorkerThread> workers = new ArrayList<WorkerThread>() ;

    /** Create an unbounded thread pool in the current thread group
     * with the current context ClassLoader as the worker thread default
     * ClassLoader.
     */
    public ThreadPoolImpl(String threadpoolName) {
        this( Thread.currentThread().getThreadGroup(), threadpoolName ) ; 
    }

    /** Create an unbounded thread pool in the given thread group
     * with the current context ClassLoader as the worker thread default
     * ClassLoader.
     */
    public ThreadPoolImpl(ThreadGroup tg, String threadpoolName ) {
        this( tg, threadpoolName, getDefaultClassLoader() ) ;
    }

    /** Create an unbounded thread pool in the given thread group
     * with the given ClassLoader as the worker thread default
     * ClassLoader.
     */
    public ThreadPoolImpl(ThreadGroup tg, String threadpoolName, 
        ClassLoader defaultClassLoader) {

        inactivityTimeout = DEFAULT_INACTIVITY_TIMEOUT;
        minWorkerThreads = 0;
        maxWorkerThreads = Integer.MAX_VALUE;
        workQueue = new WorkQueueImpl(this);
        // XXX register this with gmbal.
        threadGroup = tg ;
        name = threadpoolName;
        workerThreadClassLoader = defaultClassLoader ;
    }
 
    /** Create a bounded thread pool in the current thread group
     * with the current context ClassLoader as the worker thread default
     * ClassLoader.
     */
    public ThreadPoolImpl( int minSize, int maxSize, long timeout, 
        String threadpoolName) {

        this( minSize, maxSize, timeout, threadpoolName, getDefaultClassLoader() ) ;
    }

    /** Create a bounded thread pool in the current thread group
     * with the given ClassLoader as the worker thread default
     * ClassLoader.
     */
    public ThreadPoolImpl( int minSize, int maxSize, long timeout, 
        String threadpoolName, ClassLoader defaultClassLoader ) 
    {
        inactivityTimeout = timeout;
        minWorkerThreads = minSize;
        maxWorkerThreads = maxSize;
        workQueue = new WorkQueueImpl(this);
        threadGroup = Thread.currentThread().getThreadGroup() ;
        name = threadpoolName;
        workerThreadClassLoader = defaultClassLoader ;
        synchronized (workQueue) {
            for (int i = 0; i < minWorkerThreads; i++) {
                createWorkerThread();
            }
        }
    }


    // Note that this method should not return until AFTER all threads have died.
    public void close() throws IOException {
        // Copy to avoid concurrent modification problems.
        List<WorkerThread> copy = null ;
        synchronized (workersLock) {
            copy = new ArrayList<WorkerThread>( workers ) ;
        }

        for (WorkerThread wt : copy) {
            wt.close() ;

            while (wt.getState() != Thread.State.TERMINATED) {
                try {
                    wt.join() ;
                } catch (InterruptedException exc) {
                    Exceptions.self.interruptedJoinCallWhileClosingThreadPool( exc,
                        wt, this ) ;
                }
            }
        }

        threadGroup = null ;
    }

    private static ClassLoader getDefaultClassLoader() {
        if (System.getSecurityManager() == null)
            return Thread.currentThread().getContextClassLoader() ;
        else {
            final ClassLoader cl = AccessController.doPrivileged( 
                new PrivilegedAction<ClassLoader>() {
                    public ClassLoader run() {
                        return Thread.currentThread().getContextClassLoader() ;
                    }
                } 
            ) ;

            return cl ;
        }
    }

    public WorkQueue getAnyWorkQueue()
    {
        return workQueue;
    }

    public WorkQueue getWorkQueue(int queueId)
        throws NoSuchWorkQueueException
    {
        if (queueId != 0)
            throw new NoSuchWorkQueueException();
        return workQueue;
    }

    private Thread createWorkerThreadHelper( String name ) { 
        // Thread creation needs to be in a doPrivileged block
        // if there is a non-null security manager for two reasons:
        // 1. The creation of a thread in a specific ThreadGroup
        //    is a privileged operation.  Lack of a doPrivileged
        //    block here causes an AccessControlException
        //    (see bug 6268145).
        // 2. We want to make sure that the permissions associated
        //    with this thread do NOT include the permissions of
        //    the current thread that is calling this method.
        //    This leads to problems in the app server where
        //    some threads in the ThreadPool randomly get
        //    bad permissions, leading to unpredictable
        //    permission errors (see bug 6021011).
        //
        //    A Java thread contains a stack of call frames,
        //    one for each method called that has not yet returned.
        //    Each method comes from a particular class.  The class
        //    was loaded by a ClassLoader which has an associated
        //    CodeSource, and this determines the Permissions
        //    for all methods in that class.  The current
        //    Permissions for the thread are the intersection of
        //    all Permissions for the methods on the stack.
        //    This is part of the Security Context of the thread.
        //
        //    When a thread creates a new thread, the new thread
        //    inherits the security context of the old thread.
        //    This is bad in a ThreadPool, because different
        //    creators of threads may have different security contexts.
        //    This leads to occasional unpredictable errors when
        //    a thread is re-used in a different security context.
        //
        //    Avoiding this problem is simple: just do the thread
        //    creation in a doPrivileged block.  This sets the
        //    inherited security context to that of the code source
        //    for the ORB code itself, which contains all permissions
        //    in either Java SE or Java EE.
        WorkerThread thread = new WorkerThread(threadGroup, name);
        synchronized (workersLock) {
            workers.add( thread ) ;
        }
        
        // The thread must be set to a daemon thread so the
        // VM can exit if the only threads left are PooledThreads
        // or other daemons.  We don't want to rely on the
        // calling thread always being a daemon.
        // Note that no exception is possible here since we
        // are inside the doPrivileged block.
        thread.setDaemon(true);
        
        Exceptions.self.workerThreadCreated( thread, thread.getContextClassLoader() ) ;
        
        thread.start();
        return null ;
    }

    /**
     * To be called from the WorkQueue to create worker threads when none
     * available.
     */
    void createWorkerThread() {
        final String lname = getName();
        synchronized (workQueue) {
            try {
                if (System.getSecurityManager() == null) {
                    createWorkerThreadHelper(lname) ;
                } else {
                    // If we get here, we need to create a thread.
                    AccessController.doPrivileged(
                            new PrivilegedAction() {
                        public Object run() {
                            return createWorkerThreadHelper(lname) ;
                        }
                    }
                    ) ;
                }
            } catch (Throwable t) {
                // Decrementing the count of current worker threads.
                // But, it will be increased in the finally block.
                decrementCurrentNumberOfThreads();
                Exceptions.self.workerThreadCreationFailure(t);
            } finally {
                incrementCurrentNumberOfThreads();
            }
        }
    }
    
    public int minimumNumberOfThreads() {
        return minWorkerThreads;
    }

    public int maximumNumberOfThreads() {
        return maxWorkerThreads;
    }

    public long idleTimeoutForThreads() {
        return inactivityTimeout;
    }
    
    @ManagedAttribute
    @Description( "The current number of threads" ) 
    public int currentNumberOfThreads() {
        synchronized (workQueue) {
            return currentThreadCount;
        }
    }

    void decrementCurrentNumberOfThreads() {
        synchronized (workQueue) {
            currentThreadCount--;
        }
    }

    void incrementCurrentNumberOfThreads() {
        synchronized (workQueue) {
            currentThreadCount++;
        }
    }

    @ManagedAttribute
    @Description( "The number of available threads in this ThreadPool" ) 
    public int numberOfAvailableThreads() {
         synchronized (workQueue) {
            return availableWorkerThreads;
        }
    }

    @ManagedAttribute
    @Description( "The number of threads busy processing work in this ThreadPool" ) 
    public int numberOfBusyThreads() {
        synchronized (workQueue) {
            return (currentNumberOfThreads() - numberOfAvailableThreads());
        }
    }
    
    @ManagedAttribute
    @Description( "The average time needed to complete a work item" ) 
    public long averageWorkCompletionTime() {
        return (totalTimeTaken.get() / processedCount.get());
    }
    
    @ManagedAttribute
    @Description( "The number of work items processed" ) 
    public long currentProcessedCount() {
        return processedCount.get();
    }

    @NameValue
    public String getName() {
        return name;
    }

    /** 
    * This method will return the number of WorkQueues serviced by the threadpool. 
    */ 
    public int numberOfWorkQueues() {
        return 1;
    } 


    private static int getUniqueThreadId() {
        return ThreadPoolImpl.threadCounter.incrementAndGet();
    }

    /** 
     * This method will decrement the number of available threads
     * in the threadpool which are waiting for work. Called from 
     * WorkQueueImpl.requestWork()
     */ 
    void decrementNumberOfAvailableThreads() {
        synchronized (workQueue) {
            availableWorkerThreads--;
        }
    }
    
    /** 
     * This method will increment the number of available threads
     * in the threadpool which are waiting for work. Called from 
     * WorkQueueImpl.requestWork()
     */ 
    void incrementNumberOfAvailableThreads() {
        synchronized (workQueue) {
            availableWorkerThreads++;
        }
    }

    private class WorkerThread extends Thread implements Closeable
    {
        final private static String THREAD_POOLNAME_PREFIX_STR = "p: ";
        final private static String WORKER_THREAD_NAME_PREFIX_STR = "; w: ";
        final private static String IDLE_STR = "Idle";

        private Work currentWork ;
        private volatile boolean closeCalled = false ;

        WorkerThread(ThreadGroup tg, String threadPoolName) {
            super(tg, THREAD_POOLNAME_PREFIX_STR + threadPoolName + 
                  WORKER_THREAD_NAME_PREFIX_STR + ThreadPoolImpl.getUniqueThreadId());
            this.currentWork = null;
        }

        private void setClassLoader() {
            if (System.getSecurityManager() == null)
                setClassLoaderHelper() ;
            else {
                AccessController.doPrivileged( 
                    new PrivilegedAction<ClassLoader>() {
                        public ClassLoader run() {
                            return WorkerThread.this.setClassLoaderHelper() ;
                        }
                    } 
                ) ;
            }
        }

        private ClassLoader setClassLoaderHelper() {
            Thread thr = Thread.currentThread() ;
            ClassLoader result = thr.getContextClassLoader() ;
            thr.setContextClassLoader( workerThreadClassLoader ) ;
            return result ; 
        }

        public synchronized void close() {
            closeCalled = true ;
            interrupt() ;
        }
        
        private void resetClassLoader() {
            ClassLoader currentClassLoader = null;
            try {
                if (System.getSecurityManager() == null) {
                    currentClassLoader = getContextClassLoader() ;
                } else {
                    currentClassLoader = AccessController.doPrivileged(
                        new PrivilegedAction<ClassLoader>() {
                            public ClassLoader run() {
                                return getContextClassLoader();
                            }
                        } 
                    );
                }
            } catch (SecurityException se) {
                throw Exceptions.self.workerThreadGetContextClassloaderFailed(se, this);
            }

            if (workerThreadClassLoader != currentClassLoader) {
                Exceptions.self.workerThreadForgotClassloaderReset(this, 
                    currentClassLoader, workerThreadClassLoader);

                try {
                    setClassLoader() ;
                } catch (SecurityException se) {
                    Exceptions.self.workerThreadResetContextClassloaderFailed(se, this);
                }
            }
        }

        private void performWork() {
            long start = System.currentTimeMillis();
            try {
                currentWork.doWork();
            } catch (Throwable t) {
                Exceptions.self.workerThreadDoWorkThrowable(t, this);
            } finally {
                ThreadStateValidator.checkValidators();
            }
            long elapsedTime = System.currentTimeMillis() - start;
            totalTimeTaken.addAndGet(elapsedTime);
            processedCount.incrementAndGet();
        }

        @Override
        public void run() {
            try  {
                // Issue 13266: Make sure that the ClassLoader is set the FIRST time
                // the worker thread runs.  resetClassLoader below takes care of the
                // other cases.
                setClassLoader() ;

                while (!closeCalled) {
                    try {
                        currentWork = ((WorkQueueImpl)workQueue).requestWork(
                            inactivityTimeout);
                        if (currentWork == null) 
                            continue;
                    } catch (WorkerThreadNotNeededException toe) {
                        Exceptions.self.workerThreadNotNeeded(this, 
                            currentNumberOfThreads(), minimumNumberOfThreads());
                        closeCalled = true ;
                        continue ;
                    } catch (InterruptedException exc) {
                        Thread.interrupted() ;
                        Exceptions.self.workQueueThreadInterrupted( exc, super.getName(), 
                            Boolean.valueOf( closeCalled ) ) ;

                        continue ;
                    } catch (Throwable t) {
                        Exceptions.self.workerThreadThrowableFromRequestWork(t, this, 
                                workQueue.getName());
                        
                        continue;
                    } 

                    performWork() ;

                    // set currentWork to null so that the work item can be 
                    // garbage collected without waiting for the next work item.
                    currentWork = null;

                    resetClassLoader() ;
                }
            } catch (Throwable e) {
                // This should not be possible
                Exceptions.self.workerThreadCaughtUnexpectedThrowable(e, this);
            } finally {
                synchronized (workersLock) {
                    workers.remove( this ) ;
                }
            }
        }
    } // End of WorkerThread class
}

// End of file.
