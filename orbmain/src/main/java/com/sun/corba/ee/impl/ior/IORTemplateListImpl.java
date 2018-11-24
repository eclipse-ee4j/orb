/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.ior;

import java.util.ArrayList;
import java.util.Iterator;

import org.omg.CORBA_2_3.portable.InputStream;
import org.omg.CORBA_2_3.portable.OutputStream;

import com.sun.corba.ee.spi.ior.IORTemplateList;
import com.sun.corba.ee.spi.ior.ObjectId;
import com.sun.corba.ee.spi.ior.IORTemplate;
import com.sun.corba.ee.spi.ior.IOR;
import com.sun.corba.ee.spi.ior.IORFactory;
import com.sun.corba.ee.spi.ior.IORFactories;

import com.sun.corba.ee.spi.orb.ORB;

public class IORTemplateListImpl extends FreezableList<IORTemplate> implements IORTemplateList {
    public IORTemplateListImpl() {
        super(new ArrayList<IORTemplate>());
    }

    public IORTemplateListImpl(InputStream is) {
        this();
        int size = is.read_long();
        for (int ctr = 0; ctr < size; ctr++) {
            IORTemplate iortemp = IORFactories.makeIORTemplate(is);
            add(iortemp);
        }

        makeImmutable();
    }

    @Override
    public void makeImmutable() {
        makeElementsImmutable();
        super.makeImmutable();
    }

    public void write(OutputStream os) {
        os.write_long(size());
        for (IORTemplate iortemp : this) {
            iortemp.write(os);
        }
    }

    public IOR makeIOR(ORB orb, String typeid, ObjectId oid) {
        return new IORImpl(orb, typeid, this, oid);
    }

    public boolean isEquivalent(IORFactory other) {
        if (!(other instanceof IORTemplateList))
            return false;

        IORTemplateList list = (IORTemplateList) other;

        Iterator<IORTemplate> thisIterator = iterator();
        Iterator<IORTemplate> listIterator = list.iterator();
        while (thisIterator.hasNext() && listIterator.hasNext()) {
            IORTemplate thisTemplate = thisIterator.next();
            IORTemplate listTemplate = listIterator.next();
            if (!thisTemplate.isEquivalent(listTemplate))
                return false;
        }

        return thisIterator.hasNext() == listIterator.hasNext();
    }
}
