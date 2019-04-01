/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.orb;

import org.omg.CORBA.portable.OutputStream;

import com.sun.corba.ee.spi.orb.ORBVersion;

public class ORBVersionImpl implements ORBVersion {
    private byte orbType;

    public ORBVersionImpl(byte orbType) {
        this.orbType = orbType;
    }

    public static final ORBVersion FOREIGN = new ORBVersionImpl(ORBVersion.FOREIGN);

    public static final ORBVersion OLD = new ORBVersionImpl(ORBVersion.OLD);

    public static final ORBVersion NEW = new ORBVersionImpl(ORBVersion.NEW);

    public static final ORBVersion JDK1_3_1_01 = new ORBVersionImpl(ORBVersion.JDK1_3_1_01);

    public static final ORBVersion NEWER = new ORBVersionImpl(ORBVersion.NEWER);

    public static final ORBVersion PEORB = new ORBVersionImpl(ORBVersion.PEORB);

    public byte getORBType() {
        return orbType;
    }

    public void write(OutputStream os) {
        os.write_octet((byte) orbType);
    }

    public String toString() {
        return "ORBVersionImpl[" + Byte.toString(orbType) + "]";
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof ORBVersion))
            return false;

        ORBVersion version = (ORBVersion) obj;
        return version.getORBType() == orbType;
    }

    public int hashCode() {
        return orbType;
    }

    public boolean lessThan(ORBVersion version) {
        return orbType < version.getORBType();
    }

    public int compareTo(ORBVersion obj) {
        // The Comparable interface says that this
        // method throws a ClassCastException if the
        // given object's type prevents it from being
        // compared.
        return getORBType() - obj.getORBType();
    }
}
