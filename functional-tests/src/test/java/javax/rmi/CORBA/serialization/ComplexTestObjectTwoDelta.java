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

public class ComplexTestObjectTwoDelta implements Serializable
{
    static Random r = new Random();
    public int fInt;
    public long fLong;
    public float fFloat;
    public double fDouble;
    public String fString;
    public ComplexTestObjectOne fOne = null;

    public ComplexTestObjectTwoDelta()
    {
        fInt = r.nextInt();
        fLong = r.nextLong();
        fFloat = r.nextFloat();
        fDouble = r.nextDouble();
        fString = new String(fInt +""+ fLong +""+ fFloat +""+ fDouble);
        fOne = new ComplexTestObjectOne();
    }

    public boolean equals(Object o)
    {
        try
            {
                ComplexTestObjectTwoDelta ctbo = (ComplexTestObjectTwoDelta)o;
                return ((ctbo.fString.equals(fString)) && (ctbo.fInt == fInt) &&
                        (ctbo.fLong == fLong) && (ctbo.fFloat == fFloat) && (ctbo.fDouble == fDouble)
                        && (ctbo.fOne.equals(fOne))
                        );
            }
        catch(Exception e)
            {
                return false;
            }
    }

    public String toString()
    {
        return new String("fInt="+fInt+"; fLong="+fLong+"; fFloat="+fFloat+"; fDouble="+fDouble+"; fString="+fString);
    }

    /**
     * Serialize out to output stream.
     */
    private void writeObject(ObjectOutputStream s) throws IOException
    {
        try
            {
                s.defaultWriteObject();
            }
        catch(IOException e)
            {
                throw e;
            }
    }

}

