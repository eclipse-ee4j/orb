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

import com.sun.corba.ee.spi.orb.ORB;

import com.sun.corba.ee.impl.ior.EncapsulationUtility;

public abstract class EncapsulationFactoryBase<E extends Identifiable> implements IdentifiableFactory<E> {

    private int id;

    public int getId() {
        return id;
    }

    public EncapsulationFactoryBase(int id) {
        this.id = id;
    }

    public final E create(ORB orb, InputStream in) {
        InputStream is = EncapsulationUtility.getEncapsulationStream(orb, in);
        return readContents(is);
    }

    protected abstract E readContents(InputStream is);
}
