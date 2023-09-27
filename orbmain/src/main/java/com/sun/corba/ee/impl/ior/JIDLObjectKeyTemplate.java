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

import org.omg.CORBA.OctetSeqHolder;

import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.orb.ORBVersionFactory;

/**
 * @author Ken Cavanaugh
 */
public final class JIDLObjectKeyTemplate extends NewObjectKeyTemplateBase {
    /**
     * This constructor reads the template ONLY from the stream.
     * 
     * @param orb ORB to use
     * @param magic Magic number
     * @param scid ID of template
     * @param is stream to read from
     */
    public JIDLObjectKeyTemplate(ORB orb, int magic, int scid, InputStream is) {
        super(orb, magic, scid, is.read_long(), JIDL_ORB_ID, JIDL_OAID);

        setORBVersion(is);
    }

    /**
     * This constructor reads a complete ObjectKey (template and Id) from the stream.
     * 
     * @param orb ORB to use
     * @param magic Magic number
     * @param scid ID of the Object
     * @param is Stream to read from
     * @param osh Holder for Octet
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

    @Override
    protected void writeTemplate(OutputStream os) {
        os.write_long(getMagic());
        os.write_long(getSubcontractId());
        os.write_long(getServerId());
    }
}
