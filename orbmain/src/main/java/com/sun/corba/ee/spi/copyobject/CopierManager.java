/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.spi.copyobject;

import org.glassfish.pfl.dynamic.copyobject.spi.ObjectCopierFactory;

/**
 * Manager of ObjectCopier implementations used to support javax.rmi.CORBA.Util.copyObject(s). This provides simple
 * methods for registering all supported ObjectCopier factories. A default copier is also supported, for use in contexts
 * where no specific copier id is available.
 */
public interface CopierManager {
    /**
     * Set the Id of the copier to use if no other copier has been set.
     */
    void setDefaultId(int id);

    /**
     * Return the copier for the default copier id. Throws a BAD_PARAM exception if no default copier id has been set.
     */
    int getDefaultId();

    ObjectCopierFactory getObjectCopierFactory(int id);

    ObjectCopierFactory getDefaultObjectCopierFactory();

    /**
     * Register an ObjectCopierFactory under a particular id. This can be retrieved later by getObjectCopierFactory.
     */
    void registerObjectCopierFactory(ObjectCopierFactory factory, int id);
}
