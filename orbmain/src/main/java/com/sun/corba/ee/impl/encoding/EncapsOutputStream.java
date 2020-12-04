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

package com.sun.corba.ee.impl.encoding;

import com.sun.corba.ee.spi.orb.ORB;

import com.sun.corba.ee.spi.ior.iiop.GIOPVersion;

import com.sun.corba.ee.spi.misc.ORBConstants;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Encapsulations are supposed to explicitly define their
 * code sets and GIOP version.  The original resolution to issue 2784 
 * said that the defaults were UTF-8 and UTF-16, but that was not
 * agreed upon.
 *
 * These streams currently use CDR 1.2 with ISO8859-1 for char/string and
 * UTF16 for wchar/wstring.  If no byte order marker is available,
 * the endianness of the encapsulation is used.
 *
 * When more encapsulations arise that have their own special code
 * sets defined, we can make all constructors take such parameters.
 */
public class EncapsOutputStream extends CDROutputObject
{

    // REVISIT - Right now, EncapsOutputStream's do not use
    // pooled byte buffers. This is controlled by the following
    // static constant. This should be re-factored such that
    // the EncapsOutputStream doesn't know it's using pooled
    // byte buffers.
    final static boolean usePooledByteBuffers = false;
    private static final InputObjectFactory INPUT_STREAM_FACTORY = new EncapsInputStreamFactory();

    // REVISIT - Right now, valuetypes in encapsulations will
    // only use stream format version 1, which may create problems
    // for service contexts or codecs (?).

    // corba/ORB
    // corba/ORBSingleton
    // iiop/ORB
    // iiop/GIOPImpl
    // corba/AnyImpl
    // IIOPProfileTemplate
    public EncapsOutputStream(ORB orb) {
        // GIOP version 1.2 with no fragmentation, big endian,
        // UTF8 for char data and UTF-16 for wide char data;
        this(orb, GIOPVersion.V1_2);
    }

    // CDREncapsCodec
    //
    // REVISIT.  A UTF-16 encoding with GIOP 1.1 will not work
    // with byte order markers.
    public EncapsOutputStream(ORB orb, GIOPVersion version) {
        super(orb, version, BufferManagerFactory.newWriteEncapsulationBufferManager(orb),
              ORBConstants.STREAM_FORMAT_VERSION_1, usePooledByteBuffers
        );
    }

    @Override
    public org.omg.CORBA.portable.InputStream create_input_stream() {
        freeInternalCaches();
        return createInputObject(null, INPUT_STREAM_FACTORY);
    }

    private static class EncapsInputStreamFactory implements InputObjectFactory {
        @Override
        public CDRInputObject createInputObject(CDROutputObject outputObject, ORB orb, ByteBuffer byteBuffer, int size, GIOPVersion giopVersion) {
            return com.sun.corba.ee.impl.encoding.EncapsInputStreamFactory.newEncapsInputStream(outputObject.orb(),
            		byteBuffer, size, ByteOrder.BIG_ENDIAN, giopVersion);
        }
    }
    
    @Override
    protected CodeSetConversion.CTBConverter createCharCTBConverter() {
        return CodeSetConversion.impl().getCTBConverter(
            OSFCodeSetRegistry.ISO_8859_1);
    }

    @Override
    protected CodeSetConversion.CTBConverter createWCharCTBConverter() {
        if (getGIOPVersion().equals(GIOPVersion.V1_0))
            throw wrapper.wcharDataInGiop10();            

        // In the case of GIOP 1.1, we take the byte order of the stream 
        // and don't use byte order markers since we're limited to a 2 byte
        // fixed width encoding.
        if (getGIOPVersion().equals(GIOPVersion.V1_1))
            return CodeSetConversion.impl().getCTBConverter(OSFCodeSetRegistry.UTF_16, false, false);

        // Assume anything else meets GIOP 1.2 requirements
        //
        // Use byte order markers?  If not, use big endian in GIOP 1.2.  
        // (formal 00-11-03 15.3.16)

        boolean useBOM = ((ORB)orb()).getORBData().useByteOrderMarkersInEncapsulations();

        return CodeSetConversion.impl().getCTBConverter(OSFCodeSetRegistry.UTF_16, false, useBOM);
    }
}
