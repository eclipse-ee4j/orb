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

import com.sun.corba.ee.spi.ior.iiop.GIOPVersion;
import com.sun.corba.ee.spi.logging.ORBUtilSystemException;
import com.sun.org.omg.SendingContext.CodeBase;

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
public class EncapsInputStream extends CDRInputObject
{
    private static final ORBUtilSystemException wrapper =
        ORBUtilSystemException.self ;

    public EncapsInputStream(org.omg.CORBA.ORB orb, byte[] buf,
                             int size, ByteOrder byteOrder,
                             GIOPVersion version) {
        super(orb, ByteBuffer.wrap(buf), size, byteOrder, version,
                BufferManagerFactory.newReadEncapsulationBufferManager()
        );

        performORBVersionSpecificInit();
    }

    public EncapsInputStream(org.omg.CORBA.ORB orb, ByteBuffer byteBuffer,
                             int size, ByteOrder byteOrder,
                             GIOPVersion version) {
        super(orb, byteBuffer, size, byteOrder, version,
                BufferManagerFactory.newReadEncapsulationBufferManager()
        );

        performORBVersionSpecificInit();
    }

    // exported to Glassfish - DON'T change this!!!
    public EncapsInputStream(org.omg.CORBA.ORB orb, byte[] data, int size) 
    {
        this(orb, data, size, GIOPVersion.V1_2);
    }
    
    // corba/AnyImpl
    public EncapsInputStream(EncapsInputStream eis) 
    {
        super(eis);

        performORBVersionSpecificInit();
    }

    // CDREncapsCodec
    // ServiceContext
    //
    // Assumes big endian (can use consumeEndian to read and set
    // the endianness if it is an encapsulation with a byte order
    // mark at the beginning)
    public EncapsInputStream(org.omg.CORBA.ORB orb, byte[] data, int size, GIOPVersion version) 
    {
        this(orb, data, size, ByteOrder.BIG_ENDIAN, version);
    }

    /**
     * Full constructor with a CodeBase parameter useful for
     * unmarshaling RMI-IIOP valuetypes (technically against the
     * intention of an encapsulation, but necessary due to OMG
     * issue 4795.  Used by ServiceContexts.
     * @param orb the ORB
     * @param data data to read in
     * @param size size of data
     * @param version GIOP version
     * @param codeBase CodeBase to use
     */
    public EncapsInputStream(org.omg.CORBA.ORB orb, 
                             byte[] data, 
                             int size, 
                             GIOPVersion version, 
                             CodeBase codeBase) {
        super(orb, 
              ByteBuffer.wrap(data), 
              size, 
              ByteOrder.BIG_ENDIAN,
              version,
                BufferManagerFactory.newReadEncapsulationBufferManager()
        ); // IDLJavaSerializationInputStream::directRead == false

        this.codeBase = codeBase;

        performORBVersionSpecificInit();
    }

    @Override
    public CDRInputObject dup() {
        return EncapsInputStreamFactory.newEncapsInputStream(this);
    }

    @Override
    protected CodeSetConversion.BTCConverter createCharBTCConverter() {
        return CodeSetConversion.impl().getBTCConverter(
            OSFCodeSetRegistry.ISO_8859_1);
    }

    @Override
    protected CodeSetConversion.BTCConverter createWCharBTCConverter() {
        // Wide characters don't exist in GIOP 1.0
        if (getGIOPVersion().equals(GIOPVersion.V1_0))
            throw wrapper.wcharDataInGiop10();

        // In GIOP 1.1, we shouldn't have byte order markers.  Take the order
        // of the stream if we don't see them.
        if (getGIOPVersion().equals(GIOPVersion.V1_1))
            return CodeSetConversion.impl().getBTCConverter(OSFCodeSetRegistry.UTF_16, getByteOrder());

        // Assume anything else adheres to GIOP 1.2 requirements.
        //
        // Our UTF_16 converter will work with byte order markers, and if
        // they aren't present, it will use the provided endianness.
        //
        // With no byte order marker, it's big endian in GIOP 1.2.  
        // formal 00-11-03 15.3.16.
        return CodeSetConversion.impl().getBTCConverter(OSFCodeSetRegistry.UTF_16, ByteOrder.BIG_ENDIAN);
    }

    @Override
    public CodeBase getCodeBase() {
        return codeBase;
    }

    private CodeBase codeBase;
}
