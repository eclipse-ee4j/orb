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

public class ComplexTestObjectXXX implements Serializable
{
    static Random r = new Random();
    int fInt;
    long fLong;
    float fFloat;
    double fDouble;
    String fString;
    
    Object fHandleAsAny = null;
    
    ComplexTestObjectOne fOne = null;
    ComplexTestObjectTwo fTwo = null;

    public ComplexTestObjectXXX()
    {
        fInt = r.nextInt();
        fLong = r.nextLong();
        fFloat = r.nextFloat();
        fDouble = r.nextDouble();
        fString = new String(fInt +""+ fLong +""+ fFloat +""+ fDouble);
        fHandleAsAny = new ComplexTestObjectOne();
        fOne = new ComplexTestObjectOne();
        fTwo = new ComplexTestObjectTwo();
    }

    public boolean equals(Object o)
    {
        try
            {
                ComplexTestObjectXXX ctbo = (ComplexTestObjectXXX)o;
                return ((ctbo.fString.equals(fString)) && (ctbo.fInt == fInt) && 
                        (ctbo.fLong == fLong) && (ctbo.fFloat == fFloat) && 
                        (ctbo.fDouble == fDouble)
                        && (ctbo.fHandleAsAny.equals(fHandleAsAny))
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
}
