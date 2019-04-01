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

import org.omg.CORBA.MARSHAL;
import org.omg.CORBA.OctetSeqHolder;
import org.omg.CORBA_2_3.portable.InputStream;

import com.sun.corba.ee.spi.ior.ObjectId;
import com.sun.corba.ee.spi.ior.ObjectKey;
import com.sun.corba.ee.spi.ior.ObjectKeyFactory;
import com.sun.corba.ee.spi.ior.ObjectKeyTemplate;

import com.sun.corba.ee.spi.orb.ORB;

import com.sun.corba.ee.spi.misc.ORBConstants;

import com.sun.corba.ee.spi.logging.IORSystemException;

import com.sun.corba.ee.impl.encoding.EncapsInputStream;
import com.sun.corba.ee.impl.encoding.EncapsInputStreamFactory;

/**
 * Based on the magic and scid, return the appropriate ObjectKeyTemplate. Expects to be called with a valid magic. If
 * scid is not valid, null should be returned.
 */
interface Handler {
    ObjectKeyTemplate handle(int magic, int scid, InputStream is, OctetSeqHolder osh);
}

/**
 * Singleton used to manufacture ObjectKey and ObjectKeyTemplate instances.
 *
 * @author Ken Cavanaugh
 */
public class ObjectKeyFactoryImpl implements ObjectKeyFactory {
    private static final IORSystemException wrapper = IORSystemException.self;

    public static final int MAGIC_BASE = 0xAFABCAFE;

    // Magic used in our object keys for JDK 1.2, 1.3, RMI-IIOP OP,
    // J2EE 1.0-1.2.1.
    public static final int JAVAMAGIC_OLD = MAGIC_BASE;

    // Magic used only in JDK 1.3.1. No format changes in object keys.
    public static final int JAVAMAGIC_NEW = MAGIC_BASE + 1;

    // New magic used in our object keys for JDK 1.4, J2EE 1.3 and later.
    // Format changes: all object keys have version string; POA key format
    // is changed.
    public static final int JAVAMAGIC_NEWER = MAGIC_BASE + 2;

    public static final int MAX_MAGIC = JAVAMAGIC_NEWER;

    // Beginning in JDK 1.3.1_01, we introduced changes which required
    // the ability to distinguish between JDK 1.3.1 FCS and the patch
    // versions. See OldJIDLObjectKeyTemplate.
    public static final byte JDK1_3_1_01_PATCH_LEVEL = 1;

    private final ORB orb;

    public ObjectKeyFactoryImpl(ORB orb) {
        this.orb = orb;
    }

    // The handlers still need to be made pluggable.
    //
    // I think this can be done as follows:
    // 1. Move the Handler interface into the SPI as ObjectKeyHandler.
    // 2. Add two methods to ObjectAdapterFactory:
    // ObjectKeyHandler getHandlerForObjectKey( ) ;
    // ObjectKeyHandler getHandlerForObjectKeyTemplate( ) ;
    // 3. Move the implementation of the fullKey handler and the
    // oktempOnly handler into TOAFactory and POAFactory.
    // 4. Move the ObjectKey impl classes into the impl/oa packages.
    // 5. Create an internal interface
    // interface HandlerFinder {
    // ObjectKeyHandler get( int scid ) ;
    // }
    // and modify create(InputStream,Handler,OctetSeqHolder)
    // to take a HandlerFinder instead of a Handler.
    // 6. Modify create( byte[] ) and createTemplate( InputStream )
    // to create an instance of HandlerFinder: something like:
    // new HandlerFinder() {
    // ObjectKeyHandler get( int scid )
    // {
    // return orb.getRequestDispatcherRegistry().
    // getObjectAdapterFactory( scid ).getHandlerForObjectKey() ;
    // }
    // and similarly for getHandlerForObjectKeyTemplate.

