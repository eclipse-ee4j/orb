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

import org.omg.CORBA.OctetSeqHolder;

import com.sun.corba.ee.spi.ior.ObjectId;
import com.sun.corba.ee.spi.ior.ObjectKeyFactory;

import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.orb.ORBVersion;
import com.sun.corba.ee.spi.orb.ORBVersionFactory;

import com.sun.corba.ee.impl.ior.ObjectKeyFactoryImpl;

/**
 * @author Ken Cavanaugh
 */
public final class JIDLObjectKeyTemplate extends NewObjectKeyTemplateBase {
    /**
     * This constructor reads the template ONLY from the stream.
     */
    public JIDLObjectKeyTemplate(ORB orb, int magic, int scid, InputStream is) {
        super(orb, magic, scid, is.read_long(), JIDL_ORB_ID, JIDL_OAID);

        setORBVersion(is);
    }

    /**
     * This constructor reads a complete ObjectKey (template and Id) from the stream.
     */
    public JIDLObjectKeyTemplate(ORB orb, int magic, int scid, InputStream is, OctetSeqHolder osh) {
        super(orb, magic, scid, is.read_long(), JIDL_ORB_ID, JIDL_OAID);

        osh.value = readObjectKey(is);

        setORBVersion(is);
    }

    public JIDLObjectKeyTemplate(ORB orb, int scid, int serverid) {
        super(orb, ObjectKeyFactoryImpl.JAVAMAGIC_NEWER, scid, serverid, JIDL_ORB_ID, JIDL_OAID);

        setORBVersion(ORBVersionFactory.getORBVersion());
    }

    protected void writeTemplate(OutputStream os) {
        os.write_long(getMagic());
        os.write_long(getSubcontractId());
        os.write_long(getServerId());
    }
}
