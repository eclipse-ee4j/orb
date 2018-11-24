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

public interface Work {

    /**
     * This method denotes the actual work that is done by the work item.
     */
    public void doWork();

    /**
     * This methods sets the time in millis in the work item, when this work item was enqueued in the work queue.
     */
    public void setEnqueueTime(long timeInMillis);

    /**
     * This methods gets the time in millis in the work item, when this work item was enqueued in the work queue.
     */
    public long getEnqueueTime();

    /**
     * This method will return the name of the work item.
     */
    public String getName();

}

// End of file.
