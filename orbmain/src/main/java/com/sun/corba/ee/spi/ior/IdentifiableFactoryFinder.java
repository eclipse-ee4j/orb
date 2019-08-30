/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.spi.ior;

import org.omg.CORBA_2_3.portable.InputStream ;

/** Interface used to manage a group of related IdentifiableFactory instances.
 * Factories can be registered, and invoked through a create method, which 
 * must be implemented to handle the case of no registered factory 
 * appropriately.
 * @author Ken Cavanaugh
 */
public interface IdentifiableFactoryFinder<E extends Identifiable> 
{
    /** If there is a registered factory for id, use it to 
     * read an Identifiable from is.  Otherwise create an
     * appropriate generic container, or throw an error.
     * The type of generic container, or error behavior is 
     * a property of the implementation.
     * 
     * @param id id of factory to use
     * @param is  stream to create factory from
     * @return found factory or generic container
     */
    E create(int id, InputStream is);

    /** Register a factory for the given id.
     * 
     * @param factory factory to register
     */
    void registerFactory( IdentifiableFactory<E> factory ) ; 
}