    /**
     * This handler reads the full object key, both the oktemp and the ID.
     */
    private Handler fullKey = new Handler() {
        public ObjectKeyTemplate handle(int magic, int scid, InputStream is, OctetSeqHolder osh) {
            ObjectKeyTemplate oktemp = null;

            if ((scid >= ORBConstants.FIRST_POA_SCID) && (scid <= ORBConstants.MAX_POA_SCID)) {
                if (magic >= JAVAMAGIC_NEWER) {
                    oktemp = new POAObjectKeyTemplate(orb, magic, scid, is, osh);
                } else {
                    oktemp = new OldPOAObjectKeyTemplate(orb, magic, scid, is, osh);
                }
            } else if ((scid >= 0) && (scid < ORBConstants.FIRST_POA_SCID)) {
                if (magic >= JAVAMAGIC_NEWER) {
                    oktemp = new JIDLObjectKeyTemplate(orb, magic, scid, is, osh);
                } else {
                    oktemp = new OldJIDLObjectKeyTemplate(orb, magic, scid, is, osh);
                }
            }

            return oktemp;
        }
    };

    /**
     * This handler reads only the oktemp.
     */
    private Handler oktempOnly = new Handler() {
        public ObjectKeyTemplate handle(int magic, int scid, InputStream is, OctetSeqHolder osh) {
            ObjectKeyTemplate oktemp = null;

            if ((scid >= ORBConstants.FIRST_POA_SCID) && (scid <= ORBConstants.MAX_POA_SCID)) {
                if (magic >= JAVAMAGIC_NEWER) {
                    oktemp = new POAObjectKeyTemplate(orb, magic, scid, is);
                } else {
                    oktemp = new OldPOAObjectKeyTemplate(orb, magic, scid, is);
                }
            } else if ((scid >= 0) && (scid < ORBConstants.FIRST_POA_SCID)) {
                if (magic >= JAVAMAGIC_NEWER) {
                    oktemp = new JIDLObjectKeyTemplate(orb, magic, scid, is);
                } else {
                    oktemp = new OldJIDLObjectKeyTemplate(orb, magic, scid, is);
                }
            }

            return oktemp;
        }
    };

    /**
     * Returns true iff magic is in the range of valid magic numbers for our ORB.
     */
    private boolean validMagic(int magic) {
        return (magic >= MAGIC_BASE) && (magic <= MAX_MAGIC);
    }

    /**
     * Creates an ObjectKeyTemplate from the InputStream. Most of the decoding is done inside the handler.
     */
    private ObjectKeyTemplate create(InputStream is, Handler handler, OctetSeqHolder osh) {
        ObjectKeyTemplate oktemp = null;

        try {
            int magic = is.read_long();

            if (validMagic(magic)) {
                int scid = is.read_long();
                oktemp = handler.handle(magic, scid, is, osh);
            }
        } catch (MARSHAL mexc) {
            wrapper.createMarshalError(mexc);
        }

        return oktemp;
    }

    public ObjectKey create(byte[] key) {

        OctetSeqHolder osh = new OctetSeqHolder();
        EncapsInputStream is = EncapsInputStreamFactory.newEncapsInputStream(orb, key, key.length);

        ObjectKeyTemplate oktemp;
        try {
            oktemp = create(is, fullKey, osh);
        } finally {
            try {
                is.close();
            } catch (java.io.IOException e) {
                wrapper.ioexceptionDuringStreamClose(e);
            }
        }
        if (oktemp == null) {
            oktemp = orb.getWireObjectKeyTemplate(); // cached singleton
            osh.value = key;
        }

        ObjectId oid = new ObjectIdImpl(osh.value);
        return new ObjectKeyImpl(oktemp, oid);
    }

    public ObjectKeyTemplate createTemplate(InputStream is) {
        ObjectKeyTemplate oktemp = create(is, oktempOnly, null);
        if (oktemp == null) {
            oktemp = orb.getWireObjectKeyTemplate(); // cached singleton
        }

        return oktemp;
    }
}
