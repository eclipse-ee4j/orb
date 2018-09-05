/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 1998-1999 IBM Corp. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0, which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package javax.rmi.CORBA.serialization;

import java.util.*;
import java.io.*;

public class ComplexTestObjectTwoSubclassDefaults extends ComplexTestObjectTwo
{
    public int fInt2;
    public long fLong2;

    public ComplexTestObjectTwoSubclassDefaults()
    {
        super();
        fInt2 = r.nextInt();
        fLong2 = r.nextLong();
    }

    public boolean equals(Object o)
    {
        try
            {
                ComplexTestObjectTwoSubclassDefaults ctbo = (ComplexTestObjectTwoSubclassDefaults)o;
                return ((ctbo.fInt2 == fInt2) &&
                        (ctbo.fLong2 == fLong2)
                        );
            }
        catch(Exception e)
            {
                return false;
            }
    }

    /**
     * Serialize out to output stream.
     */
    private void writeObject(ObjectOutputStream s) throws IOException
    {
        try
            {
                s.defaultWriteObject();
                s.writeDouble(55.5);
            }
        catch(IOException e)
            {
                throw e;
            }
    }

    /**
     * Serialize in from input stream.
     */
    private void readObject(ObjectInputStream s) throws IOException,
    ClassNotFoundException
    {
        try
            {
                s.defaultReadObject();
                double d = s.readDouble();
                if (d != 55.5)
                    throw new IOException("ComplexTestObjectTwoSubclassDefaults  - Read after defaultReadObject Failed!");
            }
        catch(IOException e)
            {
                throw e;
            }

    }

}
