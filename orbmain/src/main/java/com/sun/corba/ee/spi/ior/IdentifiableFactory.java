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

import org.omg.CORBA_2_3.portable.InputStream;

import com.sun.corba.ee.spi.ior.Identifiable;

import com.sun.corba.ee.spi.orb.ORB;

/**
 * Factory interface for creating Identifiables.
 */
public interface IdentifiableFactory<E extends Identifiable> {
    /**
     * Return the id of this factory, which is the id of the result of any create call.
     */
    public int getId();

    /**
     * Construct the appropriate Identifiable object with the given id from the InputStream is.
     */
    public E create(ORB orb, InputStream in);
}
