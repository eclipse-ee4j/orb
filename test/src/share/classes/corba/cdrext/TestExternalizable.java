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
