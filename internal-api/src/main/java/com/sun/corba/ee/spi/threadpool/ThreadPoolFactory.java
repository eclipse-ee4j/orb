/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.spi.threadpool;

import com.sun.corba.ee.impl.threadpool.ThreadPoolImpl ;

public class ThreadPoolFactory {
    /** Create an unbounded thread pool in the current thread group
     * with the current context ClassLoader as the worker thread default
     * ClassLoader.
     */
    public ThreadPool create( String threadpoolName) {
        return new ThreadPoolImpl( threadpoolName ) ;
    }

    /** Create an unbounded thread pool in the given thread group
     * with the current context ClassLoader as the worker thread default
     * ClassLoader.
     */
    public ThreadPool create( ThreadGroup tg, String threadpoolName ) {
        return new ThreadPoolImpl( tg, threadpoolName ) ;
    }

    /** Create an unbounded thread pool in the given thread group
     * with the given ClassLoader as the worker thread default
     * ClassLoader.
     */
    public ThreadPool create(ThreadGroup tg, String threadpoolName, 
        ClassLoader defaultClassLoader) {
        return new ThreadPoolImpl( tg, threadpoolName, defaultClassLoader ) ;
    }
 
    /** Create a bounded thread pool in the current thread group
     * with the current context ClassLoader as the worker thread default
     * ClassLoader.
     */
    public ThreadPool create( int minSize, int maxSize, long timeout, 
        String threadpoolName) {

        return new ThreadPoolImpl( minSize, maxSize, timeout, threadpoolName ) ;
    }

    /** Create a bounded thread pool in the current thread group
     * with the given ClassLoader as the worker thread default
     * ClassLoader.
     */
    public ThreadPool create( int minSize, int maxSize, long timeout, 
        String threadpoolName, ClassLoader defaultClassLoader ) 
    {
        return new ThreadPoolImpl( minSize, maxSize, timeout,
            threadpoolName, defaultClassLoader ) ;
    }
}
