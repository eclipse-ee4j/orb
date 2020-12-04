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

package com.sun.corba.ee.spi.threadpool;

public interface WorkQueue 
{ 

    /** 
    * This method is used to add work to the WorkQueue 
    */ 
    public void addWork(Work aWorkItem); 

    /** 
    * This method will return the name of the WorkQueue. 
    */ 
    public String getName(); 

    /** 
    * Returns the total number of Work items added to the Queue. 
    */ 
    public long totalWorkItemsAdded(); 

    /** 
    * Returns the total number of Work items in the Queue to be processed. 
    */ 
    public int workItemsInQueue(); 

    /** 
    * Returns the average time a work item is waiting in the queue before
    * getting processed.
    */ 
    public long averageTimeInQueue();

    /**
     * Set the ThreadPool instance servicing this WorkQueue
     */
    public void setThreadPool(ThreadPool aThreadPool);

    /**
     * Get the ThreadPool instance servicing this WorkQueue
     */
    public ThreadPool getThreadPool();
}

// End of file.
