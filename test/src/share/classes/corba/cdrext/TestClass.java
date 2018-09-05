/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package corba.cdrext;

import java.io.*;

public class TestClass implements Serializable
{
    private NestedInnerClass instance;

    public TestClass() {
        instance = new NestedInnerClass();
    }

    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        
        try {
            return instance.equals(((TestClass)obj).instance);
        } catch (ClassCastException cce) {
            return false;
        }
    }

    private static class NestedInnerClass implements Externalizable {

        private NestedInnerClass() {
            data = 12344512L;
        }

        private long data;

        public void readExternal(ObjectInput decoder) 
            throws IOException, ClassNotFoundException {

            data = decoder.readLong();
        }

        public void writeExternal(ObjectOutput encoder) 
            throws IOException {

            encoder.writeLong(data);
        }

        public boolean equals(Object obj) {
            if (obj == null)
                return false;

            try {
                return data == ((NestedInnerClass)obj).data;
            } catch (ClassCastException cce) {
                return false;
            }
        }
    }
}


