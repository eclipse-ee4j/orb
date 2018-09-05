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

public class ComplexTestObjectFour implements Externalizable
{
    static Random r = new Random();
    int fInt;
    long fLong;
    float fFloat;
    double fDouble;
    String fString;

    ComplexTestObjectOne fOne = null;
    ComplexTestObjectTwo fTwo = null;

    public ComplexTestObjectFour()
    {
        fInt = r.nextInt();
        fLong = r.nextLong();
        fFloat = r.nextFloat();
        fDouble = r.nextDouble();
        fString = new String(fInt +""+ fLong +""+ fFloat +""+ fDouble);
        fOne = new ComplexTestObjectOne();
        fTwo = new ComplexTestObjectTwo();
    }

    public boolean equals(Object o)
    {
        try
            {
                ComplexTestObjectFour ctbo = (ComplexTestObjectFour)o;
                return ((ctbo.fString.equals(fString)) && (ctbo.fInt == fInt) && 
                        (ctbo.fLong == fLong) && (ctbo.fFloat == fFloat) && 
                        (ctbo.fDouble == fDouble)
                        && (ctbo.fOne.equals(fOne)) 
                        && (ctbo.fTwo.equals(fTwo))
                        );
            }
        catch(Exception e)
            {
                return false;
            }
    }

    public String toString()
    {
        return new String("fInt="+fInt+"; fLong="+fLong+"; fFloat="+fFloat+"; fDouble="+fDouble/*+"; fString="+fString*/);
    }

    public void writeExternal(ObjectOutput out)
        throws IOException {
        out.writeInt(fInt);
        out.writeLong(fLong);
        out.writeFloat(fFloat);
        out.writeDouble(fDouble);
        out.writeObject(fString);
        out.writeObject(fOne);
        out.writeObject(fTwo);
    }

    public void readExternal(ObjectInput in)
        throws IOException, ClassNotFoundException {
        fInt = in.readInt();
        fLong = in.readLong();
        fFloat = in.readFloat();
        fDouble = in.readDouble();
        fString = (String)in.readObject();
        fOne = (ComplexTestObjectOne)in.readObject();
        fTwo = (ComplexTestObjectTwo)in.readObject();

    }

}
