/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package corba.cdrstreams;

import java.io.*;

public class CustomMarshaled implements Serializable
{
    int value1;
    long value2;

    boolean good;

    public CustomMarshaled(int value1, long value2, boolean good)
    {
        this.value1 = value1;
        this.value2 = value2;
        this.good = good;
    }

    public boolean equals(Object obj)
    {
        CustomMarshaled gcm = (CustomMarshaled)obj;

        return (value1 == gcm.value1 && value2 == gcm.value2);
    }

    private void writeObject(java.io.ObjectOutputStream out)
        throws IOException
    {
        out.defaultWriteObject();

        byte[] buffer = new byte[1024];
        for (int i = 0; i < buffer.length; i++)
            buffer[i] = (byte)(i % 255);

        out.write(buffer);

        out.writeObject("CustomMarshaled 1.0");

    }

    private void readObject(java.io.ObjectInputStream in)
        throws IOException, ClassNotFoundException
    {
        in.defaultReadObject();

        if (good) {
            byte[] buffer = new byte[1024];
            in.readFully(buffer);

            for (int i = 0; i < buffer.length; i++)
                if (buffer[i] != (byte)(i % 255))
                    throw new IOException("Data buffer corrupted");

            if (!((String)(in.readObject())).equals("CustomMarshaled 1.0"))
                throw new IOException("Strings didn't match properly");
        } 

        // If it's a bad (has a bug) custom marshaler, it leaves the
        // string on the wire
    }
}
