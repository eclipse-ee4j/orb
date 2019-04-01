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

import org.omg.CORBA_2_3.portable.InputStream;
import org.omg.CORBA_2_3.portable.OutputStream;

import com.sun.corba.ee.spi.ior.ObjectId;
import com.sun.corba.ee.spi.ior.ObjectAdapterId;

import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.orb.ORBVersion;
import com.sun.corba.ee.spi.orb.ORBVersionFactory;

public abstract class NewObjectKeyTemplateBase extends ObjectKeyTemplateBase {
    public NewObjectKeyTemplateBase(ORB orb, int magic, int scid, int serverid, String orbid, ObjectAdapterId oaid) {
        super(orb, magic, scid, serverid, orbid, oaid);
        // subclass must set the version, since we don't have the object key here.

        if (magic != ObjectKeyFactoryImpl.JAVAMAGIC_NEWER) {
            throw wrapper.badMagic(magic);
        }
    }

    @Override
    public void write(ObjectId objectId, OutputStream os) {
        super.write(objectId, os);
        getORBVersion().write(os);
    }

    @Override
    public void write(OutputStream os) {
        super.write(os);
        getORBVersion().write(os);
    }

    protected void setORBVersion(InputStream is) {
        ORBVersion version = ORBVersionFactory.create(is);
        setORBVersion(version);
    }
}
