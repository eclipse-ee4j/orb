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

import java.util.Iterator;

import org.omg.CORBA_2_3.portable.OutputStream;

import com.sun.corba.ee.spi.orb.ORB;

import com.sun.corba.ee.impl.ior.EncapsulationUtility;

public abstract class TaggedProfileTemplateBase extends IdentifiableContainerBase<TaggedComponent> implements TaggedProfileTemplate {
    public void write(OutputStream os) {
        EncapsulationUtility.writeEncapsulation(this, os);
    }

    public org.omg.IOP.TaggedComponent[] getIOPComponents(ORB orb, int id) {
        int count = 0;
        Iterator<TaggedComponent> iter = iteratorById(id);
        while (iter.hasNext()) {
            iter.next();
            count++;
        }

        org.omg.IOP.TaggedComponent[] result = new org.omg.IOP.TaggedComponent[count];

        int index = 0;
        iter = iteratorById(id);
        while (iter.hasNext()) {
            TaggedComponent comp = iter.next();
            result[index++] = comp.getIOPComponent(orb);
        }

        return result;
    }

    public <T extends TaggedComponent> Iterator<T> iteratorById(int id, Class<T> cls) {

        return (Iterator<T>) iteratorById(id);
    }
}
