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
     * 
     * @param id ID of the copier
     */
    void setDefaultId(int id);

    /**
     * Return the copier for the default copier id. Throws a BAD_PARAM exception if no default copier id has been set.
     * 
     * @return ID of the copier
     */
    int getDefaultId();

    ObjectCopierFactory getObjectCopierFactory(int id);

    ObjectCopierFactory getDefaultObjectCopierFactory();

    /**
     * Register an ObjectCopierFactory under a particular id. This can be retrieved later by getObjectCopierFactory.
     * 
     * @param factory Factory to register
     * @param id ID of the factory
     */
    void registerObjectCopierFactory(ObjectCopierFactory factory, int id);
}
