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

public interface Work 
{ 

    /** 
     * This method denotes the actual work that is done by the work item.
     */ 
    public void doWork(); 

    /** 
     * This methods sets the time in millis in the work item, when this
     * work item was enqueued in the work queue.
     */ 
    public void setEnqueueTime(long timeInMillis);

    /** 
     * This methods gets the time in millis in the work item, when this
     * work item was enqueued in the work queue.
     */ 
    public long getEnqueueTime();

    /** 
    * This method will return the name of the work item. 
    */ 
    public String getName(); 

}

// End of file.

