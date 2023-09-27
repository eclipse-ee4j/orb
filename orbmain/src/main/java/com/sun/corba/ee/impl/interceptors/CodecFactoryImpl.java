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

package com.sun.corba.ee.impl.interceptors;

import org.omg.IOP.Codec;
import org.omg.IOP.CodecFactory;
import org.omg.IOP.CodecFactoryPackage.UnknownEncoding;
import org.omg.IOP.Encoding;
import org.omg.IOP.ENCODING_CDR_ENCAPS;

import com.sun.corba.ee.spi.logging.ORBUtilSystemException;

import org.omg.CORBA.ORB;

/**
 * CodecFactoryImpl is the implementation of the Codec Factory, as described in orbos/99-12-02.
 */
public final class CodecFactoryImpl extends org.omg.CORBA.LocalObject implements CodecFactory {
    // The ORB that created this Codec Factory
    private transient ORB orb;
    private static final ORBUtilSystemException wrapper = ORBUtilSystemException.self;

    // The maximum minor version of GIOP supported by this codec factory.
    // Currently, this is 1.2.
    private static final int MAX_MINOR_VERSION_SUPPORTED = 2;

    // The pre-created minor versions of Codec version 1.0, 1.1, ...,
    // 1.(MAX_MINOR_VERSION_SUPPORTED)
    private Codec codecs[] = new Codec[MAX_MINOR_VERSION_SUPPORTED + 1];

    /**
     * Creates a new CodecFactory implementation. Stores the ORB that created this factory, for later use by the Codec.
     * 
     * @param orb ORB that created this factory
     */
    public CodecFactoryImpl(ORB orb) {
        this.orb = orb;

        // Precreate a codec for version 1.0 through
        // 1.(MAX_MINOR_VERSION_SUPPORTED). This can be
        // done since Codecs are immutable in their current implementation.
        // This is an optimization that eliminates the overhead of creating
        // a new Codec each time create_codec is called.
        for (int minor = 0; minor <= MAX_MINOR_VERSION_SUPPORTED; minor++) {
            codecs[minor] = new CDREncapsCodec(orb, 1, minor);
        }
    }

    /**
     * Creates a codec of the given encoding. The only format recognized by this factory is ENCODING_CDR_ENCAPS, versions
     * 1.0 through 1.(MAX_MINOR_VERSION_SUPPORTED).
     *
     * @exception UnknownEncoding Thrown if this factory cannot create a Codec of the given encoding.
     */
    public Codec create_codec(Encoding enc) throws UnknownEncoding {
        if (enc == null)
            nullParam();

        Codec result = null;

        // This is the only format we can currently create codecs for:
        if ((enc.format == ENCODING_CDR_ENCAPS.value) && (enc.major_version == 1)) {
            if ((enc.minor_version >= 0) && (enc.minor_version <= MAX_MINOR_VERSION_SUPPORTED)) {
                result = codecs[enc.minor_version];
            }
        }

        if (result == null) {
            throw new UnknownEncoding();
        }

        return result;
    }

    /**
     * Called when an invalid null parameter was passed. Throws a BAD_PARAM with a minor code of 1
     */
    private void nullParam() {
        throw wrapper.nullParamNoComplete();
    }
}
