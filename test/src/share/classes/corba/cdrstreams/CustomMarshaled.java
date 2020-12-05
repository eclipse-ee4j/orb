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
