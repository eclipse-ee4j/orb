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
