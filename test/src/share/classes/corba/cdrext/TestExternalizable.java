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

public class TestExternalizable implements Externalizable
{
    private long data1;
    private String data2;
    private int data3;
    private char data4;

    public TestExternalizable() {}

    public TestExternalizable(long data1,
                              String data2,
                              int data3,
                              char data4) {
        this.data1 = data1;
        this.data2 = data2;
        this.data3 = data3;
        this.data4 = data4;
    }

    public boolean equals(Object obj) {
        try {
            if (obj == null)
                return false;
            
            TestExternalizable other
                = (TestExternalizable)obj;

            return (data1 == other.data1 &&
                    (data2 == null ||
                     data2.equals(other.data2)) &&
                    data3 == other.data3 &&
                    data4 == other.data4);
        } catch (ClassCastException cce) {
            return false;
        }
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeLong(data1);

        // Check to make sure these are working
        // the same
        out.writeObject(data2);
        out.writeUTF(data2);

        out.writeInt(data3);

        out.writeChar(data4);
    }

    public void readExternal(ObjectInput in) 
        throws IOException, ClassNotFoundException {

        data1 = in.readLong();

        String data2_obj = (String)in.readObject();
        String data2_utf = in.readUTF();

        if (data2_obj == null && data2_obj != data2_utf)
            throw new IOException("data2_obj null mismatch");
        else
        if (data2_obj != null && !data2_obj.equals(data2_utf))
            throw new IOException("data2_obj data2_utf mismatch");

        data2 = data2_obj;

        data3 = in.readInt();

        data4 = in.readChar();
    }
}
