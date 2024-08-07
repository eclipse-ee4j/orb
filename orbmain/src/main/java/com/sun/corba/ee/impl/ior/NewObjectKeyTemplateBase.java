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
