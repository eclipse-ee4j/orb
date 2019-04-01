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

import java.util.Iterator;

import org.omg.CORBA_2_3.portable.OutputStream;

import com.sun.corba.ee.spi.ior.ObjectAdapterId;

abstract class ObjectAdapterIdBase implements ObjectAdapterId {
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof ObjectAdapterId))
            return false;

        ObjectAdapterId theOther = (ObjectAdapterId) other;

        Iterator<String> iter1 = iterator();
        Iterator<String> iter2 = theOther.iterator();

        while (iter1.hasNext() && iter2.hasNext()) {
            String str1 = iter1.next();
            String str2 = iter2.next();

            if (!str1.equals(str2))
                return false;
        }

        return iter1.hasNext() == iter2.hasNext();
    }

    @Override
    public int hashCode() {
        int result = 17;
        for (String str : this) {
            result = 37 * result + str.hashCode();
        }
        return result;
    }

    @Override
    public String toString() {
        StringBuilder buff = new StringBuilder();
        buff.append("ObjectAdapterID[");

        boolean first = true;
        for (String str : this) {
            if (first)
                first = false;
            else
                buff.append("/");

            buff.append(str);
        }

        buff.append("]");

        return buff.toString();
    }

    public void write(OutputStream os) {
        os.write_long(getNumLevels());
        for (String str : this) {
            os.write_string(str);
        }
    }
}
