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

import org.omg.CORBA.OctetSeqHolder;

import org.omg.CORBA_2_3.portable.InputStream;
import org.omg.CORBA_2_3.portable.OutputStream;

import com.sun.corba.ee.spi.ior.ObjectId;

import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.orb.ORBVersionFactory;

import com.sun.corba.ee.impl.encoding.CDRInputObject;

/**
 * Handles object keys created by JDK ORBs from before JDK 1.4.0.
 */
public final class OldJIDLObjectKeyTemplate extends OldObjectKeyTemplateBase {
    /**
     * JDK 1.3.1 FCS did not include a version byte at the end of its object keys. JDK 1.3.1_01 included the byte with the
     * value 1. Anything below 1 is considered an invalid value.
     */
    public static final byte NULL_PATCH_VERSION = 0;

    byte patchVersion = OldJIDLObjectKeyTemplate.NULL_PATCH_VERSION;

    public OldJIDLObjectKeyTemplate(ORB orb, int magic, int scid, InputStream is, OctetSeqHolder osh) {
        this(orb, magic, scid, is);

        osh.value = readObjectKey(is);

        /**
         * Beginning with JDK 1.3.1_01, a byte was placed at the end of the object key with a value indicating the patch
         * version. JDK 1.3.1_01 had the value 1. If other patches are necessary which involve ORB versioning changes, they
         * should increment the patch version.
         *
         * Note that if we see a value greater than 1 in this code, we will treat it as if we're talking to the most recent ORB
         * version.
         *
         * WARNING: This code is sensitive to changes in CDRInputStream getPosition. It assumes that the CDRInputStream is an
         * encapsulation whose position can be compared to the object key array length.
         */
        if (magic == ObjectKeyFactoryImpl.JAVAMAGIC_NEW && osh.value.length > ((CDRInputObject) is).getPosition()) {

            patchVersion = is.read_octet();

            if (patchVersion == ObjectKeyFactoryImpl.JDK1_3_1_01_PATCH_LEVEL) {
                setORBVersion(ORBVersionFactory.getJDK1_3_1_01());
            } else if (patchVersion > ObjectKeyFactoryImpl.JDK1_3_1_01_PATCH_LEVEL) {
                setORBVersion(ORBVersionFactory.getORBVersion());
            } else {
                throw wrapper.invalidJdk131PatchLevel(patchVersion);
            }
        }
    }

    public OldJIDLObjectKeyTemplate(ORB orb, int magic, int scid, int serverid) {
        super(orb, magic, scid, serverid, JIDL_ORB_ID, JIDL_OAID);
    }

    public OldJIDLObjectKeyTemplate(ORB orb, int magic, int scid, InputStream is) {
        this(orb, magic, scid, is.read_long());
    }

    protected void writeTemplate(OutputStream os) {
        os.write_long(getMagic());
        os.write_long(getSubcontractId());
        os.write_long(getServerId());
    }

    @Override
    public void write(ObjectId objectId, OutputStream os) {
        super.write(objectId, os);

        if (patchVersion != OldJIDLObjectKeyTemplate.NULL_PATCH_VERSION) {
            os.write_octet(patchVersion);
        }
    }
}
