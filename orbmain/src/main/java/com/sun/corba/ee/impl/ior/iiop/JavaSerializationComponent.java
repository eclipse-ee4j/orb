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

package com.sun.corba.ee.impl.ior.iiop;

import com.sun.corba.ee.spi.ior.TaggedComponentBase;
import com.sun.corba.ee.spi.misc.ORBConstants;

import org.omg.CORBA_2_3.portable.OutputStream;

/**
 * Tagged component that contains a value that indicates the Java 
 * serialization version supported by the ORB.
 *
 * ORB Java serialization uses IIOP as the transport protocol, but uses
 * Java serialization mechanism and its accompanying encodings, instead
 * of IIOP CDR serialization mechanism. Java serialization is generally
 * observed to be faster than CDR.
 */ 
public class JavaSerializationComponent extends TaggedComponentBase {

    private byte version;

    private static JavaSerializationComponent singleton;

    static {
        singleton = new JavaSerializationComponent(
                                               ORBConstants.JAVA_ENC_VERSION);
    }

    public static JavaSerializationComponent singleton() {
        return singleton;
    }

    public JavaSerializationComponent(byte version) {
        this.version = version;
    }

    public byte javaSerializationVersion() {
        return this.version;
    }

    public void writeContents(OutputStream os) {
        os.write_octet(version);
    }
    
    public int getId() {
        return ORBConstants.TAG_JAVA_SERIALIZATION_ID;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof JavaSerializationComponent)) {
            return false;
        }
        JavaSerializationComponent other = (JavaSerializationComponent) obj;
        return this.version == other.version;
    }

    public int hashCode() {
        return this.version;
    }
}
