/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.sun.corba.ee.impl.ior;

import java.util.Arrays;
import com.sun.corba.ee.spi.ior.ObjectId;
import org.omg.CORBA_2_3.portable.OutputStream;
import com.sun.corba.ee.impl.misc.ORBUtility;

/**
 * @author Ken Cavanaugh
 */
public final class ObjectIdImpl implements ObjectId {
    private byte[] id;

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ObjectIdImpl))
            return false;

        ObjectIdImpl other = (ObjectIdImpl) obj;

        return Arrays.equals(this.id, other.id);
    }

    @Override
    public int hashCode() {
        int result = 17;
        for (int ctr = 0; ctr < id.length; ctr++)
            result = 37 * result + id[ctr];
        return result;
    }

    public ObjectIdImpl(byte[] id) {
        if (id == null) {
            this.id = null;
        } else {
            this.id = (byte[]) id.clone();
        }
    }

    public String getIdString() {
        return ORBUtility.dumpBinary(id);
    }

    public String toString() {
        return "ObjectIdImpl[" + getIdString() + "]";
    }

    public byte[] getId() {
        return (byte[]) id.clone();
    }

    public void write(OutputStream os) {
        os.write_long(id.length);
        os.write_octet_array(id, 0, id.length);
    }
}
