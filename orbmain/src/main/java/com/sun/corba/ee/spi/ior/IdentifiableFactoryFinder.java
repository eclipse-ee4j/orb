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

package com.sun.corba.ee.spi.ior;

import org.omg.CORBA_2_3.portable.InputStream;

/**
 * Interface used to manage a group of related IdentifiableFactory instances. Factories can be registered, and invoked
 * through a create method, which must be implemented to handle the case of no registered factory appropriately.
 * 
 * @author Ken Cavanaugh
 */
public interface IdentifiableFactoryFinder<E extends Identifiable> {
    /**
     * If there is a registered factory for id, use it to read an Identifiable from is. Otherwise create an appropriate
     * generic container, or throw an error. The type of generic container, or error behavior is a property of the
     * implementation.
     * 
     * @param id id of registered factory
     * @param is stream to read from
     * @return {@link Identifiable} found
     */
    E create(int id, InputStream is);

    /**
     * Register a factory for the given id.
     * 
     * @param factory factory to register
     */
    void registerFactory(IdentifiableFactory<E> factory);
}
