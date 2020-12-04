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

package corba.requestpartitioning;

import com.sun.corba.ee.spi.threadpool.ThreadPoolChooser;
import com.sun.corba.ee.spi.threadpool.ThreadPoolManager;
import com.sun.corba.ee.spi.threadpool.ThreadPool;
import com.sun.corba.ee.spi.threadpool.NoSuchThreadPoolException;

import com.sun.corba.ee.impl.threadpool.ThreadPoolImpl;
import com.sun.corba.ee.spi.misc.ORBConstants;

import java.util.HashMap;
import java.util.ArrayList;

public class TestThreadPoolManager implements ThreadPoolManager { 

    public static final int NUMBER_OF_THREAD_POOLS_TO_CREATE = 64;

    private static final int DEFAULT_NUMBER_OF_QUEUES = 0;
    private static final int DEFAULT_MIN_THREAD_COUNT = 10;
    private static final int DEFAULT_MAX_THREAD_COUNT = 100;

    private static HashMap idToIndexTable = new HashMap();
    private static HashMap indexToIdTable = new HashMap();
    private static ArrayList threadpoolList = new ArrayList();
    private static String defaultID;

    private static ThreadPoolManager testThreadPoolMgr = new TestThreadPoolManager();

    public static ThreadPoolManager getThreadPoolManager() {
        return testThreadPoolMgr;
    }

    TestThreadPoolManager() {

        for (int i = 0; i < NUMBER_OF_THREAD_POOLS_TO_CREATE; i++) {
            createThreadPools(i);
        }
        defaultID = (String)indexToIdTable.get(new Integer(0));
    }

    private void createThreadPools(int index) {
        String threadpoolId = Integer.toString(index);

        // Mutiply the idleTimeoutInSeconds by 1000 to convert to milliseconds
        com.sun.corba.ee.spi.threadpool.ThreadPool threadpool = 
            new ThreadPoolImpl(DEFAULT_MIN_THREAD_COUNT,
                               DEFAULT_MAX_THREAD_COUNT, 
                               ThreadPoolImpl.DEFAULT_INACTIVITY_TIMEOUT * 1000,
                               threadpoolId);

        // Add the threadpool instance to the threadpoolList
        threadpoolList.add(threadpool);

        // Associate the threadpoolId to the index passed
        idToIndexTable.put(threadpoolId, new Integer(index));

        // Associate the threadpoolId to the index passed
        indexToIdTable.put(new Integer(index), threadpoolId);
        
    }

    /** 
    * This method will return an instance of the threadpool given a threadpoolId, 
    * that can be used by any component in the app. server. 
    *
    * @throws NoSuchThreadPoolException thrown when invalid threadpoolId is passed
    * as a parameter
    */ 
    public com.sun.corba.ee.spi.threadpool.ThreadPool
                                getThreadPool(String id) 
        throws NoSuchThreadPoolException {

        Integer i = (Integer)idToIndexTable.get(id);
        if (i == null) {
            throw new NoSuchThreadPoolException();
        }
        try {
            com.sun.corba.ee.spi.threadpool.ThreadPool threadpool =
                (com.sun.corba.ee.spi.threadpool.ThreadPool)
                threadpoolList.get(i.intValue());
            return threadpool;
        } catch (IndexOutOfBoundsException iobe) {
            throw new NoSuchThreadPoolException();
        }
    }

    /** 
    * This method will return an instance of the threadpool given a numeric threadpoolId. 
    * This method will be used by the ORB to support the functionality of 
    * dedicated threadpool for EJB beans 
    *
    * @throws NoSuchThreadPoolException thrown when invalidnumericIdForThreadpool is passed
    * as a parameter
    */ 
    public com.sun.corba.ee.spi.threadpool.ThreadPool 
                        getThreadPool(int numericIdForThreadpool) 
        throws NoSuchThreadPoolException { 

        try {
            com.sun.corba.ee.spi.threadpool.ThreadPool threadpool =
                (com.sun.corba.ee.spi.threadpool.ThreadPool)
                threadpoolList.get(numericIdForThreadpool);
            return threadpool;
        } catch (IndexOutOfBoundsException iobe) {
            throw new NoSuchThreadPoolException();
        }
    }

    /** 
    * This method is used to return the numeric id of the threadpool, given a String 
    * threadpoolId. This is used by the POA interceptors to add the numeric threadpool 
    * Id, as a tagged component in the IOR. This is used to provide the functionality of 
    * dedicated threadpool. 
    */ 
    public int  getThreadPoolNumericId(String id) { 
        Integer i = (Integer)idToIndexTable.get(id);
        return ((i == null) ? 0 : i.intValue());
    }

    /** 
    * Return a String Id for a numericId of a threadpool managed by the threadpool 
    * manager 
    */ 
    public String getThreadPoolStringId(int numericIdForThreadpool) {
        String id = (String)indexToIdTable.get(new Integer(numericIdForThreadpool));
        return ((id == null) ? defaultID : id);
    } 

    /** 
    * Returns the first instance of ThreadPool in the ThreadPoolManager 
    */ 
    public com.sun.corba.ee.spi.threadpool.ThreadPool 
                                        getDefaultThreadPool() {
        try {
            return getThreadPool(0);
        } catch (NoSuchThreadPoolException nstpe) {
            System.err.println("No default ThreadPool defined " + nstpe);
            System.exit(1);
        }
        return null;
    }

    public ThreadPoolChooser getThreadPoolChooser(String componentId) {
        // not used
        return null;
    }

    public ThreadPoolChooser getThreadPoolChooser(int componentIndex) {
        // not used
        return null;
    }

    public void setThreadPoolChooser(String componentId, ThreadPoolChooser aThreadPoolChooser) {
        // not used
    }

    public int getThreadPoolChooserNumericId(String componentId) {
        // not used
        return 0;
    }

    public void close() throws java.io.IOException {
    }
} 


