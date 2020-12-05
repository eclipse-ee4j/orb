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


public interface ThreadPoolChooser 
{
    /**
     * This method is used to return an instance of ThreadPool based on the 
     * strategy/policy implemented in the ThreadPoolChooser from the set of
     * ThreadPools allowed to be used by the ORB. Typically, the set of 
     * ThreadPools would be specified by passing the threadpool-ids
     * configured in the ORB element of the domain.xml of the appserver.
     */
    public ThreadPool getThreadPool();

    /**
     * This method is used to return an instance of ThreadPool that is obtained
     * by using the id argument passed to it. This method will be used in 
     * situations where the threadpool id is known to the caller e.g. by the
     * connection object or looking at the high order bits of the request id
     */
    public ThreadPool getThreadPool(int id);

    /**
     * This method is a convenience method to see what threadpool-ids are being
     * used by the ThreadPoolChooser
     */
    public String[] getThreadPoolIds();
}
