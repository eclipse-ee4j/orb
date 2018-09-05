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

public class OBVTestObjectOneImpl extends OBVTestObjectOne
{
    static Random r = new Random();
    
    OBVTestObjectOneImpl(int aInt, long aLong, float aFloat, double aDouble, String aString)
    {
        fInt = aInt;
        fLong = aLong;
        fFloat = aFloat;
        fDouble = aDouble;
        fString = aString;
    }

    public OBVTestObjectOneImpl()
    {
        fInt = r.nextInt();
        fLong = r.nextLong();
        fFloat = r.nextFloat();
        fDouble = r.nextDouble();
        fString = new String(fInt +""+ fLong +""+ fFloat +""+ fDouble);
    }

    OBVTestObjectOneImpl(org.omg.CORBA.portable.InputStream is)
    {
        // for unmarshalling only, called by the factory
    }

    public boolean equals(Object o)
    {
        try
            {
                OBVTestObjectOne ctbo = (OBVTestObjectOne)o;
                return ((ctbo.fString.equals(fString)) && (ctbo.fInt == fInt) &&
                        (ctbo.fLong == fLong) && (ctbo.fFloat == fFloat) && (ctbo.fDouble == fDouble)
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

}
