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

public class ComplexTestObjectOne implements Serializable
{
    static Random r = new Random();
    public int fInt;
    public long fLong;
    public float fFloat;
    public double fDouble;
    public String fString;
    public java.lang.Object fMarshalAsAny;
    
    public ComplexTestObjectOne()
    {
        fInt = 1;//r.nextInt();
        fLong = 2;//r.nextLong();
        fFloat = 3;//r.nextFloat();
        fDouble = 4;//r.nextDouble();
        fString = new String(fInt +""+ fLong +""+ fFloat +""+ fDouble);
        fMarshalAsAny = (Object)new Character('S');
    }

    public boolean equals(Object o)
    {
        try
            {
                ComplexTestObjectOne ctbo = (ComplexTestObjectOne)o;
                return ((ctbo.fString.equals(fString)) && (ctbo.fInt == fInt) && 
                        (ctbo.fLong == fLong) && (ctbo.fFloat == fFloat) && 
                        (ctbo.fDouble == fDouble) && (ctbo.fMarshalAsAny.equals(fMarshalAsAny)));
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
}
