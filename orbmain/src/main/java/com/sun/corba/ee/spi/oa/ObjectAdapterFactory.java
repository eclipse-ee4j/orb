/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.spi.oa;

import com.sun.corba.ee.spi.orb.ORB;

import com.sun.corba.ee.spi.ior.ObjectAdapterId;

public interface ObjectAdapterFactory {
    /**
     * Initialize this object adapter factory instance.
     */
    void init(ORB orb);

    /**
     * Shutdown all object adapters and other state associated with this factory.
     */
    void shutdown(boolean waitForCompletion);

    /**
     * Find the ObjectAdapter instance that corresponds to the given ObjectAdapterId.
     */
    ObjectAdapter find(ObjectAdapterId oaid);

    ORB getORB();
}
