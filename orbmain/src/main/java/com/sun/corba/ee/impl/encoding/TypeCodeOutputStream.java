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
import com.sun.corba.ee.spi.orb.ORB;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;

import org.omg.CORBA_2_3.portable.OutputStream;

public final class TypeCodeOutputStream extends EncapsOutputStream {

    private static final InputObjectFactory TYPE_CODE_INPUT_OBJECT_FACTORY = new TypeCodeInputStreamFactory();

    private OutputStream enclosure = null;
    private Map<String,Integer> typeMap = null;
    private boolean isEncapsulation = false;

    public TypeCodeOutputStream(ORB orb) {
        super(orb);
    }

    @Override
    public org.omg.CORBA.portable.InputStream create_input_stream() {
        return createInputObject(null, TYPE_CODE_INPUT_OBJECT_FACTORY);
    }

    private static class TypeCodeInputStreamFactory implements InputObjectFactory {
        @Override
        public CDRInputObject createInputObject(CDROutputObject outputObject, ORB orb, ByteBuffer byteBuffer, int size, GIOPVersion giopVersion) {
            return EncapsInputStreamFactory.newTypeCodeInputStream(outputObject.orb(), byteBuffer, size, ByteOrder.BIG_ENDIAN, giopVersion);
        }
    }

    public void setEnclosingOutputStream(OutputStream enclosure) {
        this.enclosure = enclosure;
    }

    public TypeCodeOutputStream getTopLevelStream() {
        if (enclosure == null)
            return this;
        if (enclosure instanceof TypeCodeOutputStream)
            return ((TypeCodeOutputStream)enclosure).getTopLevelStream();
        return this;
    }

    public int getTopLevelPosition() {
        if (enclosure != null && enclosure instanceof TypeCodeOutputStream) {
            int pos = ((TypeCodeOutputStream)enclosure).getTopLevelPosition()
                + getPosition();
            // Add four bytes for the encaps length, not another 4 for the 
            // byte order which is included in getPosition().
            if (isEncapsulation) {
                pos += 4;
            }

            return pos;
        }
        return getPosition();
    }

    public void addIDAtPosition(String id, int position) {
        if (typeMap == null)
            typeMap = new HashMap<String,Integer>(16);
        typeMap.put(id, position);
    }

    public int getPositionForID(String id) {
        if (typeMap == null)
            throw wrapper.refTypeIndirType() ;
        return
            typeMap.get(id) ;
    }

    public TypeCodeOutputStream createEncapsulation(org.omg.CORBA.ORB _orb) {
        TypeCodeOutputStream encap = OutputStreamFactory.newTypeCodeOutputStream((ORB)_orb);
        encap.setEnclosingOutputStream(this);
        encap.makeEncapsulation();
        return encap;
    }

    protected void makeEncapsulation() {
        // first entry in an encapsulation is the endianess
        putEndian();
        isEncapsulation = true;
    }

    public static TypeCodeOutputStream wrapOutputStream(OutputStream os) {
        TypeCodeOutputStream tos = OutputStreamFactory.newTypeCodeOutputStream((ORB)os.orb());
        tos.setEnclosingOutputStream(os);
        return tos;
    }

    public int getPosition() {
        return getIndex();
    }

    @Override
    public int getRealIndex(int index) {
        return getTopLevelPosition();
    }

    public byte[] getTypeCodeBuffer() {
        return toByteArray(4);
    }

}
