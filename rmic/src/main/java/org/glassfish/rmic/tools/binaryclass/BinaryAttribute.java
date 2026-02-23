/*
 * Copyright (c) 1994, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.rmic.tools.binaryclass;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.glassfish.rmic.tools.java.Constants;
import org.glassfish.rmic.tools.java.Environment;
import org.glassfish.rmic.tools.java.Identifier;

/**
 * This class is used to represent an attribute from a binary class.
 * This class should go away once arrays are objects.
 *
 * WARNING: The contents of this source file are not part of any
 * supported API.  Code that depends on them does so at its own risk:
 * they are subject to change or removal without notice.
 */
public final
class BinaryAttribute implements Constants {
    Identifier name;
    byte data[];
    BinaryAttribute next;

    /**
     * Constructor
     */
    BinaryAttribute(Identifier name, byte data[], BinaryAttribute next) {
        this.name = name;
        this.data = data;
        this.next = next;
    }

    /**
     * Load a list of attributes
     */
    public static BinaryAttribute load(DataInputStream in, BinaryConstantPool cpool, int mask) throws IOException {
        BinaryAttribute atts = null;
        int natt = in.readUnsignedShort();  // JVM 4.6 method_info.attrutes_count

        for (int i = 0 ; i < natt ; i++) {
            // id from JVM 4.7 attribute_info.attribute_name_index
            Identifier id = cpool.getIdentifier(in.readUnsignedShort());
            // id from JVM 4.7 attribute_info.attribute_length
            int len = in.readInt();

            if (id.equals(idCode) && ((mask & ATT_CODE) == 0)) {
                in.skipBytes(len);
            } else {
                byte data[] = new byte[len];
                in.readFully(data);
                atts = new BinaryAttribute(id, data, atts);
            }
        }
        return atts;
    }

    // write out the Binary attributes to the given stream
    // (note that attributes may be null)
    static void write(BinaryAttribute attributes, DataOutputStream out,
                      BinaryConstantPool cpool, Environment env) throws IOException {
        // count the number of attributes
        int attributeCount = 0;
        for (BinaryAttribute att = attributes; att != null; att = att.next)
            attributeCount++;
        out.writeShort(attributeCount);

        // write out each attribute
        for (BinaryAttribute att = attributes; att != null; att = att.next) {
            Identifier name = att.name;
            byte data[] = att.data;
            // write the identifier
            out.writeShort(cpool.indexString(name.toString(), env));
            // write the length
            out.writeInt(data.length);
            // write the data
            out.write(data, 0, data.length);
        }
    }

    /**
     * Accessors
     */

    public Identifier getName() { return name; }

    public byte getData()[] { return data; }

    public BinaryAttribute getNextAttribute() { return next; }

}
