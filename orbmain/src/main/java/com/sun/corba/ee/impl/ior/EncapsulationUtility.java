/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2019 Payara Services Ltd.
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

import java.util.List;

import org.omg.CORBA_2_3.portable.OutputStream;
import org.omg.CORBA_2_3.portable.InputStream;

import com.sun.corba.ee.spi.ior.Identifiable;
import com.sun.corba.ee.spi.ior.IdentifiableFactoryFinder;
import com.sun.corba.ee.spi.ior.WriteContents;

import com.sun.corba.ee.spi.orb.ORB;

import com.sun.corba.ee.impl.encoding.CDROutputObject;
import com.sun.corba.ee.impl.encoding.EncapsOutputStream;
import com.sun.corba.ee.impl.encoding.OutputStreamFactory;
import com.sun.corba.ee.impl.encoding.EncapsInputStream;
import com.sun.corba.ee.impl.encoding.EncapsInputStreamFactory;

/**
 * This static utility class contains various utility methods for reading and writing CDR encapsulations.
 *
 * @author Ken Cavanaugh
 */
public final class EncapsulationUtility {
    private EncapsulationUtility() {
    }

    /**
     * Read the count from is, then read count Identifiables from is using the factory. Add each constructed Identifiable to
     * container.
     * 
     * @param <E> extends {@link Identifiable}
     * @param container List to add constructed Identifiables to
     * @param finder Factory to use in creation
     * @param istr Stream to read from
     */
    public static <E extends Identifiable> void readIdentifiableSequence(List<E> container, IdentifiableFactoryFinder<E> finder,
            InputStream istr) {
        int count = istr.read_long();
        for (int ctr = 0; ctr < count; ctr++) {
            int id = istr.read_long();
            E obj = finder.create(id, istr);
            container.add(obj);
        }
    }

    /**
     * Write all Identifiables that we contain to os. The total length must be written before this method is called.
     * 
     * @param <E> extends {@link Identifiable}
     * @param container List of Identifiables
     * @param os Stream to write to
     */
    public static <E extends Identifiable> void writeIdentifiableSequence(List<E> container, OutputStream os) {
        os.write_long(container.size());
        for (Identifiable obj : container) {
            os.write_long(obj.getId());
            obj.write(os);
        }
    }

    /**
     * Helper method that is used to extract data from an output stream and write the data to another output stream. Defined
     * as static so that it can be used in another class.
     * 
     * @param dataStream Stream to get data from
     * @param os Stream to write to
     */
    public static void writeOutputStream(OutputStream dataStream, OutputStream os) {
        byte[] data = ((CDROutputObject) dataStream).toByteArray();
        os.write_long(data.length);
        os.write_octet_array(data, 0, data.length);
    }

    /**
     * Helper method to read the octet array from is, deencapsulate it, and return as another InputStream. This must be
     * called inside the constructor of a derived class to obtain the correct stream for unmarshalling data.
     * 
     * @param orb The ORB
     * @param is Stream to read from
     * @return Deencapsulated InputStream
     */
    public static InputStream getEncapsulationStream(ORB orb, InputStream is) {
        byte[] data = readOctets(is);
        EncapsInputStream result = EncapsInputStreamFactory.newEncapsInputStream(orb, data, data.length);
        result.consumeEndian();
        return result;
    }

    /**
     * Helper method that reads an octet array from an input stream. Defined as static here so that it can be used in
     * another class.
     * 
     * @param is Stream to read from
     * @return Array of bytes from stream
     */
    public static byte[] readOctets(InputStream is) {
        int len = is.read_ulong();
        byte[] data = new byte[len];
        is.read_octet_array(data, 0, len);
        return data;
    }

    public static void writeEncapsulation(WriteContents obj, OutputStream os) {
        EncapsOutputStream out = OutputStreamFactory.newEncapsOutputStream((ORB) os.orb());

        out.putEndian();

        obj.writeContents(out);

        writeOutputStream(out, os);
    }
}
