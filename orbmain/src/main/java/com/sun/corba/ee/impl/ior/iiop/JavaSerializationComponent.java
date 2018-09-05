/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.ior.iiop;

import org.omg.CORBA_2_3.portable.OutputStream;

import com.sun.corba.ee.spi.misc.ORBConstants;
import com.sun.corba.ee.impl.misc.ORBUtility;

import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.ior.TaggedComponentBase;

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
